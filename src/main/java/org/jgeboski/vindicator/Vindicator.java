/*
 * Copyright 2012 James Geboski <jgeboski@gmail.com>
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

package org.jgeboski.vindicator;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.ensifera.animosity.craftirc.CraftIRC;
import com.ensifera.animosity.craftirc.EndPoint;
import com.ensifera.animosity.craftirc.RelayedMessage;

import org.jgeboski.vindicator.api.VindicatorAPI;
import org.jgeboski.vindicator.command.*;
import org.jgeboski.vindicator.storage.Storage;
import org.jgeboski.vindicator.storage.StorageSQL;

public class Vindicator extends JavaPlugin
{
    public static final String pluginName = "Vindicator";

    public Configuration config;
    public Storage       storage;
    public VindicatorAPI api;

    private EventListener events;

    public CraftIRC craftirc;
    public VPoint   vPoint;

    public void onLoad()
    {
        config   = new Configuration(new File(getDataFolder(), "config.yml"));
        storage  = null;
        api      = new VindicatorAPI();
        events   = new EventListener(this);

        craftirc = null;
        vPoint   = null;
    }

    public void onEnable()
    {
        PluginManager pm;
        Plugin p;

        config.load();

        if(config.ircEnabled) {
            pm = getServer().getPluginManager();
            p  = pm.getPlugin("CraftIRC");

            if((p != null) && p.isEnabled()) {
                craftirc = (CraftIRC) p;
                vPoint   = new VPoint();
            }

            if(!registerEndPoint(config.ircTag, vPoint))
                config.ircEnabled = false;
        }

        /* For now, SQL only */
        storage = new StorageSQL(config.storeURL,  config.storeUser,
                                 config.storePass, config.storePrefix);

        if(!storage.onEnable()) {
            setEnabled(false);
            return;
        }

       getCommand("ban").setExecutor(new CBan(this));
       getCommand("kick").setExecutor(new CKick(this));
       getCommand("lookup").setExecutor(new CLookup(this));
       getCommand("noteadd").setExecutor(new CNoteAdd(this));
       getCommand("noterem").setExecutor(new CNoteRem(this));
       getCommand("unban").setExecutor(new CUnban(this));
       getCommand("vindicator").setExecutor(new CVindicator(this));
    }

    public void onDisable()
    {
        if(config.ircEnabled)
            craftirc.unregisterEndPoint(config.ircTag);

        if(storage != null)
            storage.onDisable();
    }

    public void reload()
    {
        if(config.ircEnabled)
            craftirc.unregisterEndPoint(config.ircTag);

        config.load();

        if(config.ircEnabled && !registerEndPoint(config.ircTag, vPoint))
            config.ircEnabled = false;
    }

    private boolean registerEndPoint(String tag, Object ep)
    {
        if(craftirc == null)
            return false;

        if(craftirc.registerEndPoint(tag, (EndPoint) ep))
            return true;

        Log.severe("Unable to register CraftIRC tag: %s", tag);
        return false;
    }

    public boolean hasPermissionM(CommandSender sender, String perm)
    {
        if(sender.hasPermission(perm))
            return true;
        
        Message.severe(sender, "You don't have permission for that");
        return false;
    }

    public void broadcast(String perm, String format, Object ... args)
    {
        String msg;

        msg = String.format(format, args);

        for(Player p : getServer().getOnlinePlayers()) {
            if(p.hasPermission(perm))
                Message.info(p, msg);
        }

        Log.info(format, args);

        if(!config.ircEnabled)
            return;

        RelayedMessage rmsg = craftirc.newMsg(vPoint, null, "chat");

        if(!config.ircColored)
            msg = ChatColor.stripColor(msg);

        rmsg.setField("realSender", pluginName);
        rmsg.setField("sender",     pluginName);
        rmsg.setField("message",    msg);

        if(rmsg.post())
            return;

        registerEndPoint(config.ircTag, vPoint);
        rmsg.post();
    }
}
