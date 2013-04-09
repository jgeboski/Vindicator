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

import java.util.logging.Logger;
import org.bukkit.ChatColor;

import org.jgeboski.vindicator.Vindicator;

public class Log
{
    protected static final Logger log = Logger.getLogger("Minecraft");

    public static void info(String format, Object ... args)
    {
        log.info(format(format, args));
    }

    public static void warning(String format, Object ... args)
    {
        log.warning(format(format, args));
    }

    public static void severe(String format, Object ... args)
    {
        log.severe(format(format, args));
    }

    private static String format(String msg, Object ... args)
    {
        msg = ChatColor.stripColor(String.format(msg, args));
        msg = String.format("[%s] %s", Vindicator.pluginName, msg);
        return msg;
    }
}
