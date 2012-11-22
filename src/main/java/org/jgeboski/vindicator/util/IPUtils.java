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

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

public class IPUtils
{
    public static boolean isAddress(String str)
    {
        return (getAddress(str) != null);
    }

    public static boolean isIPv4(String str)
    {
        InetAddress ia;

        ia = getAddress(str);

        if(ia == null)
            return false;

        return (ia instanceof Inet4Address);
    }

    public static boolean isIPv6(String str)
    {
        InetAddress ia;

        ia = getAddress(str);

        if(ia == null)
            return false;

        return (ia instanceof Inet6Address);
    }

    private static InetAddress getAddress(String str)
    {
        InetAddress ia;

        try {
            ia = InetAddress.getByName(str);
        } catch(UnknownHostException e) {
            return null;
        }

        return str.equals(ia.getHostAddress()) ? ia : null;
    }
}
