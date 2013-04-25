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

package org.jgeboski.vindicator.listener;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import org.jgeboski.vindicator.api.APIException;
import org.jgeboski.vindicator.api.APIRecord;
import org.jgeboski.vindicator.api.APIRunnable;
import org.jgeboski.vindicator.storage.StorageException;
import org.jgeboski.vindicator.util.Log;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.Vindicator;

import static org.jgeboski.vindicator.util.Message.hl;

public class PlayerListener extends APIRunnable implements Listener
{
    public Vindicator vind;

    public PlayerListener(Vindicator vind)
    {
        this.vind = vind;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        Player player;
        String str;

        player = event.getPlayer();

        try {
            vind.api.checkChat(player.getName(), event.getMessage());
        } catch (APIException e) {
            if (e instanceof StorageException) {
                str = "Failed mute check. Notify the administrator.";
                Log.severe(e.getMessage());
            } else {
                str = e.getMessage();
            }

            event.setCancelled(true);
            Message.severe(player, str);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event)
    {
        String str;

        str = event.getName();

        try {
            vind.api.checkRecords(str);
            vind.api.checkAddresses(str, event.getAddress().getHostAddress());
        } catch (APIException e) {
            if (e instanceof StorageException) {
                str = "Failed username check. Notify the administrator.";
                Log.severe(e.getMessage());
            } else {
                str = e.getMessage();
            }

            event.disallow(Result.KICK_OTHER, str);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        vind.api.mutes.remove(event.getPlayer().getName());
    }
}
