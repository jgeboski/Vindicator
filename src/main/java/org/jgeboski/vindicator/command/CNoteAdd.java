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

public class CNoteAdd implements CommandExecutor
{
    public Vindicator vind;

    public CNoteAdd(Vindicator vind)
    {
        this.vind = vind;
    }

    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args)
    {
        boolean pub;
        String  note;

        if (!Utils.hasPermission(sender, "vindicator.noteadd"))
            return true;

        if (args.length < 2) {
            Message.info(sender, command.getUsage());
            return true;
        }

        pub = false;

        if (args[0].startsWith("-")) {
            if (!args[0].equals("-p")) {
                Message.info(sender, command.getUsage());
                return true;
            }

            if (!Utils.hasPermission(sender, "vindicator.noteadd.public"))
                return true;

            pub = true;
        }

        note = StrUtils.strjoin(args, " ", 1);

        try {
            vind.api.noteAdd(sender, args[0], note, pub);
        } catch (APIException e) {
            Message.severe(sender, e.getMessage());
        }

        return true;
    }
}
