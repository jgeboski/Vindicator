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

package org.jgeboski.vindicator.api;

public class TargetObject
{
    public static final int PRIVATE = 1 << 0;

    public static final int IP      = 1 << 1;
    public static final int PLAYER  = 1 << 2;

    public static final int BAN     = 1 << 3;
    public static final int NOTE    = 1 << 4;

    private int    id;
    private String target;
    private String issuer;
    private String message;
    private long   timeout;
    private long   time;
    private int    flags;

    public TargetObject(String target, String issuer, String message,
                        long timeout)
    {
        this.id      = 0;
        this.target  = target;
        this.issuer  = issuer;
        this.message = message;
        this.timeout = timeout;
        this.time    = System.currentTimeMillis();
        this.flags   = 0;
    }

    public TargetObject(String target, String issuer, String message)
    {
        this(target, issuer, message, 0);
    }

    public TargetObject()
    {
        this(null, null, null, 0);
    }

    public void addFlag(int flag)
    {
        flags |= flag;
    }

    public boolean hasFlag(int flag)
    {
        return ((flags & flag) != 0);
    }

    public int getId()
    {
        return id;
    }

    public String getTarget()
    {
        return target;
    }

    public String getIssuer()
    {
        return issuer;
    }

    public String getMessage()
    {
        return message;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public long getTime()
    {
        return time;
    }

    public int getFlags()
    {
        return flags;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public void setIssuer(String issuer)
    {
        this.issuer = issuer;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public void setTime(long time)
    {
        this.time = time;
    }

    public void setFlags(int flags)
    {
        this.flags = flags;
    }
}
