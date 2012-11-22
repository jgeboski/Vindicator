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

package org.jgeboski.vindicator.util;

public class Utils
{
    public static String strjoin(String[] strs, String glue, int start, int end)
    {
        String ret;
        int    i;

        if(start > strs.length)
            return new String();

        if(end > strs.length)
            end = strs.length;

        ret = strs[start];

        for(i = (start + 1); i < strs.length; i++)
            ret.concat(glue + strs[i]);

        return ret;
    }

    public static String strjoin(String[] strs, String glue, int start)
    {
        return strjoin(strs, glue, start, strs.length);
    }

    public static boolean isMinecraftName(String str)
    {
        return str.matches("\\w{2,16}");
    }
}
