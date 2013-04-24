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
        APIRecord mr;
        Player    player;
        String    target;

        player = event.getPlayer();
        target = player.getName();
        mr     = vind.api.mutes.get(target);

        if (mr == null)
            return;

        if ((mr.timeout < 1) || (mr.timeout > Utils.time())) {
            Log.info("Player %s attempted to speak muted: %s",
                     hl(target), hl(event.getMessage()));
            Message.severe(player, "You cannot speak while being muted!");
            event.setCancelled(true);
            return;
        }

        mr.issuer = vind.getDescription().getName();

        try {
            vind.api.unmute(mr);
        } catch (APIException e) {
            Log.severe("Failed to unmute %s: %s", target, e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event)
    {
        List<APIRecord> ars;
        APIRecord br;
        APIRecord mr;
        String    target;
        String    str;
        int       nc;

        target = event.getName();
        br     = null;
        mr     = null;
        nc     = 0;

        try {
            ars = vind.api.getAllRecords(target);
        } catch (APIException e) {
            Log.severe(e.getMessage());
            return;
        }

        for (APIRecord r : ars) {
            if (r.hasFlag(APIRecord.BAN)) {
                br = r;
                break;
            } else if (r.hasFlag(APIRecord.MUTE)) {
                mr = r;
            } else if (r.hasFlag(APIRecord.NOTE)) {
                nc++;
            }
        }

        if (br != null) {
            if ((br.timeout < 1) || (br.timeout > Utils.time())) {
                if (br.hasFlag(APIRecord.ADDRESS))
                    str = "Player %s attempted to join with a banned IP: %s";
                else
                    str = "Player %s attempted to join banned: %s";

                event.disallow(Result.KICK_OTHER, "Banned: " + br.message);
                vind.broadcast("vindicator.message.notify", str,
                               hl(target), hl(br.message));
                return;
            }

            br.issuer = vind.getDescription().getName();

            try {
                vind.api.unban(br);
            } catch (APIException e) {
                Log.severe(e.getMessage());
            }
        }

        if (mr != null) {
            if ((mr.timeout > 0) && (mr.timeout < Utils.time())) {
                mr.issuer = vind.getDescription().getName();

                try {
                    vind.api.unmute(mr);
                } catch (APIException e) {
                    Log.severe(e.getMessage());
                }

                mr = null;
                vind.api.mutes.remove(target);
            } else {
                vind.api.mutes.put(target, mr);
            }
        }

        if ((nc < 1) && (mr == null))
            return;

        str = String.format("Player %s has %s note(s)", hl(target), hl(nc));

        if (mr != null)
            str += ", and is " + hl("muted");

        str += ".";
        vind.broadcast("vindicator.message.notify", str);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        vind.api.mutes.remove(event.getPlayer().getName());
    }
}
