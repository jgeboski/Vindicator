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

import org.bukkit.plugin.java.JavaPlugin;

public class Vindicator extends JavaPlugin
{
    public static final String pluginName = "Vindicator";

    public  Configuration config;
    private EventListener events;

    public void onLoad()
    {
        config  = new Configuration(new File(getDataFolder(), "config.yml"));
        events  = new EventListener(this);
    }

    public void onEnable()
    {
        
    }

    public void onDisable()
    {
        
    }
}
