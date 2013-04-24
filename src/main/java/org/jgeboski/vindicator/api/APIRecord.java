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

package org.jgeboski.vindicator.api;

import java.lang.reflect.Field;
import org.bukkit.command.CommandSender;
import org.jgeboski.vindicator.util.Utils;

public class APIRecord extends APITask<APIRecord>
{
    public static final int ADDRESS = 1 << 1;
    public static final int PLAYER  = 1 << 2;

    public static final int BAN     = 1 << 3;
    public static final int MUTE    = 1 << 4;
    public static final int NOTE    = 1 << 5;

    public CommandSender sender;

    public int    id;
    public String target;
    public String issuer;
    public String message;
    public long   timeout;
    public long   time;
    public int    flags;

    public APIRecord(APIRunnable arun, String target, String issuer)
    {
        super(APIRecord.class, arun);
        init(null, target, issuer);
    }

    public APIRecord(APIRunnable arun, CommandSender sender, String target)
    {
        super(APIRecord.class, arun);
        init(sender, target, sender.getName());
    }

    public APIRecord()
    {
        super(APIRecord.class, null);
        init(null, null, null);
    }

    private void init(CommandSender sender, String target, String issuer)
    {
        this.sender  = sender;

        this.id      = 0;
        this.target  = target;
        this.issuer  = issuer;
        this.message = null;
        this.timeout = 0;
        this.time    = Utils.time();
        this.flags   = 0;
    }

    public void addFlag(int flag)
    {
        flags |= flag;
    }

    public boolean hasFlag(int flag)
    {
        return ((flags & flag) != 0);
    }

    public void setObject(APIRecord ar)
    {
        Object v;

        for (Field f : ar.getClass().getDeclaredFields()) {
            try {
                v = f.get(ar);
                f.set(this, v);
            } catch (Exception e) { }
        }
    }
}
