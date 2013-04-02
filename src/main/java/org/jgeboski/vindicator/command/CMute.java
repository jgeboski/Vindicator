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

import org.jgeboski.vindicator.api.APIException;
import org.jgeboski.vindicator.api.APIRunnable;
import org.jgeboski.vindicator.api.APITask;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.StrUtils;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.Vindicator;

public class CMute extends APIRunnable implements CommandExecutor
{
    public Vindicator vind;

    public CMute(Vindicator vind)
    {
        this.vind = vind;
    }

    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args)
    {
        APITask at;

        if (!Utils.hasPermission(sender, "vindicator.mute"))
            return true;

        if (args.length < 1) {
            Message.info(sender, command.getUsage());
            return true;
        }

        at = new APITask(this, sender, args[0]);

        if (args.length > 1) {
            at.timeout = StrUtils.strsecs(args[1]);
            at.message = StrUtils.strjoin(args, " ",
                             ((at.timeout == 0) ? 1 : 2));
        }

        try {
            vind.api.mute(at);
        } catch (APIException e) {
            Message.severe(sender, e.getMessage());
        }

        return true;
    }

    public void run(APITask at, APIException expt)
    {
        if (expt != null)
            Message.severe(at.sender, expt.getMessage());
    }
}
