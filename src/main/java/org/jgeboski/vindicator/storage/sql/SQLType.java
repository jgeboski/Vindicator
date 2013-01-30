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

package org.jgeboski.vindicator.storage.sql;

public enum SQLType
{
    MYSQL,
    SQLITE;

    public static SQLType fromURL(String url)
    {
        String strs[];
        String str;

        str  = url.toLowerCase();
        strs = str.split(":", 3);

        if ((strs.length < 2) || !strs[0].equals("jdbc"))
            return null;

        for (SQLType t : SQLType.values()) {
            str = t.toString();

            if (str.equalsIgnoreCase(strs[1]))
                return t;
        }

        return null;
    }
}
