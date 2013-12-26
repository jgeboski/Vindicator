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

package org.jgeboski.vindicator.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.jgeboski.vindicator.event.VindicatorNoteRemEvent;
import org.jgeboski.vindicator.storage.StorageException;
import org.jgeboski.vindicator.storage.StorageRecord;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.Vindicator;
import org.jgeboski.vindicator.VindicatorException;

public class CNoteRem implements CommandExecutor
{
    public Vindicator vind;

    public CNoteRem(Vindicator vind)
    {
        this.vind = vind;
    }

    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args)
    {
        StorageRecord recd;

        if (!Utils.hasPermission(sender, "vindicator.noterem"))
            return true;

        if (args.length < 2) {
            Message.info(sender, command.getUsage());
            return true;
        }

        try {
            recd = new StorageRecord(args[0], sender.getName());
        } catch (StorageException e) {
            Message.severe(sender, e.getMessage());
            return true;
        }

        args[1] = args[1].replaceAll("#", "");

        try {
            recd.id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            Message.severe(sender, "Invalid note index: %s", args[1]);
            return true;
        }

        try {
            vind.queue(new VindicatorNoteRemEvent(recd, sender));
        } catch (VindicatorException e) {
            Message.severe(sender, e.getMessage());
        }

        return true;
    }
}
