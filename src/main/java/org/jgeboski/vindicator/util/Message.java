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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jgeboski.vindicator.Vindicator;

public class Message
{
    public static String format(String format, Object ... args)
    {
        String str;
        String rc;
        String rcr;

        str = String.format(format, args);

        if (str.charAt(0) != ChatColor.COLOR_CHAR)
            str = ChatColor.GOLD + str;

        rc  = ChatColor.RESET.toString();
        rcr = rc + str.substring(0, 2);
        str = str.replaceAll(rc, rcr);

        return str;
    }

    public static void info(CommandSender sender, String format,
                            Object ... args)
    {
        send(sender, format, args);
    }

    public static void warning(CommandSender sender, String format,
                               Object ... args)
    {
        if (sender instanceof Player)
            format = ChatColor.YELLOW + format;

        send(sender, format, args);
    }

    public static void severe(CommandSender sender, String format,
                              Object ... args)
    {
        if (sender instanceof Player)
            format = ChatColor.RED + format;

        send(sender, format, args);
    }

    public static void send(CommandSender sender, String format,
                            Object ... args)
    {
        String str;

        str = format(format, args);

        if (sender instanceof Player) {
            str = String.format("%s[%s]%s %s", ChatColor.DARK_AQUA,
                                Vindicator.pluginName, ChatColor.RESET, str);
        } else {
            str = ChatColor.stripColor(str);
            str = String.format("[%s] %s", Vindicator.pluginName, str);
        }

        sender.sendMessage(str);
    }
}
