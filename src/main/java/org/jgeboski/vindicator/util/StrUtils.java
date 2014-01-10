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

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public class StrUtils
{
    public static String getAddress(String str)
    {
        InetAddress ia;

        if (str == null)
            return null;

        ia = getInetAddress(str);
        return (ia != null) ? ia.getHostAddress() : null;
    }

    public static boolean isAddress(String str)
    {
        if (str == null)
            return false;

        return (getInetAddress(str) != null);
    }

    public static boolean isAddress4(String str)
    {
        if (str == null)
            return false;

        return (getInetAddress(str) instanceof Inet4Address);
    }

    public static boolean isAddress6(String str)
    {
        if (str == null)
            return false;

        return (getInetAddress(str) instanceof Inet6Address);
    }

    public static boolean isMinecraftName(String str)
    {
        if (str == null)
            return false;

        return str.matches("\\w{2,16}");
    }

    public static boolean isUUID(String str)
    {
        if (str == null)
            return false;

        return str.matches("[\\w]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12}");
    }

    public static String join(String[] strs, String glue, int start, int end)
    {
        String ret;
        int    i;

        if (start >= strs.length)
            return null;

        if (end > strs.length)
            end = strs.length;

        ret = strs[start];

        for (i = start + 1; i < end; i++)
            ret += glue + strs[i];

        return ret;
    }

    public static String join(String[] strs, String glue, int start)
    {
        return join(strs, glue, start, strs.length);
    }

    public static String join(String[] strs, String glue)
    {
        return join(strs, glue, 0, strs.length);
    }

    public static String join(List<String> strs, String glue, int start,
                              int end)
    {
        String ret;
        int    size;
        int    i;

        size = strs.size();

        if (start >= size)
            return null;

        if (end > size)
            end = size;

        ret = strs.get(start);

        for (i = start + 1; i < end; i++)
            ret += glue + strs.get(i);

        return ret;
    }

    public static String join(List<String> strs, String glue, int start)
    {
        return join(strs, glue, start, strs.size());
    }

    public static String join(List<String> strs, String glue)
    {
        return join(strs, glue, 0, strs.size());
    }

    public static long toSeconds(String str)
    {
        String sstr;
        char[] cstr;
        long   time;
        long   mod;
        int    i;
        int    s;
        int    e;

        cstr = str.toLowerCase().toCharArray();
        time = 0;

        for (i = s = e = 0; i < cstr.length; i++) {
            if (Character.isDigit(cstr[i])) {
                if (s == 0)
                    s = i;

                e = i;
                continue;
            }

            e++;

            if ((i != e) || (e > cstr.length))
                continue;

            switch (cstr[e]) {
            case 'm':
                if (((e + 1) < cstr.length) && (cstr[e + 1] == 'o'))
                    mod = 2592000;
                else
                    mod = 60;
                break;

            case 's': mod = 1;        break;
            case 'h': mod = 3600;     break;
            case 'd': mod = 86400;    break;
            case 'w': mod = 604800;   break;
            case 'y': mod = 31536000; break;

            default:
                return 0;
            }

            sstr = new String(Arrays.copyOfRange(cstr, s, e));
            s    = e = 0;

            try {
                time += Integer.parseInt(sstr) * mod;
            } catch (NumberFormatException ex) {
                return 0;
            }
        }

        return time;
    }

    private static InetAddress getInetAddress(String str)
    {
        byte addr[];

        addr = getNumericAddress4(str);

        if (addr == null)
            addr = getNumericAddress6(str);

        if (addr == null)
            return null;

        try {
            return InetAddress.getByAddress(new String(), addr);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private static byte[] getNumericAddress4(String str)
    {
        String ss[];
        byte   bs[];
        int    i;
        int    v;

        ss = str.split("\\.");

        if (ss.length != 4)
            return null;

        bs = new byte[4];

        try {
            for (i = 0; i < bs.length; i++) {
                v = Integer.parseInt(ss[i]);

                if ((v < 0) || (v > 255))
                    return null;

                bs[i] = (byte) v;
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return bs;
    }

    private static byte[] getNumericAddress6(String str)
    {
        char cs[];
        byte bs[];
        int  i;
        int  c;
        int  o;
        int  val;
        int  vch;

        boolean cold;

        cs   = str.toCharArray();
        bs   = new byte[16];
        cold = false;
        val  = i = c = 0;

        if ((cs[0] == ':') && (cs[1] != ':'))
            return null;

        while ((i < cs.length) && (c < bs.length)) {
            if (cs[i] == ':') {
                cold = true;

                if (++i >= cs.length)
                    return null;

                if (cs[i] != ':')
                    continue;

                if (++i >= cs.length)
                    return bs;

                o  = i - 1;
                c += bs.length - (c + 2);

                while ((o = Arrays.binarySearch(cs, ++o, cs.length, ':')) >= 0)
                    c += 2;

                if (c >= bs.length)
                    return null;
            }

            while (i < cs.length) {
                vch = Character.digit(cs[i], 16);

                if (vch < 0) {
                    if (cs[i] == ':')
                        break;

                    return null;
                }

                val <<= 4;
                val  |= (byte) vch;
                i++;
            }

            if (val > 0xFFFF)
                return null;

            bs[c++] = (byte) ((val >> 8) & 0xFF);
            bs[c++] = (byte) (val & 0xFF);
            val = 0;
        }

        return cold ? bs : null;
    }
}
