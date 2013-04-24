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
import org.jgeboski.vindicator.api.APIRecord;
import org.jgeboski.vindicator.api.APIRunnable;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.Vindicator;

import static org.jgeboski.vindicator.util.Message.hl;

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
        APIRecord ar;

        if (!Utils.hasPermission(sender, "vindicator.lookup"))
            return true;

        if (args.length < 1) {
            Message.info(sender, command.getUsage());
            return true;
        }

        ar = new APIRecord(this, sender, args[0]);

        try {
            vind.api.lookup(ar);
        } catch (APIException e) {
            Message.severe(sender, e.getMessage());
        }

        return true;
    }

    public void run(APIRecord ar, List<APIRecord> ars, APIException expt)
    {
        String type;
        String time;

        if (ars.size() < 1) {
            Message.info(ar.sender, "There are no records for %s.",
                         hl(ar.target));
            return;
        }

        for (APIRecord r : ars) {
            type = r.hasFlag(APIRecord.ADDRESS) ? "Address" : "Player";
            time = Utils.timestr(Utils.DATEF_SHORT, r.time);

            if (r.hasFlag(APIRecord.BAN)) {
                Message.severe(ar.sender, "[%s] %s ban by %s: %s",
                               hl(time), type, hl(r.issuer), hl(r.message));

                if (r.timeout < 1)
                    continue;

                Message.severe(ar.sender, "Ban will be removed: %s",
                               hl(Utils.timestr(Utils.DATEF_LONG, r.timeout)));
            } else if (r.hasFlag(APIRecord.MUTE)) {
                Message.severe(ar.sender, "[%s] Mute by %s: %s",
                               hl(time), hl(r.issuer), hl(r.message));

                if (r.timeout < 1)
                    continue;

                Message.severe(ar.sender, "Mute will be removed: %s",
                               hl(Utils.timestr(Utils.DATEF_LONG, r.timeout)));
            } else if (r.hasFlag(APIRecord.NOTE)) {
                Message.warning(ar.sender, "[%s] %s note #%s by %s: %s",
                                hl(time), type, hl(r.id), hl(r.issuer),
                                hl(r.message));
            }
        }
    }
}
