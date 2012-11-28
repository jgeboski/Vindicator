/*
 * Copyright 2012 James Geboski <jgeboski@gmail.com>
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

package org.jgeboski.vindicator;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jgeboski.vindicator.exception.APIException;
import org.jgeboski.vindicator.exception.StorageException;
import org.jgeboski.vindicator.runnable.*;
import org.jgeboski.vindicator.storage.Storage;
import org.jgeboski.vindicator.storage.StorageSQL;
import org.jgeboski.vindicator.util.IPUtils;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.Vindicator;

public class VindicatorAPI extends ThreadPoolExecutor
{
    public Vindicator vind;
    public Storage    storage;

    public VindicatorAPI(Vindicator vind)
        throws APIException
    {
        super(vind.config.poolMinSize, vind.config.poolMaxSize,
              vind.config.poolKeepAlive, TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<Runnable>(),
              Executors.defaultThreadFactory());

        this.vind = vind;

        /* For now, SQL only */
        storage = new StorageSQL(
            vind.config.storeURL,  vind.config.storeUser,
            vind.config.storePass, vind.config.storePrefix);
    }

    public void close()
    {
        shutdownNow();
        storage.close();
    }

    public void ban(CommandSender sender, String target, String message,
                    long timeout)
        throws APIException
    {
        RBan run;
        int  type;

        target = getTarget(target);
        type   = getTypeFlag(sender, target, RObject.PLAYER, RObject.IP);

        if(type < 0)
            return;

        run = new RBan(this, sender, target, message);
        run.addFlag(type);

        if(timeout > 0)
            run.setTimeout(Utils.time() + timeout);

        execrun(run);

        if(run.hasFlag(RObject.IP))
            kickIP(target, "Banned: " + message);
        else
            kick(target, "Banned: " + message);
    }

    public void ban(CommandSender sender, String target, String reason)
        throws APIException
    {
        ban(sender, target, reason, 0);
    }

    public void kick(CommandSender sender, String target, String reason)
        throws APIException
    {
        if(IPUtils.isAddress(target)) {
            if(kickIP(target, reason))
                return;

            throw new APIException("Player(s) for %s not found", target);
        }

        target = getTarget(target);

        if(!kick(target, reason))
            throw new APIException("Player %s not found", target);

        vind.broadcast("vindicator.message.kick",
                       "Kick placed for %s by %s: %s",
                       target, sender.getName(), reason);
    }

    public void lookup(CommandSender sender, String target)
        throws APIException
    {
        target = getTarget(target);
        execrun(new RLookup(this, sender, target));
    }

    public void mute(CommandSender sender, String target, String message,
                     long timeout)
        throws APIException
    {
        RMute run;

        if(!Utils.isMinecraftName(target))
            throw new APIException("Invalid player: %s", target);

        target = getTarget(target);
        run    = new RMute(this, sender, target, message);

        if(timeout > 0)
            run.setTimeout(Utils.time() + timeout);

        execrun(run);
    }

    public void noteAdd(CommandSender sender, String target, String message,
                        boolean pub)
        throws APIException
    {
        RNoteAdd run;
        int  type;

        target = getTarget(target);
        type   = getTypeFlag(sender, target, RObject.PLAYER, RObject.IP);

        if(type < 0)
            return;

        run = new RNoteAdd(this, sender, target, message);
        run.addFlag(type);

        if(pub)
            run.addFlag(RObject.PUBLIC);

        execrun(run);
    }

    public void noteRem(CommandSender sender, String target, int index)
        throws APIException
    {
        target = getTarget(target);
        execrun(new RNoteRem(this, sender, target, index));
    }

    public void noteRem(CommandSender sender, String target, String index)
        throws APIException
    {
        int i;

        try {
            i = Integer.parseInt(index);
        } catch(NumberFormatException e) {
            throw new APIException("Invalid note index: %s", index);
        }

        noteRem(sender, target, i);
    }

    public void unban(CommandSender sender, String target)
        throws APIException
    {
        target = getTarget(target);
        execute(new RUnban(this, sender, target));
    }

    public void unmute(CommandSender sender, String target)
        throws APIException
    {
        target = getTarget(target);
        execute(new RUnmute(this, sender, target));
    }

    private void execrun(Runnable command)
        throws APIException
    {
        try {
            super.execute(command);
        } catch(RejectedExecutionException e) {
            throw new APIException("Failed to execute command. ",
                                   "Is the thread pool shutdown?");
        }
    }

    private String getTarget(String target)
    {
        Player p;

        if(!vind.config.autoComplete)
            return target;

        p = vind.getServer().getPlayer(target);

        if(p != null)
            return p.getName();

        return target;
    }

    private int getTypeFlag(CommandSender sender, String target, int ifname,
                            int ifaddress)
        throws APIException
    {
        if(Utils.isMinecraftName(target))
            return ifname;

        if(IPUtils.isAddress(target))
            return ifaddress;

        throw new APIException("Invalid player/IP: %s", target);
    }

    private boolean kick(String target, String message)
    {
        Player p;

        p = vind.getServer().getPlayerExact(target);

        if(p == null)
            return false;

        p.kickPlayer(message);
        return true;
    }

    private boolean kickIP(String target, String message)
    {
        String ip;
        int    i;

        i = 0;

        for(Player p : vind.getServer().getOnlinePlayers()) {
            ip = p.getAddress().getAddress().getHostAddress();

            if(ip.equals(target))
                continue;

            p.kickPlayer(message);
            i++;
        }

        return (i > 0);
    }
}
