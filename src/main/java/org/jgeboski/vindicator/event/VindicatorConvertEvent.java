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

package org.jgeboski.vindicator.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;

import org.jgeboski.vindicator.storage.StorageException;
import org.jgeboski.vindicator.storage.StorageLogin;
import org.jgeboski.vindicator.storage.StorageRecord;
import org.jgeboski.vindicator.util.Log;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.VindicatorException;

import static org.jgeboski.vindicator.util.Message.hl;

public class VindicatorConvertEvent extends VindicatorEvent
{
    public static VindicatorConvertEvent converter;

    static
    {
        converter = null;
    }

    public VindicatorConvertEvent(CommandSender sender)
    {
        super(sender, true);
    }

    public VindicatorConvertEvent()
    {
        super(true);
    }

    public void task()
        throws VindicatorException
    {
        PluginManager pm;

        if (converter != null)
            throw new VindicatorException("Storage conversion in progress!");

        if (!eventContinue())
            return;

        converter = this;
        pm = vind.getServer().getPluginManager();

        message("Storage conversion starting...");
        message("Disabled plugin!");
        pm.disablePlugin(vind);

        try {
            logins();
            records();
        } catch (VindicatorException e) {
            converter = null;
            pm.enablePlugin(vind);
            message("Enabled plugin!");
            message("Storage conversion failed!");
            throw e;
        }

        converter = null;
        pm.enablePlugin(vind);
        message("Enabled plugin!");
        message("Storage conversion complete!");
    }

    private void logins()
        throws VindicatorException
    {
        List<StorageLogin>     logins;
        Iterator<StorageLogin> iter;
        StorageLogin           login;
        int size;
        int i;

        message("Loading logins...");

        logins = storage.getLogins();
        iter   = logins.iterator();
        size   = logins.size();

        message("Loaded logins!");
        message("Validating logins...");

        for (i = 1; iter.hasNext(); i++) {
            login = iter.next();

            try {
                login.player.validate(storage, false);
            } catch (StorageException e) {
                message("%s", e.getMessage());
                message("Login removed: %s", login);
                iter.remove();
            }

            if ((i % 100) == 0)
                message("Validated %d of %d logins", i, size);
        }

        message("Validated logins!");
        message("Clearing old logins...");
        storage.clearLogins();
        message("Cleared old logins!");

        message("Adding updated logins...");
        storage.addLogins(logins);
        message("Added updated logins!");
    }

    private void records()
        throws VindicatorException
    {
        List<StorageRecord>     recds;
        Iterator<StorageRecord> iter;
        StorageRecord           recd;
        boolean remove;
        int     size;
        int     i;

        message("Loading records...");

        recds = storage.getRecords();
        iter  = recds.iterator();
        size  = recds.size();

        message("Loaded records!");
        message("Validating records...");

        for (i = 1; iter.hasNext(); i++) {
            recd   = iter.next();
            remove = false;

            try {
                recd.target.validate(storage, false);
            } catch (StorageException e) {
                message("%s", e.getMessage());
                remove = true;
            }

            try {
                recd.issuer.validate(storage, false);
            } catch (StorageException e) {
                message("%s", e.getMessage());
                remove = true;
            }

            if (remove) {
                iter.remove();
                message("Record removed: %s", recd);
            }

            if ((i % 100) == 0)
                message("Validated %d of %d records", i, size);
        }

        message("Validated records!");
        message("Clearing old records...");
        storage.clearRecords();
        message("Cleared old records!");

        message("Adding updated records...");
        storage.addRecords(recds);
        message("Added updated records!");
    }

    private void message(String format, Object ... args)
    {
        format = "[Conversion] " + format;

        Log.info(format, args);

        if ((sender != null) && !(sender instanceof ConsoleCommandSender))
            Message.info(sender, format, args);
    }
}
