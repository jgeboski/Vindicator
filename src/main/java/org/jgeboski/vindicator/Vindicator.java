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

import org.jgeboski.vindicator.api.APIException;
import org.jgeboski.vindicator.api.VindicatorAPI;
import org.jgeboski.vindicator.command.*;
import org.jgeboski.vindicator.listener.PlayerListener;
import org.jgeboski.vindicator.util.Log;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;

public class Vindicator extends JavaPlugin
{
    public Configuration config;
    public VindicatorAPI api;

    public CraftIRC craftirc;
    public VPoint   vPoint;

    public void onLoad()
    {
        config   = new Configuration(new File(getDataFolder(), "config.yml"));
        api      = null;

        craftirc = null;
        vPoint   = null;
    }

    public void onEnable()
    {
        PluginManager pm;
        Plugin        p;

        Log.init(getLogger());
        Message.init(getDescription().getName());

        pm = getServer().getPluginManager();
        config.load();

        try {
            api = new VindicatorAPI(this);
        } catch (APIException e) {
            Log.severe("Failed to enable VindicatorAPI: %s", e.getMessage());
            setEnabled(false);
            return;
        }

        if (config.ircEnabled) {
            p = pm.getPlugin("CraftIRC");

            if ((p != null) && p.isEnabled()) {
                craftirc = (CraftIRC) p;
                vPoint   = new VPoint();
            }

            if (!registerEndPoint(config.ircTag, vPoint))
                config.ircEnabled = false;
        }

        pm.registerEvents(new PlayerListener(this), this);

        getCommand("ban").setExecutor(new CBan(this));
        getCommand("kick").setExecutor(new CKick(this));
        getCommand("lookup").setExecutor(new CLookup(this));
        getCommand("lookupa").setExecutor(new CLookupA(this));
        getCommand("mute").setExecutor(new CMute(this));
        getCommand("noteadd").setExecutor(new CNoteAdd(this));
        getCommand("noterem").setExecutor(new CNoteRem(this));
        getCommand("unban").setExecutor(new CUnban(this));
        getCommand("unmute").setExecutor(new CUnmute(this));
        getCommand("vindicator").setExecutor(new CVindicator(this));
    }

    public void onDisable()
    {
        if (config.ircEnabled)
            craftirc.unregisterEndPoint(config.ircTag);

        if (api != null)
            api.close();
    }

    public void reload()
    {
        PluginManager pm;

        pm = getServer().getPluginManager();

        pm.disablePlugin(this);
        pm.enablePlugin(this);
    }

    private boolean registerEndPoint(String tag, Object ep)
    {
        if (craftirc == null)
            return false;

        if (craftirc.registerEndPoint(tag, (EndPoint) ep))
            return true;

        Log.severe("Unable to register CraftIRC tag: %s", tag);
        return false;
    }

    public void broadcast(String perm, String format, Object ... args)
    {
        String msg;
        String name;

        msg = Message.format(format, args);
        Utils.broadcast(perm, msg);

        if (!config.ircEnabled)
            return;

        RelayedMessage rmsg;

        /* This typecasting is needed to prevent a ClassNotFoundException
         * from being thrown over com.ensifera.animosity.craftirc.EndPoint.
         */
        rmsg = craftirc.newMsg((EndPoint) ((Object) vPoint), null, "chat");
        name = getDescription().getName();

        if (!config.ircColored)
            msg = ChatColor.stripColor(msg);

        rmsg.setField("realSender", name);
        rmsg.setField("sender",     name);
        rmsg.setField("message",    msg);

        if (rmsg.post())
            return;

        registerEndPoint(config.ircTag, vPoint);
        rmsg.post();
    }
}
