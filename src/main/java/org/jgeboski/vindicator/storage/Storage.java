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

import java.util.ArrayList;
import java.util.List;

public abstract class Storage
{
    public abstract void close();

    public abstract void add(StorageLogin login)
        throws StorageException;

    public abstract void add(StorageRecord recd)
        throws StorageException;

    public abstract void addLogins(List<StorageLogin> logins)
        throws StorageException;

    public abstract void addRecords(List<StorageRecord> recds)
        throws StorageException;

    public abstract void clearLogins()
        throws StorageException;

    public abstract void clearRecords()
        throws StorageException;

    public abstract StorageLogin getLogin(StorageLogin login)
        throws StorageException;

    public abstract StorageLogin getLoginLast(StoragePlayer plyr)
        throws StorageException;

    public abstract List<StorageLogin> getLogins()
        throws StorageException;

    public abstract List<StorageLogin> getLogins(StorageAddress addr)
        throws StorageException;

    public abstract List<StorageLogin> getLogins(StoragePlayer plyr)
        throws StorageException;

    public abstract List<StorageRecord> getRecords()
        throws StorageException;

    public abstract List<StorageRecord> getRecords(StorageAddress addr)
        throws StorageException;

    public abstract List<StorageRecord> getRecords(StoragePlayer plyr)
        throws StorageException;

    public abstract void remove(StorageLogin login)
        throws StorageException;

    public abstract void remove(StorageRecord recd)
        throws StorageException;

    public abstract void removeLogins(List<StorageLogin> logins)
        throws StorageException;

    public abstract void removeRecords(List<StorageRecord> recds)
        throws StorageException;

    public abstract void update(StorageLogin login)
        throws StorageException;

    public abstract void update(StorageRecord recd)
        throws StorageException;

    public List<StorageLogin> getLogins(StorageEntity entity)
        throws StorageException
    {
        if (entity instanceof StorageAddress)
            return getLogins((StorageAddress) entity);

        if (entity instanceof StoragePlayer)
            return getLogins((StoragePlayer) entity);

        entity.invalid();
        return new ArrayList<StorageLogin>();
    }

    public List<StorageLogin> getLogins(StorageLogin login)
        throws StorageException
    {
        List<StorageLogin> logins;

        logins = new ArrayList<StorageLogin>();
        logins.addAll(getLogins(login.player));
        logins.addAll(getLogins(login.address));

        return logins;
    }

    public List<StorageRecord> getRecords(StorageEntity entity)
        throws StorageException
    {
        if (entity instanceof StorageAddress)
            return getRecords((StorageAddress) entity);

        if (entity instanceof StoragePlayer)
            return getRecords((StoragePlayer) entity);

        entity.invalid();
        return new ArrayList<StorageRecord>();
    }

    public List<StorageRecord> getRecords(StorageLogin login)
        throws StorageException
    {
        List<StorageRecord> recds;

        recds = new ArrayList<StorageRecord>();
        recds.addAll(getRecords(login.player));
        recds.addAll(getRecords(login.address));

        return recds;
    }

    public List<StorageRecord> getRecords(StorageRecord recd)
        throws StorageException
    {
        List<StorageRecord> recds;

        recds = new ArrayList<StorageRecord>();
        recds.addAll(getRecords(recd.target));
        recds.addAll(getRecords(recd.issuer));

        return recds;
    }
}
