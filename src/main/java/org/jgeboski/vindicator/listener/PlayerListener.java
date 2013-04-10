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
import org.jgeboski.vindicator.api.APIRunnable;
import org.jgeboski.vindicator.api.APITask;
import org.jgeboski.vindicator.storage.StorageException;
import org.jgeboski.vindicator.storage.TargetObject;
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
        TargetObject mute;
        APITask      at;
        Player       player;
        String       target;

        player = event.getPlayer();
        target = player.getName();
        mute   = vind.api.mutes.get(target);

        if (mute == null)
            return;

        if ((mute.timeout < 1) || (mute.timeout > Utils.time())) {
            Log.info("Player %s attempted to speak muted: %s",
                     hl(target), hl(event.getMessage()));
            Message.severe(player, "You cannot speak while being muted!");
            event.setCancelled(true);
            return;
        }

        at = new APITask(null, mute);
        at.issuer = Vindicator.pluginName;

        try {
            vind.api.unmute(at);
        } catch (APIException e) {
            Log.severe("Failed to unmute %s: %s", target, e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event)
    {
        List<TargetObject> tos;
        TargetObject ban;
        TargetObject mute;
        APITask      at;

        String target;
        String str;
        int    notes;

        target = event.getName();
        tos    = null;
        mute   = null;
        ban    = null;
        notes  = 0;

        try {
            tos = vind.api.storage.getRecords(target);
        } catch (StorageException e) { }

        str = event.getAddress().getHostAddress();

        try {
            if (tos != null)
                tos.addAll(vind.api.storage.getRecords(str));
            else
                tos = vind.api.storage.getRecords(str);
        } catch (StorageException e) { }

        for (TargetObject to : tos) {
            if (to.hasFlag(TargetObject.BAN)) {
                ban = to;
                break;
            } else if (to.hasFlag(TargetObject.MUTE)) {
                mute = to;
            } else if (to.hasFlag(TargetObject.NOTE)) {
                notes++;
            }
        }

        if (ban != null) {
            if ((ban.timeout < 1) || (ban.timeout > Utils.time())) {
                if (ban.hasFlag(TargetObject.ADDRESS))
                    str = "Player %s attempted to join with a banned IP: %s";
                else
                    str = "Player %s attempted to join banned: %s";

                event.disallow(Result.KICK_OTHER, "Banned: " + ban.message);
                vind.broadcast("vindicator.message.notify", str,
                               hl(target), hl(ban.message));
                return;
            }

            at = new APITask(null, ban);
            at.issuer = Vindicator.pluginName;

            try {
                vind.api.unban(at);
            } catch (APIException e) {
                Log.severe("Failed to unban %s: %s", target, e.getMessage());
            }
        }

        if (mute != null)
            vind.api.mutes.put(target, mute);

        if ((notes < 1) && (mute == null))
            return;

        str = String.format("Player %s has %s note(s)", hl(target), hl(notes));

        if (mute != null)
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
