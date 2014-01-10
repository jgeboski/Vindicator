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

package org.jgeboski.vindicator.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Kick implements Runnable
{
    public List<Player> players;
    public String message;

    protected Kick(Plugin plugin, List players, String message)
    {
        this.players = players;
        this.message = message;

        plugin.getServer().getScheduler()
                          .scheduleSyncDelayedTask(plugin, this, 10);
    }

    public static boolean target(Plugin plugin, String target, String message,
                                 String ... args)
    {
        if (StrUtils.isAddress(target))
            return Kick.address(plugin, target, message, args);
        if (StrUtils.isUUID(target))
            return Kick.uuid(plugin, target, message, args);
        else if (StrUtils.isMinecraftName(target))
            return Kick.player(plugin, target, message, args);

        return false;
    }

    public static boolean address(Plugin plugin, String address, String message,
                                  String ... args)
    {
        ArrayList<Player> players;
        String addr;
        int    i;

        players = new ArrayList<Player>();
        message = String.format(message, args);
        i       = 0;

        for(Player p : plugin.getServer().getOnlinePlayers()) {
            addr = p.getAddress().getAddress().getHostAddress();
            addr = StrUtils.getAddress(addr);

            if (addr == null)
                addr = address;

            if(!addr.equals(address))
                continue;

            players.add(p);
            i++;
        }

        new Kick(plugin, players, message);
        return (i > 0);
    }

    public static boolean player(Plugin plugin, Player player, String message,
                                 String ... args)
    {
        ArrayList<Player> players;

        players = new ArrayList<Player>();
        message = String.format(message, args);

        players.add(player);
        new Kick(plugin, players, message);
        return true;
    }

    public static boolean player(Plugin plugin, String player, String message,
                                 String ... args)
    {
        Player p;

        p = plugin.getServer().getPlayerExact(player);

        if (p == null)
            return false;

        return player(plugin, p, message, args);
    }

    public static boolean uuid(Plugin plugin, String uuid, String message,
                               String ... args)
    {
        for(Player p : plugin.getServer().getOnlinePlayers()) {
            if (uuid.equalsIgnoreCase(p.getUniqueId().toString()))
                return player(plugin, p, message, args);
        }

        return false;
    }

    public void run()
    {
        for (Player p : players)
            p.kickPlayer(message);
    }
}
