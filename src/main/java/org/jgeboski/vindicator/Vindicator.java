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

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import org.jgeboski.vindicator.api.VindicatorAPI;
import org.jgeboski.vindicator.storage.Storage;
import org.jgeboski.vindicator.storage.StorageSQL;

public class Vindicator extends JavaPlugin
{
    public static final String pluginName = "Vindicator";

    public Configuration config;
    public Storage       storage;
    public VindicatorAPI api;

    private EventListener events;

    public void onLoad()
    {
        config  = new Configuration(new File(getDataFolder(), "config.yml"));
        storage = null;
        api     = new VindicatorAPI();

        events  = new EventListener(this);
    }

    public void onEnable()
    {
        config.load();

        /* For now, SQL only */
        storage = new StorageSQL(config.storeURL,  config.storeUser,
                                 config.storePass, config.storePrefix);

        if(!storage.onEnable()) {
            setEnabled(false);
            return;
        }

        
    }

    public void onDisable()
    {
        if(storage != null)
            storage.onDisable();
    }

    public void reload()
    {
        config.load();
    }

    public boolean hasPermissionM(CommandSender sender, String perm)
    {
        if(sender.hasPermission(perm))
            return true;
        
        Message.severe(sender, "You don't have permission for that");
        return false;
    }
}
