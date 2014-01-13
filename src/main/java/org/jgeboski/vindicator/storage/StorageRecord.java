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

package org.jgeboski.vindicator.storage;

import org.jgeboski.vindicator.util.Utils;

public class StorageRecord
{
    public static final int ADDRESS = 1 << 1;
    public static final int PLAYER  = 1 << 2;

    public static final int BAN     = 1 << 3;
    public static final int MUTE    = 1 << 4;
    public static final int NOTE    = 1 << 5;

    public StorageEntity target;
    public StoragePlayer issuer;
    public int           id;
    public String        message;
    public long          timeout;
    public long          time;
    public int           flags;

    public StorageRecord(StorageEntity target, StoragePlayer issuer)
    {
        this.target  = target;
        this.issuer  = issuer;
        this.id      = 0;
        this.message = null;
        this.timeout = 0;
        this.time    = Utils.time();
        this.flags   = 0;
    }

    public StorageRecord(String target, String issuer)
        throws StorageException
    {
        this(StorageEntity.fromString(target), new StoragePlayer(issuer));
    }

    public StorageRecord(StoragePlayer target)
    {
        this(target, null);
    }

    public StorageRecord(String target)
        throws StorageException
    {
        this(StorageEntity.fromString(target), null);
    }

    public void addFlag(int flag)
    {
        flags |= flag;
    }

    public boolean hasFlag(int flag)
    {
        return ((flags & flag) != 0);
    }

    public void remFlag(int flag)
    {
        flags &= ~flag;
    }

    public void validate(Storage storage, int type, boolean complete)
        throws StorageException
    {
        int valid;

        if (issuer != null)
            issuer.validate(storage, complete);

        if (target == null)
            return;

        valid = 0;

        if (target instanceof StorageAddress) {
            valid |= ADDRESS;
            ((StorageAddress) target).validate(storage, complete);
        } else if (target instanceof StoragePlayer) {
            valid |= PLAYER;
            ((StoragePlayer) target).validate(storage, complete);
        } else {
            target.invalid();
        }

        flags = valid | type;

        if (hasFlag(NOTE))
            timeout = 0;
    }

    public void validate(Storage storage, boolean complete)
        throws StorageException
    {
        int type;

        type = flags & ~(ADDRESS | PLAYER);
        validate(storage, type, complete);
    }

    public String toString()
    {
        String ret;

        ret = String.format("{id: %d, target: %s, issuer: %s, message: '%s', " +
                            "timeout: %d, time: %d, flags: %d}",
                            id, target, issuer, message, timeout, time, flags);

        return ret;
    }
}
