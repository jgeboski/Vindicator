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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.jgeboski.vindicator.util.Log;

public class Configuration extends YamlConfiguration
{
    private File file;

    public String storeDriver;
    public String storeURL;
    public String storeUser;
    public String storePass;
    public String storePrefix;

    public int  poolMinSize;
    public int  poolMaxSize;
    public long poolKeepAlive;

    public String  defBanReason;
    public String  defKickReason;
    public String  defMuteReason;
    public boolean autoComplete;
    public boolean mustReason;
    public boolean banUpdate;
    public boolean muteUpdate;
    public boolean unbanNote;
    public boolean unmuteNote;
    public int     altInfoLogins;
    public long    altInfoTime;

    public String  ircTag;
    public boolean ircEnabled;
    public boolean ircColored;

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

        this.defBanReason  = "You have been banned";
        this.defKickReason = "You have been kicked";
        this.defMuteReason = "You have been muted";
        this.autoComplete  = true;
        this.mustReason    = false;
        this.banUpdate     = true;
        this.muteUpdate    = true;
        this.unbanNote     = false;
        this.unmuteNote    = false;
        this.altInfoLogins = -1;
        this.altInfoTime   = -1;

        this.ircTag        = "vindicator";
        this.ircEnabled    = false;
        this.ircColored    = true;
    }

    public void load()
    {
        ConfigurationSection cs;

        try {
            super.load(file);
        } catch (Exception e) {
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
        defBanReason  = cs.getString("default-kick",    defBanReason);
        defKickReason = cs.getString("default-ban",     defKickReason);
        defMuteReason = cs.getString("default-mute",    defMuteReason);
        autoComplete  = cs.getBoolean("auto-complete",  autoComplete);
        mustReason    = cs.getBoolean("must-reason",    mustReason);
        banUpdate     = cs.getBoolean("ban-update",     banUpdate);
        muteUpdate    = cs.getBoolean("mute-update",    muteUpdate);
        unbanNote     = cs.getBoolean("unban-to-note",  unbanNote);
        unmuteNote    = cs.getBoolean("unmute-to-note", unmuteNote);
        altInfoLogins = cs.getInt("alt-info-logins",    altInfoLogins);
        altInfoTime   = cs.getLong("alt-info-time",     altInfoTime);

        cs            = getConfigurationSection("irc");
        ircTag        = cs.getString("tag",      ircTag);
        ircEnabled    = cs.getBoolean("enabled", ircEnabled);
        ircColored    = cs.getBoolean("colored", ircEnabled);

        if (!file.exists())
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
        cs.set("default-kick",    defBanReason);
        cs.set("default-ban",     defKickReason);
        cs.set("default-mute",    defMuteReason);
        cs.set("auto-complete",   autoComplete);
        cs.set("must-reason",     mustReason);
        cs.set("ban-update",      banUpdate);
        cs.set("mute-update",     muteUpdate);
        cs.set("unban-to-note",   unbanNote);
        cs.set("unmute-to-note",  unmuteNote);
        cs.set("alt-info-logins", altInfoLogins);
        cs.set("alt-info-time",   altInfoTime);

        cs = getConfigurationSection("irc");
        cs.set("tag",     ircTag);
        cs.set("enabled", ircEnabled);
        cs.set("colored", ircEnabled);

        try {
            super.save(file);
        } catch (Exception e) {
            Log.warning("Unable to save: %s", file.toString());
        }
    }

    public ConfigurationSection getConfigurationSection(String path)
    {
        ConfigurationSection ret;

        ret = super.getConfigurationSection(path);

        if (ret == null)
            ret = createSection(path);

        return ret;
    }
}
