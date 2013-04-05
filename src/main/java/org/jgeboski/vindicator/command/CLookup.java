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

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.jgeboski.vindicator.api.APIException;
import org.jgeboski.vindicator.api.APIRunnable;
import org.jgeboski.vindicator.api.APITask;
import org.jgeboski.vindicator.storage.TargetObject;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.Vindicator;

public class CLookup extends APIRunnable implements CommandExecutor
{
    public Vindicator vind;

    public CLookup(Vindicator vind)
    {
        this.vind = vind;
    }

    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args)
    {
        APITask at;

        if (!Utils.hasPermission(sender, "vindicator.lookup"))
            return true;

        if (args.length < 1) {
            Message.info(sender, command.getUsage());
            return true;
        }

        at = new APITask(this, sender, args[0]);

        try {
            vind.api.lookup(at);
        } catch (APIException e) {
            Message.severe(sender, e.getMessage());
        }

        return true;
    }

    public void run(APITask at, List<TargetObject> tos, APIException expt)
    {
        String type;

        if (tos.size() < 1) {
            Message.info(at.sender, "There are no records for %s", at.target);
            return;
        }

        for (TargetObject to : tos) {
            type = to.hasFlag(TargetObject.ADDRESS) ? "IP" : "Player";

            if (to.hasFlag(TargetObject.BAN)) {
                Message.info(at.sender, "%s[%s] %s Ban (by: %s): %s",
                             ChatColor.RED, Utils.timestr(to.time), type,
                             to.issuer, to.message);
            } else if (to.hasFlag(TargetObject.NOTE)) {
                Message.info(at.sender, "%s[%s] %s Note #%d (by: %s): %s",
                             ChatColor.YELLOW, Utils.timestr(to.time),
                             type, to.id, to.issuer, to.message);
            } else if (to.hasFlag(TargetObject.MUTE)) {
                Message.info(at.sender, "%s[%s] Muted (by: %s): %s",
                             ChatColor.RED, Utils.timestr(to.time),
                             to.issuer, to.message);
            }
        }
    }
}
