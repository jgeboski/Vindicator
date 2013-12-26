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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.jgeboski.vindicator.util.StrUtils;

import static org.jgeboski.vindicator.util.Message.hl;

public class StoragePlayer extends StorageEntity
{
    public StoragePlayer(String name)
    {
        super(name, name);
    }

    public void validate(boolean complete)
        throws StorageException
    {
        String valid;
        Player player;

        if (ident == null)
            return;

        if (!StrUtils.isMinecraftName(ident))
            throw new StorageException("Invalid player name: %s", hl(alias));

        if (!complete)
            return;

        player = Bukkit.getPlayer(ident);

        if (player != null) {
            valid = player.getName();
            ident = valid;
            alias = valid;
        }
    }
}
