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
import java.util.List;
import java.util.HashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jgeboski.vindicator.storage.Storage;
import org.jgeboski.vindicator.storage.StorageSQL;
import org.jgeboski.vindicator.storage.TargetObject;
import org.jgeboski.vindicator.util.StrUtils;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.Vindicator;

public class VindicatorAPI extends ThreadPoolExecutor
{
    public Vindicator vind;
    public Storage    storage;

    public HashMap<String, TargetObject> mutes;

    public VindicatorAPI(Vindicator vind)
        throws APIException
    {
        super(vind.config.poolMinSize, vind.config.poolMaxSize,
              vind.config.poolKeepAlive, TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<Runnable>(),
              Executors.defaultThreadFactory());

        this.vind  = vind;
        this.mutes = new HashMap<String, TargetObject>();

        /* For now, SQL only */
        storage = new StorageSQL(
            vind.config.storeURL,  vind.config.storeUser,
            vind.config.storePass, vind.config.storePrefix);

        for (Player p : vind.getServer().getOnlinePlayers()) {
            for (TargetObject to : storage.getRecords(p.getName())) {
                if (to.hasFlag(TargetObject.MUTE))
                    mutes.put(p.getName(), to);
            }
        }
    }

    public void close()
    {
        shutdown();
        storage.close();
    }

    public void ban(APITask at)
        throws APIException
    {
        if (at.message == null) {
            if (vind.config.mustReason)
                throw new APIException("A reason must be provided");

            at.message = vind.config.defBanReason;
        }

        at.target = getTarget(at);
        at.addFlag(TargetObject.BAN);
        at.addFlag(getTypeFlag(at));
        at.setHandler(this, "banHandler");

        if (at.timeout > 0)
            at.timeout += Utils.time();

        if (at.hasFlag(TargetObject.ADDRESS))
            kickIP(at, "Banned: " + at.message);
        else
            kick(at, "Banned: " + at.message);

        execrun(at);
    }

    public void banHandler(APITask at)
        throws APIException
    {
        TargetObject ban;
        String       act;

        ban = null;

        for (TargetObject to : storage.getRecords(at)) {
            if (!to.hasFlag(TargetObject.BAN))
                continue;

            ban = to;
            break;
        }

        if (ban != null) {
            if (!vind.config.banUpdate)
                throw new APIException("Ban already exists for %s", at.target);

            act   = "updated";
            at.id = ban.id;
            storage.update(at);
        } else {
            act   = "placed";
            storage.add(at);
        }

        vind.broadcast("vindicator.message.ban",
                       "Ban %s for %s by %s: %s",
                       act, at.target, at.issuer, at.message);

        if (at.timeout < 1)
            return;

        vind.broadcast("vindicator.message.ban",
                       "Temporary ban will be removed: %s",
                       Utils.timestr("EEE, MMM d 'at' h:m a z", at.timeout));
    }

    public void kick(APITask at)
        throws APIException
    {
        if (at.message == null) {
            if (vind.config.mustReason)
                throw new APIException("A reason must be provided");

            at.message = vind.config.defKickReason;
        }

        if (StrUtils.isAddress(at.target)) {
            if (kickIP(at, at.message))
                return;

            throw new APIException("Player(s) for %s not found", at.target);
        }

        at.target = getTarget(at);

        if (!kick(at, at.message))
            throw new APIException("Player %s not found", at.target);

        vind.broadcast("vindicator.message.kick",
                       "Kick placed for %s by %s: %s",
                       at.target, at.issuer, at.message);
    }

    public void lookup(APITask at)
        throws APIException
    {
        at.target = getTarget(at);
        at.setHandler(this, "lookupHandler");
        execrun(at);
    }

    public List<TargetObject> lookupHandler(APITask at)
        throws APIException
    {
        ArrayList<TargetObject> tos;
        String type;

        int m;
        int b;
        int n;

        tos = new ArrayList<TargetObject>();
        m   = b = n = 0;

        for (TargetObject to : storage.getRecords(at.target)) {
            if (to.hasFlag(TargetObject.BAN)) {
                tos.add(b, to);
                b++;
            } else if (to.hasFlag(TargetObject.NOTE)) {
                to.id = n + 1;
                tos.add(b + m + n, to);
                n++;
            } else if (to.hasFlag(TargetObject.MUTE)) {
                tos.add(b + m, to);
                m++;
            }
        }

        return tos;
    }

    public void mute(APITask at)
        throws APIException
    {
        if (at.message == null) {
            if (vind.config.mustReason)
                throw new APIException("A reason must be provided");

            at.message = vind.config.defMuteReason;
        }

        if (!StrUtils.isMinecraftName(at.target))
            throw new APIException("Invalid player: %s", at.target);

        at.target = getTarget(at);
        at.addFlag(TargetObject.MUTE);
        at.setHandler(this, "muteHandler");

        if (at.timeout > 0)
            at.timeout += Utils.time();

        execrun(at);
    }

    public void muteHandler(APITask at)
        throws APIException
    {
        TargetObject mute;
        String       act;

        mute = null;

        for (TargetObject to : storage.getRecords(at)) {
            if (!to.hasFlag(TargetObject.MUTE))
                continue;

            mute = to;
            break;
        }

        if (mute != null) {
            if (!vind.config.muteUpdate)
                throw new APIException("Mute already exists for %s", at.target);

            act   = "updated";
            at.id = mute.id;
            storage.update(at);
        } else {
            act   = "placed";
            storage.add(at);
        }

        mutes.put(at.target, at);
        vind.broadcast("vindicator.message.mute",
                       "Mute %s for %s by %s: %s",
                       act, at.target, at.issuer, at.message);

        if (at.timeout < 1)
            return;

        vind.broadcast("vindicator.message.mute",
                       "Temporary mute will be removed: %s",
                       Utils.timestr("EEE, MMM d 'at' h:m a z", at.timeout));
    }

    public void noteAdd(APITask at)
        throws APIException
    {
        at.target = getTarget(at);
        at.addFlag(TargetObject.NOTE);
        at.addFlag(getTypeFlag(at));
        at.setHandler(this, "noteAddHandler");
        execrun(at);
    }

    public void noteAddHandler(APITask at)
        throws APIException
    {
        String perm;

        perm = "vindicator.message.noteadd";

        if (at.hasFlag(TargetObject.PUBLIC))
            perm += ".public";

        storage.add(at);
        vind.broadcast(perm, "Note added for %s by %s: %s",
                       at.target, at.issuer, at.message);
    }

    public void noteRem(APITask at)
        throws APIException
    {
        at.target = getTarget(at);
        at.setHandler(this, "noteRemHandler");
        execrun(at);
    }

    public void noteRemHandler(APITask at)
        throws APIException
    {
        TargetObject note;
        String       perm;
        int          i;

        note = null;
        i    = 1;

        for (TargetObject to : storage.getRecords(at)) {
            if (!to.hasFlag(TargetObject.NOTE))
                continue;

            if (i == at.id) {
                note = to;
                break;
            }

            i++;
        }

        if (note == null)
            throw new APIException("Note index %d not found", at.id);

        perm = "vindicator.message.noterem";

        if (note.hasFlag(TargetObject.PUBLIC))
            perm += ".public";

        storage.remove(note);
        vind.broadcast(perm, "Note removed for %s by %s: %s",
                       note.target, at.issuer, note.message);
    }

    public void unban(APITask at)
        throws APIException
    {
        at.target = getTarget(at);
        at.setHandler(this, "unbanHandler");
        execrun(at);
    }

    public void unbanHandler(APITask at)
        throws APIException
    {
        TargetObject bt;

        bt = null;

        for (TargetObject to : storage.getRecords(at)) {
            if (!to.hasFlag(TargetObject.BAN))
                continue;

            bt = to;
            break;
        }

        if (bt == null)
            throw new APIException("Ban for %s not found", at.target);

        storage.remove(bt);
        vind.broadcast("vindicator.message.unban",
                       "Ban removed for %s by %s",
                       bt.target, at.issuer);

        if (!vind.config.unbanNote)
            return;

        at.setTargetObject(bt);
        at.message = "Unbanned: " + at.message;

        noteAdd(at);
    }

    public void unmute(APITask at)
        throws APIException
    {
        at.target = getTarget(at);
        at.setHandler(this, "unmuteHandler");
        execrun(at);
    }

    public void unmuteHandler(APITask at)
        throws APIException
    {
        TargetObject mt;
        String       msg;

        mt = null;

        for (TargetObject to : storage.getRecords(at)) {
            if (!to.hasFlag(TargetObject.MUTE))
                continue;

            mt = to;
            break;
        }

        if (mt == null)
            throw new APIException("Mute for %s not found", at.target);

        if (at.issuer == null) {
            msg = String.format("Mute removed for %s", mt.target);
        } else {
            msg = String.format("Mute removed for %s by %s",
                                mt.target, at.issuer);
        }

        storage.remove(mt);
        mutes.remove(at.target);
        vind.broadcast("vindicator.message.unmute", msg);

        if (!vind.config.unmuteNote)
            return;

        at.setTargetObject(mt);
        at.message = "Unmuted: " + at.message;

        noteAdd(at);
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

    private String getTarget(TargetObject to)
    {
        Player p;

        if (!vind.config.autoComplete)
            return to.target;

        p = vind.getServer().getPlayer(to.target);

        if (p != null)
            return p.getName();

        return to.target;
    }

    private int getTypeFlag(TargetObject to)
        throws APIException
    {
        if (StrUtils.isMinecraftName(to.target))
            return TargetObject.PLAYER;

        if (StrUtils.isAddress(to.target))
            return TargetObject.ADDRESS;

        throw new APIException("Invalid player/IP: %s", to.target);
    }

    private boolean kick(TargetObject to, String message)
    {
        Player p;

        p = vind.getServer().getPlayerExact(to.target);

        if (p == null)
            return false;

        p.kickPlayer(message);
        return true;
    }

    private boolean kickIP(TargetObject at, String message)
    {
        String ip;
        int    i;

        i = 0;

        for (Player p : vind.getServer().getOnlinePlayers()) {
            ip = p.getAddress().getAddress().getHostAddress();

            if (ip.equals(at.target))
                continue;

            p.kickPlayer(message);
            i++;
        }

        return (i > 0);
    }
}
