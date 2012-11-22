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

package org.jgeboski.vindicator.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.jgeboski.vindicator.api.VindicatorAPI;
import org.jgeboski.vindicator.api.APIException;
import org.jgeboski.vindicator.Message;
import org.jgeboski.vindicator.Vindicator;

public class CVindicator implements CommandExecutor
{
    protected Vindicator vind;

    public CVindicator(Vindicator vind)
    {
        this.vind = vind;
    }

    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args)
    {
        String cmd;

        if(!vind.hasPermissionM(sender, "vindicator.vindicator"))
            return true;

        if(args.length < 1) {
            Message.info(sender, command.getUsage());
            return true;
        }

        cmd = args[0].toLowerCase();

        if(cmd.matches("reload|rel"))
            reload(sender);
        else
            Message.info(sender, command.getUsage());

        return true;
    }

    private void reload(CommandSender sender)
    {
        if(!vind.hasPermissionM(sender, "vindicator.manage.reload"))
            return;

        vind.reload();
        Message.info(sender, "Configuration successfully reloaded");
    }
}
