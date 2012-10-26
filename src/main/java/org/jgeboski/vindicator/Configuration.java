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
import org.bukkit.configuration.file.YamlConfiguration;

public class Configuration extends YamlConfiguration
{
    private File file;

    public String storeDriver;
    public String storeURL;
    public String storeUser;
    public String storePass;
    public String storePrefix;

    public Configuration(File file)
    {
        this.file = file;

        this.storeDriver = "sql";
        this.storeUser   = null;
        this.storePass   = null;
        this.storePrefix = null;
        this.storeURL    = String.format("jdbc:sqlite:%s%sdatabase.sqlite",
                                         file.getParent(),
                                         file.separator);
    }

    public void load()
    {
        try {
            super.load(file);
        } catch(Exception e) {
            Log.warning("Unable to load: %s", file.toString());
        }

        storeDriver = getString("storage.driver", storeDriver);
        storeURL    = getString("storage.url",    storeURL);
        storeUser   = getString("storage.user",   storeUser);
        storePass   = getString("storage.pass",   storePass);
        storePrefix = getString("storage.prefix", storePrefix);

        if(!file.exists())
            save();
    }

    public void save()
    {
        set("storage.driver", storeDriver);
        set("storage.url",    storeURL);
        set("storage.user",   storeUser);
        set("storage.pass",   storePass);
        set("storage.prefix", storePrefix);

        try {
            super.save(file);
        } catch(Exception e) {
            Log.warning("Unable to save: %s", file.toString());
        }
    }
}
