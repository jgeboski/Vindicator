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

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mojang.api.profiles.HttpProfileRepository;
import com.mojang.api.profiles.Profile;
import com.mojang.api.profiles.ProfileCriteria;

import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.StrUtils;

import static org.jgeboski.vindicator.util.Message.hl;

public class StoragePlayer extends StorageEntity
{
    public StoragePlayer(String ident, String alias)
    {
        super(ident, alias);
    }

    public StoragePlayer(String player)
    {
        super(null, null);

        if (StrUtils.isUUID(player))
            this.ident = player;
        else
            this.alias = player;
    }

    public void validate(Storage storage, boolean complete)
        throws StorageException
    {
        Player  player;
        boolean amcn;
        boolean imcn;
        boolean iuuid;

        amcn  = StrUtils.isMinecraftName(alias);
        imcn  = StrUtils.isMinecraftName(ident);
        iuuid = StrUtils.isUUID(ident);

        if (amcn && (imcn || iuuid))
            return;

        if (iuuid) {
            try {
                alias = getPlayerName(storage, ident);
            } catch (StorageException e) {
                alias = ident;
                throw e;
            }

            return;
        }

        if (!amcn)
            throw new StorageException("Invalid player: %s", hl(alias));

        if (complete) {
            player = Bukkit.getPlayer(alias);

            if (player != null)
                alias = player.getName();
        }

        try {
            ident = getPlayerId(alias);
        } catch (StorageException e) {
            ident = alias;
            throw e;
        }
    }

    public static String getPlayerId(String player)
        throws StorageException
    {
        HttpProfileRepository repo;
        ProfileCriteria       crit;
        Profile               prfs[];
        Player                plyr;
        String                uuid;

        if (!Bukkit.getOnlineMode())
            return player;

        if (player.equalsIgnoreCase(Message.plugin))
            return new UUID(0, 0).toString();

        if (player.equalsIgnoreCase(Bukkit.getConsoleSender().getName()))
            return new UUID(1, 1).toString();

        plyr = Bukkit.getPlayerExact(player);

        if (plyr != null)
            return getPlayerId(plyr);

        repo = new HttpProfileRepository();
        crit = new ProfileCriteria(player, "minecraft");
        prfs = repo.findProfilesByCriteria(crit);

        if (prfs.length != 1)
            throw new StorageException("Failed to obtain UUID: %s", player);

        uuid = prfs[0].getId();
        uuid = String.format("%s-%s-%s-%s-%s", uuid.substring(0, 8),
                             uuid.substring(8, 12), uuid.substring(12, 16),
                             uuid.substring(16, 20), uuid.substring(20, 32));

        return uuid;
    }

    public static String getPlayerId(Player player)
    {
        if (!Bukkit.getOnlineMode())
            return player.getName();

        return player.getUniqueId().toString();
    }

    public static String getPlayerName(Storage storage, String uuid)
        throws StorageException
    {
        StorageLogin   login;
        StoragePlayer  plyr;

        if (uuid.equals(new UUID(0, 0).toString()))
            return Message.plugin;

        if (uuid.equals(new UUID(1, 1).toString()))
            return Bukkit.getConsoleSender().getName();

        plyr  = new StoragePlayer(uuid);
        login = storage.getLoginLast(plyr);

        if (login == null)
            return uuid;

        return login.player.ident;
    }
}
