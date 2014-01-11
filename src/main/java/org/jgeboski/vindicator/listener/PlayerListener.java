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

import org.jgeboski.vindicator.event.VindicatorChatEvent;
import org.jgeboski.vindicator.event.VindicatorCommandEvent;
import org.jgeboski.vindicator.event.VindicatorLoginEvent;
import org.jgeboski.vindicator.storage.StorageAddress;
import org.jgeboski.vindicator.storage.StorageLogin;
import org.jgeboski.vindicator.storage.StoragePlayer;
import org.jgeboski.vindicator.util.Kick;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.Vindicator;
import org.jgeboski.vindicator.VindicatorException;

public class PlayerListener implements Listener
{
    public Vindicator      vind;
    public HashSet<String> checking;

    public PlayerListener(Vindicator vind)
    {
        this.vind     = vind;
        this.checking = new HashSet<String>();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        StoragePlayer plyr;
        Player        player;

        player     = event.getPlayer();
        plyr       = new StoragePlayer(player.getName());
        plyr.ident = StoragePlayer.getPlayerId(player);

        if (player.hasPermission("vindicator.exempt"))
            return;

        if (isEntityChecking(player)) {
            event.setCancelled(true);
            return;
        }

        try {
            vind.execute(new VindicatorChatEvent(plyr, event.getMessage()));
        } catch (VindicatorException e) {
            event.setCancelled(true);
            Message.severe(player, e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        StoragePlayer plyr;
        Player        player;

        player     = event.getPlayer();
        plyr       = new StoragePlayer(player.getName());
        plyr.ident = StoragePlayer.getPlayerId(player);

        if (player.hasPermission("vindicator.exempt"))
            return;

        if (isEntityChecking(player)) {
            event.setCancelled(true);
            return;
        }

        try {
            vind.execute(new VindicatorCommandEvent(plyr, event.getMessage()));
        } catch (VindicatorException e) {
            event.setCancelled(true);
            Message.severe(player, e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        VindicatorLoginEvent levnt;
        StorageLogin         login;
        Player               player;
        String               name;
        String               addr;

        if (vind.getServer().getOnlineMode())
            return;

        player = event.getPlayer();
        name   = player.getName();
        addr   = player.getAddress().getAddress().getHostAddress();

        if (player.hasPermission("vindicator.exempt"))
            return;

        login = new StorageLogin(name, addr);
        levnt = new VindicatorLoginEvent(login);

        try {
            vind.queue(levnt);
            checking.add(login.player.ident);
            event.setJoinMessage(null);
        } catch (VindicatorException e) {
            Kick.target(vind, login.player.ident,
                        Message.format(e.getMessage()));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event)
    {
        StorageLogin login;
        String       name;
        String       addr;

        name = event.getName();
        addr = event.getAddress().getHostAddress();

        if (Utils.hasPrePermission(name, "vindicator.exempt"))
            return;

        login = new StorageLogin(name, addr);

        try {
            vind.execute(new VindicatorLoginEvent(login));
        } catch (VindicatorException e) {
            event.disallow(Result.KICK_OTHER, Message.format(e.getMessage()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        String uuid;

        uuid = StoragePlayer.getPlayerId(event.getPlayer());

        if (checking.remove(uuid))
            event.setQuitMessage(null);

        vind.mutes.remove(uuid);
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
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        event.setCancelled(isEntityChecking(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        event.setCancelled(isEntityChecking(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        event.setCancelled(isEntityChecking(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event)
    {
        event.setCancelled(isEntityChecking(event.getPlayer()));
    }

    private boolean isEntityChecking(Entity entity)
    {
        if (!(entity instanceof Player))
            return false;

        return checking.contains(StoragePlayer.getPlayerId((Player) entity));
    }
}
