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

import org.jgeboski.vindicator.exception.APIException;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.StrUtils;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.Vindicator;

public class CBan implements CommandExecutor
{
    public Vindicator vind;

    public CBan(Vindicator vind)
    {
        this.vind = vind;
    }

    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args)
    {
        String reason;
        long   secs;

        if (!Utils.hasPermission(sender, "vindicator.ban"))
            return true;

        if (args.length < 1) {
            Message.info(sender, command.getUsage());
            return true;
        }

        reason = null;
        secs   = 0;

        if (args.length > 1) {
            secs = StrUtils.strsecs(args[1]);

            if (secs != 0) {
                if (args.length > 2)
                    reason = StrUtils.strjoin(args, " ", 2);
            } else {
                reason = StrUtils.strjoin(args, " ", 1);
            }
        }

        if (reason == null) {
            if (vind.config.mustReason) {
                Message.severe(sender, "A reason must be specified");
                return true;
            }

            reason = vind.config.defBanReason;
        }

        try {
            vind.api.ban(sender, args[0], reason, secs);
        } catch (APIException e) {
            Message.severe(sender, e.getMessage());
        }

        return true;
    }
}
