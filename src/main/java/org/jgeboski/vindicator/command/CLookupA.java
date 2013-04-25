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

import org.jgeboski.vindicator.api.APIAddress;
import org.jgeboski.vindicator.api.APIException;
import org.jgeboski.vindicator.api.APIRunnable;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.StrUtils;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.Vindicator;

import static org.jgeboski.vindicator.util.Message.hl;

public class CLookupA extends APIRunnable implements CommandExecutor
{
    public Vindicator vind;

    public CLookupA(Vindicator vind)
    {
        this.vind = vind;
    }

    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args)
    {
        APIAddress aa;
        String     target;

        if (!Utils.hasPermission(sender, "vindicator.lookupa"))
            return true;

        if (args.length < 1) {
            Message.info(sender, command.getUsage());
            return true;
        }

        target = sender.getName();

        if (StrUtils.isMinecraftName(args[0])) {
            aa = new APIAddress(this, sender, args[0], null);
        } else if (StrUtils.isAddress(args[0])) {
            aa = new APIAddress(this, sender, null, args[0]);
        } else {
            Message.severe(sender, "Invalid player/address: %s.", hl(target));
            return true;
        }

        try {
            vind.api.lookupa(aa);
        } catch (APIException e) {
            Message.severe(sender, e.getMessage());
        }

        return true;
    }

    public void run(APIAddress aa, List<APIAddress> aas, APIException expt)
    {
        String target;
        String time;

        target = (aa.player != null) ? aa.player : aa.address;

        if (aas.size() < 1) {
            Message.info(aa.sender, "There are no addresses for %s.",
                         hl(target));
            return;
        }

        for (APIAddress a : aas) {
            time = Utils.timestr(Utils.DATEF_SHORT, a.time);

            if (aa.player != null) {
                Message.info(aa.sender, "[%s] %s logins via %s",
                             hl(time), hl(a.logins), hl(a.address));
            } else {
                Message.info(aa.sender, "[%s] %s logins from %s",
                             hl(time), hl(a.logins), hl(a.player));
            }
        }
    }
}
