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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.jgeboski.vindicator.util.Log;

public class Configuration extends YamlConfiguration
{
    private File file;

    public int  poolMinSize;
    public int  poolMaxSize;
    public long poolKeepAlive;

    public String storeDriver;
    public String storeURL;
    public String storeUser;
    public String storePass;
    public String storePrefix;

    public boolean unbanNote;
    public boolean mustReason;
    public String  defBanReason;
    public String  defKickReason;

    public boolean ircEnabled;
    public boolean ircColored;
    public String  ircTag;

    public Configuration(File file)
    {
        this.file = file;

        this.storeDriver   = "sql";
        this.storeUser     = null;
        this.storePass     = null;
        this.storePrefix   = null;
        this.storeURL      = String.format("jdbc:sqlite:%s%sdatabase.sqlite",
                                           file.getParent(), file.separator);

        this.poolMinSize   = 2;
        this.poolMaxSize   = 10;
        this.poolKeepAlive = 5000;

        this.unbanNote     = false;
        this.mustReason    = false;
        this.defBanReason  = "You have been banned";
        this.defKickReason = "You have been kicked";

        this.ircEnabled    = false;
        this.ircColored    = true;
        this.ircTag        = "vindicator";
    }

    public void load()
    {
        ConfigurationSection cs;

        try {
            super.load(file);
        } catch(Exception e) {
            Log.warning("Unable to load: %s", file.toString());
        }

        cs            = getConfigurationSection("storage");
        storeDriver   = cs.getString("driver", storeDriver);
        storeURL      = cs.getString("url",    storeURL);
        storeUser     = cs.getString("user",   storeUser);
        storePass     = cs.getString("pass",   storePass);
        storePrefix   = cs.getString("prefix", storePrefix);

        cs            = getConfigurationSection("pool");
        poolMinSize   = cs.getInt("min-size",    poolMinSize);
        poolMaxSize   = cs.getInt("max-size",    poolMaxSize);
        poolKeepAlive = cs.getLong("keep-alive", poolKeepAlive);

        cs            = getConfigurationSection("settings");
        unbanNote     = cs.getBoolean("unban-to-note", unbanNote);
        mustReason    = cs.getBoolean("must-reason",   mustReason);
        defBanReason  = cs.getString("default-kick",   defBanReason);
        defKickReason = cs.getString("default-ban",    defKickReason);

        cs            = getConfigurationSection("irc");
        ircEnabled    = cs.getBoolean("enabled", ircEnabled);
        ircColored    = cs.getBoolean("colored", ircEnabled);
        ircTag        = cs.getString("tag",      ircTag);

        if(!file.exists())
            save();
    }

    public void save()
    {
        ConfigurationSection cs;

        cs = getConfigurationSection("storage");
        cs.set("driver", storeDriver);
        cs.set("url",    storeURL);
        cs.set("user",   storeUser);
        cs.set("pass",   storePass);
        cs.set("prefix", storePrefix);

        cs = getConfigurationSection("pool");
        cs.set("min-size",   poolMinSize);
        cs.set("max-size",   poolMaxSize);
        cs.set("keep-alive", poolKeepAlive);

        cs = getConfigurationSection("settings");
        cs.set("unban-to-note", unbanNote);
        cs.set("must-reason",   mustReason);
        cs.set("default-kick",  defBanReason);
        cs.set("default-ban",   defKickReason);

        cs = getConfigurationSection("irc");
        cs.set("enabled", ircEnabled);
        cs.set("colored", ircEnabled);
        cs.set("tag",     ircTag);

        try {
            super.save(file);
        } catch(Exception e) {
            Log.warning("Unable to save: %s", file.toString());
        }
    }

    public ConfigurationSection getConfigurationSection(String path)
    {
        ConfigurationSection ret;

        ret = super.getConfigurationSection(path);

        if(ret == null)
            ret = createSection(path);

        return ret;
    }
}
