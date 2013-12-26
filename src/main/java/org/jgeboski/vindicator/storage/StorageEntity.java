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

import org.jgeboski.vindicator.util.StrUtils;

import static org.jgeboski.vindicator.util.Message.hl;

public abstract class StorageEntity
{
    public String ident;
    public String alias;

    protected StorageEntity(String ident, String alias)
    {
        this.ident = ident;
        this.alias = alias;
    }

    public abstract void validate(boolean complete)
        throws StorageException;

    public static StorageEntity fromString(String str)
        throws StorageException
    {
        if (StrUtils.isMinecraftName(str))
            return new StoragePlayer(str);

        if (StrUtils.isAddress(str))
            return new StorageAddress(str);

        throw new StorageException("Invalid player/address: %s", hl(str));
    }

    public void invalid()
        throws StorageException
    {
        String type;

        type = getClass().getName();
        throw new StorageException("Invalid StorageEntity: %s", hl(type));
    }
}
