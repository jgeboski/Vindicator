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
import org.jgeboski.vindicator.util.Log;

public class Configuration extends YamlConfiguration
{
    private File file;

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

        this.unbanNote     = false;
        this.mustReason    = false;
        this.defBanReason  = "You have been banned";
        this.defKickReason = "You have been kicked";

        this.storeDriver   = "sql";
        this.storeUser     = null;
        this.storePass     = null;
        this.storePrefix   = null;
        this.storeURL      = String.format("jdbc:sqlite:%s%sdatabase.sqlite",
                                           file.getParent(), file.separator);

        this.ircEnabled    = false;
        this.ircColored    = true;
        this.ircTag        = "vindicator";
    }

    public void load()
    {
        try {
            super.load(file);
        } catch(Exception e) {
            Log.warning("Unable to load: %s", file.toString());
        }

        unbanNote     = getBoolean("settings.unban-to-note", unbanNote);
        mustReason    = getBoolean("settings.must-reason",   mustReason);
        defBanReason  = getString("settings.default-kick",   defBanReason);
        defKickReason = getString("settings.default-ban",    defKickReason);

        storeDriver   = getString("storage.driver",          storeDriver);
        storeURL      = getString("storage.url",             storeURL);
        storeUser     = getString("storage.user",            storeUser);
        storePass     = getString("storage.pass",            storePass);
        storePrefix   = getString("storage.prefix",          storePrefix);

        ircEnabled    = getBoolean("irc.enabled",            ircEnabled);
        ircColored    = getBoolean("irc.colored",            ircEnabled);
        ircTag        = getString("irc.tag",                 ircTag);

        if(!file.exists())
            save();
    }

    public void save()
    {
        set("settings.unban-to-note", unbanNote);
        set("settings.must-reason",   mustReason);
        set("settings.default-kick",  defBanReason);
        set("settings.default-ban",   defKickReason);

        set("storage.driver",         storeDriver);
        set("storage.url",            storeURL);
        set("storage.user",           storeUser);
        set("storage.pass",           storePass);
        set("storage.prefix",         storePrefix);

        set("irc.enabled",            ircEnabled);
        set("irc.colored",            ircEnabled);
        set("irc.tag",                ircTag);

        try {
            super.save(file);
        } catch(Exception e) {
            Log.warning("Unable to save: %s", file.toString());
        }
    }
}
