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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Utils
{
    public static final String DATEF_LONG  = "EEE, MMM d 'at' h:m a z";
    public static final String DATEF_SHORT = "MM-dd kk:mm";

    public static void broadcast(String perm, String format, Object ... args)
    {
        String msg;

        msg = String.format(format, args);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(perm))
                Message.info(p, msg);
        }

        Log.info(format, args);
    }

    public static boolean hasPermission(CommandSender sender, String perm,
                                        boolean errmsg)
    {
        if (sender.hasPermission(perm))
            return true;

        if (errmsg)
            Message.severe(sender, "You don't have permission for that.");

        return false;
    }

    public static boolean hasPermission(CommandSender sender, String perm)
    {
        return hasPermission(sender, perm, true);
    }

    public static long time()
    {
        return System.currentTimeMillis() / 1000;
    }

    public static String timestr(String format, long secs)
    {
        SimpleDateFormat sdf;
        Date             date;

        sdf  = new SimpleDateFormat(format);
        date = new Date(secs * 1000);

        return sdf.format(date);
    }
}
