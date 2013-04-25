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

import java.util.List;
import org.jgeboski.vindicator.api.APIAddress;
import org.jgeboski.vindicator.api.APIRecord;

public abstract class Storage
{
    public abstract void close();

    public abstract void add(APIAddress aa)
        throws StorageException;

    public abstract void add(APIRecord ar)
        throws StorageException;

    public abstract void remove(APIAddress aa)
        throws StorageException;

    public abstract void remove(APIRecord ar)
        throws StorageException;

    public abstract void update(APIAddress aa)
        throws StorageException;

    public abstract void update(APIRecord ar)
        throws StorageException;

    public abstract APIAddress getAddress(String player, String address)
        throws StorageException;

    public abstract List<APIAddress> getAddresses(String player)
        throws StorageException;

    public abstract List<APIAddress> getAddressPlayers(String address)
        throws StorageException;

    public abstract List<APIRecord> getRecords(String target)
        throws StorageException;

    public APIAddress getAddress(APIAddress aa)
        throws StorageException
    {
        return getAddress(aa.player, aa.address);
    }

    public List<APIAddress> getAddresses(APIAddress aa)
        throws StorageException
    {
        return getAddresses(aa.player);
    }

    public List<APIAddress> getAddressPlayers(APIAddress aa)
        throws StorageException
    {
        return getAddressPlayers(aa.address);
    }

    public List<APIRecord> getRecords(APIRecord ar)
        throws StorageException
    {
        return getRecords(ar.target);
    }
}
