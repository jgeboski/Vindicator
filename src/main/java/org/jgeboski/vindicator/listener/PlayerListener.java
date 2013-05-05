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

import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.jgeboski.vindicator.api.APIException;
import org.jgeboski.vindicator.api.APILogin;
import org.jgeboski.vindicator.api.APIRecord;
import org.jgeboski.vindicator.api.APIRunnable;
import org.jgeboski.vindicator.storage.StorageException;
import org.jgeboski.vindicator.util.Kick;
import org.jgeboski.vindicator.util.Log;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.Vindicator;

import static org.jgeboski.vindicator.util.Message.hl;

public class PlayerListener extends APIRunnable implements Listener
{
    public Vindicator vind;
    public HashSet<String> checking;

    public PlayerListener(Vindicator vind)
    {
        this.vind     = vind;
        this.checking = new HashSet<String>();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        Player player;
        String pname;
        String str;

        player = event.getPlayer();
        pname  = player.getName();

        if (isEntityChecking(player)) {
            event.setCancelled(true);
            return;
        }

        try {
            vind.api.checkChat(pname, event.getMessage());
        } catch (APIException e) {
            if (e instanceof StorageException) {
                str = "Failed mute check. Notify the admin.";
                Log.severe(e.getMessage());
            } else {
                str = e.getMessage();
            }

            event.setCancelled(true);
            Message.severe(player, str);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        APILogin al;
        String   str;

        if (vind.getServer().getOnlineMode())
            return;

        al = new APILogin(this, event.getPlayer());

        try {
            vind.api.login(al);
            checking.add(al.pname);
            event.setJoinMessage(null);
        } catch (APIException e) {
            str = "Failed mute check. Notify the admin.";

            Log.severe(e.getMessage());
            al.player.kickPlayer(Message.format(str));
        }
    }

    public void run(final APILogin al, final APIException expt)
    {
        if (expt == null) {
            checking.remove(al.pname);
            return;
        }

        Kick.player(vind, al.player, Message.format(expt.getMessage()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event)
    {
        String pname;
        String address;
        String str;

        pname   = event.getName();
        address = event.getAddress().getHostAddress();

        try {
            vind.api.checkAddresses(pname, address);
            vind.api.checkRecords(pname, address);
        } catch (APIException e) {
            if (e instanceof StorageException) {
                str = "Failed username check. Notify the administrator.";
                Log.severe(e.getMessage());
            } else {
                str = e.getMessage();
            }

            event.disallow(Result.KICK_OTHER, Message.format(str));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        String pname;

        pname = event.getPlayer().getName();

        if (checking.remove(pname))
            event.setQuitMessage(null);

        vind.api.mutes.remove(pname);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event)
    {
        event.setCancelled(isEntityChecking(event.getEntity()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        event.setCancelled(isEntityChecking(event.getDamager()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFEntityTarget(EntityTargetEvent event)
    {
        event.setCancelled(isEntityChecking(event.getTarget()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event)
    {
        event.setCancelled(isEntityChecking(event.getEntity()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        event.setCancelled(isPlayerChecking(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        event.setCancelled(isPlayerChecking(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        event.setCancelled(isPlayerChecking(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        event.setCancelled(isPlayerChecking(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event)
    {
        event.setCancelled(isPlayerChecking(event.getPlayer()));
    }

    private boolean isEntityChecking(Entity entity)
    {
        if (!(entity instanceof Player))
            return false;

        return checking.contains(((Player) entity).getName());
    }

    private boolean isPlayerChecking(Player player)
    {
        return checking.contains(player.getName());
    }
}
