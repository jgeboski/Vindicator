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

public class StorageLogin
{
    public StoragePlayer  player;
    public StorageAddress address;
    public int            id;
    public int            count;
    public long           time;

    public StorageLogin(StoragePlayer player, StorageAddress address)
    {
        this.player  = player;
        this.address = address;
        this.id      = 0;
        this.count   = 1;
        this.time    = Utils.time();
    }

    public StorageLogin(String player, String address)
    {
        this(new StoragePlayer(player), new StorageAddress(address));
    }

    public void validate(boolean complete)
        throws StorageException
    {
        if (player != null)
            player.validate(complete);

        if (address != null)
            address.validate(complete);
    }
}
