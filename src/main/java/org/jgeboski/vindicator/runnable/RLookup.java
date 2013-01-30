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

package org.jgeboski.vindicator.runnable;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import org.jgeboski.vindicator.storage.TargetObject;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.VindicatorAPI;

public class RLookup extends RObject implements Runnable
{
    public RLookup(VindicatorAPI api, CommandSender sender, String target)
    {
        super(api, sender, target);
    }

    public void run()
    {
        ArrayList<TargetObject> tmp;
        TargetObject[]          tos;
        String type;

        int m;
        int b;
        int n;

        tos = getTargets(target);

        if (tos == null)
            return;

        tmp = new ArrayList<TargetObject>();
        m   = b = n = 0;

        for (TargetObject to : getTargets(target)) {
            if (to.hasFlag(TargetObject.BAN)) {
                tmp.add(m + b, to);
                b++;
            } else if (to.hasFlag(TargetObject.NOTE)) {
                to.setId(n + 1);
                tmp.add(b + n, to);
                n++;
            } else if (to.hasFlag(TargetObject.MUTE)) {
                tmp.add(m, to);
                m++;
            }
        }

        tos = tmp.toArray(new TargetObject[0]);
        Message.info(sender, "The records for %s:", target);

        if (tos.length < 1) {
            Message.info(sender, "There are no records for %s", target);
            return;
        }

        for (TargetObject to : tos) {
            type = to.hasFlag(TargetObject.IP) ? "IP" : "Player";

            if (to.hasFlag(TargetObject.BAN)) {
                Message.info(sender, "%s[%s] %s Ban (by: %s): %s",
                             ChatColor.RED, Utils.timestr(to.getTime()), type,
                             to.getIssuer(), to.getMessage());
            } else if (to.hasFlag(TargetObject.NOTE)) {
                Message.info(sender, "%s[%s] %s Note #%d (by: %s): %s",
                             ChatColor.YELLOW, Utils.timestr(to.getTime()),
                             type, to.getId(), to.getIssuer(), to.getMessage());
            } else if (to.hasFlag(TargetObject.MUTE)) {
                Message.info(sender, "%s[%s] Muted (by: %s): %s",
                             ChatColor.RED, Utils.timestr(to.getTime()),
                             to.getIssuer(), to.getMessage());
            }
        }
    }
}
