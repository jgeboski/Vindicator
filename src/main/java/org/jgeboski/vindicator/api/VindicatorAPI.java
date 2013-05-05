/*
 * Copyright 2012-2013 James Geboski <jgeboski@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jgeboski.vindicator.api;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jgeboski.vindicator.storage.Storage;
import org.jgeboski.vindicator.storage.StorageException;
import org.jgeboski.vindicator.storage.StorageSQL;
import org.jgeboski.vindicator.util.Log;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.StrUtils;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.Vindicator;

import static org.jgeboski.vindicator.util.Message.hl;

public class VindicatorAPI extends ThreadPoolExecutor
{
    public Vindicator vind;
    public Storage    storage;

    public HashMap<String, APIRecord> mutes;

    public VindicatorAPI(Vindicator vind)
        throws APIException
    {
        super(vind.config.poolMinSize, vind.config.poolMaxSize,
              vind.config.poolKeepAlive, TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<Runnable>(),
              Executors.defaultThreadFactory());

        this.vind  = vind;
        this.mutes = new HashMap<String, APIRecord>();

        /* For now, SQL only */
        storage = new StorageSQL(
            vind.config.storeURL,  vind.config.storeUser,
            vind.config.storePass, vind.config.storePrefix);

        for (Player p : vind.getServer().getOnlinePlayers()) {
            for (APIRecord r : storage.getRecords(p.getName())) {
                if (r.hasFlag(APIRecord.MUTE))
                    mutes.put(p.getName(), r);
            }
        }
    }

    public void close()
    {
        shutdown();
        storage.close();
    }

    public void checkAddresses(String player, String address)
        throws APIException
    {
        HashSet<String> pl;
        APIAddress aa;
        long val;

        address = getAddress(address);
        aa      = storage.getAddress(player, address);
        pl      = new HashSet<String>();

        if (aa != null) {
            aa.time = Utils.time();
            aa.logins++;
            storage.update(aa);
        } else {
            aa = new APIAddress(null, player, address);
            storage.add(aa);
        }

        if ((vind.config.altInfoLogins == 0) || (vind.config.altInfoTime == 0))
            return;

        for (APIAddress a : storage.getAddressPlayers(address)) {
            if (player.equals(a.player))
                continue;

            val = vind.config.altInfoLogins;

            if ((vind.config.altInfoLogins >= 0) && (a.logins > val))
                continue;

            val = vind.config.altInfoTime + a.time;

            if ((vind.config.altInfoTime >= 0) && (Utils.time() > val))
                continue;

            pl.add(hl(a.player));
        }

        if (pl.size() < 1)
            return;

        vind.broadcast("vindicator.message.notify",
                       "Player %s has %s alternates(s): %s",
                       hl(player), hl(pl.size()),
                       StrUtils.join(pl.toArray(new String[0]), ", "));
    }

    public void checkRecords(String player, String address)
        throws APIException
    {
        APIRecord br;
        APIRecord mr;
        String    str;
        int       nc;

        br = null;
        mr = null;
        nc = 0;

        for (APIRecord r : getAllRecords(player, address)) {
            if (r.hasFlag(APIRecord.BAN)) {
                br = r;
                break;
            } else if (r.hasFlag(APIRecord.MUTE)) {
                mr = r;
            } else if (r.hasFlag(APIRecord.NOTE)) {
                nc++;
            }
        }

        if (br != null) {
            if ((br.timeout < 1) || (br.timeout > Utils.time())) {
                if (br.hasFlag(APIRecord.ADDRESS))
                    str = "Player %s attempted to join with a banned IP: %s";
                else
                    str = "Player %s attempted to join banned: %s";

                vind.broadcast("vindicator.message.notify", str,
                               hl(player), hl(br.message));

                str = new String();

                if (br.timeout > 0) {
                    str += " until ";
                    str += hl(Utils.timestr(Utils.DATEF_LONG, br.timeout));
                }

                throw new APIException("Banned%s: %s", str, hl(br.message));
            }

            br.issuer = vind.getDescription().getName();
            unban(br);
        }

        if (mr != null) {
            if ((mr.timeout > 0) && (mr.timeout < Utils.time())) {
                mr.issuer = vind.getDescription().getName();
                unmute(mr);
                mutes.remove(player);
                mr = null;
            } else {
                mutes.put(player, mr);
            }
        }

        if ((nc < 1) && (mr == null))
            return;

        str = String.format("Player %s has %s note(s)", hl(player), hl(nc));

        if (mr != null)
            str += ", and is " + hl("muted");

        str += ".";
        vind.broadcast("vindicator.message.notify", str);
    }

    public void checkChat(String player, String message)
        throws APIException
    {
        APIRecord mr;

        mr = mutes.get(player);

        if (mr == null)
            return;

        if ((mr.timeout < 1) || (mr.timeout > Utils.time())) {
            Log.info("Player %s attempted to speak muted: %s", player, message);
            throw new APIException("You cannot speak while being muted!");
        }

        mr.issuer = vind.getDescription().getName();
        unmute(mr);
    }

    public void login(APILogin al)
        throws APIException
    {
        al.address = getAddress(al.address);
        al.setTask(this, "loginTask");
        execrun(al);
    }

    private void loginTask(APILogin al)
        throws APIException
    {
        try {
            checkAddresses(al.pname, al.address);
            checkRecords(al.pname, al.address);
        } catch (APIException e) {
            if (!(e instanceof StorageException))
                throw e;

            Log.severe(e.getMessage());
            throw new APIException("Failed username check. Notify the admin.");
        }
    }

    public void ban(APIRecord ar)
        throws APIException
    {
        String str;

        if (ar.message == null) {
            if (vind.config.mustReason)
                throw new APIException("A reason must be provided.");

            ar.message = vind.config.defBanReason;
        }

        ar.target = getTarget(ar.target);
        ar.flags  = 0;

        ar.addFlag(APIRecord.BAN);
        ar.addFlag(getTypeFlag(ar));
        ar.setTask(this, "banTask");

        str = new String();

        if (ar.timeout > 0) {
            ar.timeout += Utils.time();
            str        += " until ";
            str        += hl(Utils.timestr(Utils.DATEF_LONG, ar.timeout));
        }

        str = String.format("Banned%s: %s", str, hl(ar.message));

        if (ar.hasFlag(APIRecord.ADDRESS))
            kickIP(ar, str);
        else
            kick(ar, str);

        execrun(ar);
    }

    private void banTask(APIRecord ar)
        throws APIException
    {
        APIRecord br;
        String    str;

        br = null;

        for (APIRecord r : storage.getRecords(ar)) {
            if (!r.hasFlag(APIRecord.BAN))
                continue;

            br = r;
            break;
        }

        if (br != null) {
            if (!vind.config.banUpdate) {
                throw new APIException("Ban already exists on %s.",
                                       hl(ar.target));
            }

            str   = "updated";
            ar.id = br.id;
            storage.update(ar);
        } else {
            str   = "placed";
            storage.add(ar);
        }

        vind.broadcast("vindicator.message.ban",
                       "Ban %s on %s by %s: %s",
                       str, hl(ar.target), hl(ar.issuer), hl(ar.message));

        if (ar.timeout < 1)
            return;

        vind.broadcast("vindicator.message.ban",
                       "Temporary ban will be removed: %s",
                       hl(Utils.timestr(Utils.DATEF_LONG, ar.timeout)));
    }

    public void kick(APIRecord ar)
        throws APIException
    {
        if (ar.message == null) {
            if (vind.config.mustReason)
                throw new APIException("A reason must be provided.");

            ar.message = vind.config.defKickReason;
        }

        if (StrUtils.isAddress(ar.target)) {
            if (kickIP(ar, hl(ar.message)))
                return;

            throw new APIException("Player(s) for %s not found.",
                                   hl(ar.target));
        }

        ar.target = getTarget(ar.target);

        if (!kick(ar, hl(ar.message)))
            throw new APIException("Player %s not found.", hl(ar.target));

        vind.broadcast("vindicator.message.kick",
                       "Kick placed on %s by %s: %s",
                       hl(ar.target), hl(ar.issuer), hl(ar.message));
    }

    public void lookup(APIRecord ar)
        throws APIException
    {
        ar.target = getTarget(ar.target);
        ar.setTask(this, "lookupTask");
        execrun(ar);
    }

    private List<APIRecord> lookupTask(APIRecord ar)
        throws APIException
    {
        ArrayList<APIRecord> ars;

        int m;
        int b;
        int n;

        ars = new ArrayList<APIRecord>();
        m   = b = n = 0;

        for (APIRecord r : getAllRecords(ar.target)) {
            if (r.hasFlag(APIRecord.BAN)) {
                ars.add(b, r);
                b++;
            } else if (r.hasFlag(APIRecord.NOTE)) {
                r.id = n + 1;
                ars.add(b + m + n, r);
                n++;
            } else if (r.hasFlag(APIRecord.MUTE)) {
                ars.add(b + m, r);
                m++;
            }
        }

        return ars;
    }

    public void lookupa(APIAddress aa)
        throws APIException
    {
        if (aa.player != null)
            aa.player = getTarget(aa.player);

        if (aa.address != null)
            aa.address = getAddress(aa.address);

        aa.setTask(this, "lookupaTask");
        execrun(aa);
    }

    private List<APIAddress> lookupaTask(APIAddress aa)
        throws APIException
    {
        if (aa.player != null)
            return storage.getAddresses(aa.player);

        if (aa.address != null)
            return storage.getAddressPlayers(aa.address);

        return new ArrayList<APIAddress>();
    }

    public void mute(APIRecord ar)
        throws APIException
    {
        if (ar.message == null) {
            if (vind.config.mustReason)
                throw new APIException("A reason must be provided.");

            ar.message = vind.config.defMuteReason;
        }

        if (!StrUtils.isMinecraftName(ar.target))
            throw new APIException("Invalid player: %s", hl(ar.target));

        ar.target = getTarget(ar.target);
        ar.flags  = 0;

        ar.addFlag(APIRecord.MUTE);
        ar.setTask(this, "muteTask");

        if (ar.timeout > 0)
            ar.timeout += Utils.time();

        execrun(ar);
    }

    private void muteTask(APIRecord ar)
        throws APIException
    {
        APIRecord mr;
        String    str;

        mr = null;

        for (APIRecord r : storage.getRecords(ar)) {
            if (!r.hasFlag(APIRecord.MUTE))
                continue;

            mr = r;
            break;
        }

        if (mr != null) {
            if (!vind.config.muteUpdate) {
                throw new APIException("Mute already exists on %s.",
                                       hl(ar.target));
            }

            str   = "updated";
            ar.id = mr.id;
            storage.update(ar);
        } else {
            str   = "placed";
            storage.add(ar);
        }

        mutes.put(ar.target, ar);
        vind.broadcast("vindicator.message.mute",
                       "Mute %s on %s by %s: %s",
                       str, hl(ar.target), hl(ar.issuer), hl(ar.message));

        if (ar.timeout < 1)
            return;

        vind.broadcast("vindicator.message.mute",
                       "Temporary mute will be removed: %s",
                       hl(Utils.timestr(Utils.DATEF_LONG, ar.timeout)));
    }

    public void noteAdd(APIRecord ar)
        throws APIException
    {
        ar.target  = getTarget(ar.target);
        ar.timeout = 0;
        ar.flags   = 0;

        ar.addFlag(APIRecord.NOTE);
        ar.addFlag(getTypeFlag(ar));
        ar.setTask(this, "noteAddTask");
        execrun(ar);
    }

    private void noteAddTask(APIRecord ar)
        throws APIException
    {
        storage.add(ar);
        vind.broadcast("vindicator.message.noteadd",
                       "Note added on %s by %s: %s",
                       hl(ar.target), hl(ar.issuer), hl(ar.message));
    }

    public void noteRem(APIRecord ar)
        throws APIException
    {
        ar.target = getTarget(ar.target);
        ar.setTask(this, "noteRemTask");
        execrun(ar);
    }

    private void noteRemTask(APIRecord ar)
        throws APIException
    {
        APIRecord nr;
        int       i;

        nr = null;
        i  = 1;

        for (APIRecord r : getAllRecords(ar.target)) {
            if (!r.hasFlag(APIRecord.NOTE))
                continue;

            if (i == ar.id) {
                nr = r;
                break;
            }

            i++;
        }

        if (nr == null)
            throw new APIException("Note index %s not found.", hl(ar.id));

        storage.remove(nr);
        vind.broadcast("vindicator.message.noterem",
                       "Note removed from %s by %s.",
                       hl(nr.target), hl(ar.issuer));
    }

    public void unban(APIRecord ar)
        throws APIException
    {
        ar.target = getTarget(ar.target);
        ar.setTask(this, "unbanTask");
        execrun(ar);
    }

    private void unbanTask(APIRecord ar)
        throws APIException
    {
        APIRecord br;

        br = null;

        for (APIRecord r : storage.getRecords(ar)) {
            if (!r.hasFlag(APIRecord.BAN))
                continue;

            br = r;
            break;
        }

        if (br == null)
            throw new APIException("Ban for %s not found.", hl(ar.target));

        storage.remove(br);
        vind.broadcast("vindicator.message.unban",
                       "Ban removed from %s by %s.",
                       hl(br.target), hl(ar.issuer));

        if (!vind.config.unbanNote)
            return;

        br.issuer  = ar.issuer;
        br.message = "Unbanned: " + br.message;

        ar.setObject(br);
        noteAdd(ar);
    }

    public void unmute(APIRecord ar)
        throws APIException
    {
        ar.target = getTarget(ar.target);
        ar.setTask(this, "unmuteTask");
        execrun(ar);
    }

    private void unmuteTask(APIRecord ar)
        throws APIException
    {
        APIRecord mr;
        String    msg;

        mr = null;

        for (APIRecord r : storage.getRecords(ar)) {
            if (!r.hasFlag(APIRecord.MUTE))
                continue;

            mr = r;
            break;
        }

        if (mr == null)
            throw new APIException("Mute for %s not found.", hl(ar.target));

        storage.remove(mr);
        mutes.remove(ar.target);
        vind.broadcast("vindicator.message.unmute",
                       "Mute removed from %s by %s.",
                       hl(ar.target), hl(ar.issuer));

        if (!vind.config.unmuteNote)
            return;

        mr.issuer  = ar.issuer;
        mr.message = "Unmuted: " + mr.message;

        ar.setObject(mr);
        noteAdd(ar);
    }

    public List<APIRecord> getAllRecords(String ... targets)
        throws APIException
    {
        ArrayList<APIRecord> ars;
        Player p;
        String addr;

        ars = new ArrayList<APIRecord>();

        for (int i = 0; i < targets.length; i++)
            targets[i] = getAddress(targets[i]);

        for (String t : targets) {
            if (!StrUtils.isMinecraftName(t)) {
                ars.addAll(storage.getRecords(t));
                continue;
            }

            p = vind.getServer().getPlayerExact(t);
            ars.addAll(storage.getRecords(t));

            if (p != null) {
                addr = p.getAddress().getAddress().getHostAddress();

                if (Arrays.binarySearch(targets, addr) < 0)
                    ars.addAll(storage.getRecords(addr));
            }
        }

        return ars;
    }

    private void execrun(APITask at)
        throws APIException
    {
        try {
            super.execute(at);
        } catch (RejectedExecutionException e) {
            throw new APIException("Failed to execute command. ",
                                   "Is the thread pool shutdown?");
        }
    }

    private String getAddress(String address)
    {
        String addr;

        addr = StrUtils.getAddress(address);
        return (addr != null) ? addr : address;
    }

    private String getTarget(String target)
    {
        String addr;
        Player p;

        addr = StrUtils.getAddress(target);

        if (addr != null)
            return addr;

        if (!vind.config.autoComplete)
            return target;

        p = vind.getServer().getPlayer(target);

        if (p != null)
            return p.getName();

        return target;
    }

    private int getTypeFlag(APIRecord ar)
        throws APIException
    {
        if (StrUtils.isMinecraftName(ar.target))
            return APIRecord.PLAYER;

        if (StrUtils.isAddress(ar.target))
            return APIRecord.ADDRESS;

        throw new APIException("Invalid player/address: %s.", hl(ar.target));
    }

    private boolean kick(APIRecord ar, String message)
    {
        Player p;

        p = vind.getServer().getPlayerExact(ar.target);

        if (p == null)
            return false;

        message = Message.format(message);
        p.kickPlayer(message);
        return true;
    }

    private boolean kickIP(APIRecord ar, String message)
    {
        String ip;
        int    i;

        message = Message.format(message);
        i       = 0;

        for (Player p : vind.getServer().getOnlinePlayers()) {
            ip = p.getAddress().getAddress().getHostAddress();

            if (!ip.equals(ar.target))
                continue;

            p.kickPlayer(message);
            i++;
        }

        return (i > 0);
    }
}
