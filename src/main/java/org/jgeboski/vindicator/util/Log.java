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

public class Log
{
    public static Logger logger;

    public static void init(Logger logger)
    {
        Log.logger = logger;
    }

    public static void info(String format, Object ... args)
    {
        logger.info(format(format, args));
    }

    public static void warning(String format, Object ... args)
    {
        logger.warning(format(format, args));
    }

    public static void severe(String format, Object ... args)
    {
        logger.severe(format(format, args));
    }

    private static String format(String msg, Object ... args)
    {
        return ChatColor.stripColor(String.format(msg, args));
    }
}
