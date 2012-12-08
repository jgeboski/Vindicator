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

package org.jgeboski.vindicator.runnable;

import org.bukkit.command.CommandSender;

import org.jgeboski.vindicator.exception.APIException;
import org.jgeboski.vindicator.storage.TargetObject;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.VindicatorAPI;

public class RUnban extends RObject implements Runnable
{
    public RUnban(VindicatorAPI api, CommandSender sender, String target)
    {
        super(api, sender, target);
    }

    public void run()
    {
        TargetObject[] tos;
        TargetObject   bt;

        tos = getTargets(target);
        bt  = null;

        if (tos == null)
            return;

        for (TargetObject to : tos) {
            if (!to.hasFlag(TargetObject.BAN))
                continue;

            bt = to;
            break;
        }

        if (bt == null) {
            Message.info(sender, "Ban for %s not found", target);
            return;
        }

        if (!remove(bt))
            return;

        broadcast("vindicator.message.unban",
                  "Ban removed for %s by %s: %s",
                  bt.getTarget(), issuer, bt.getMessage());

        if (!api.vind.config.unbanNote)
            return;

        try {
            api.noteAdd(sender, bt.getTarget(), "Unbanned: " + bt.getMessage(),
                        false);
        } catch (APIException e) {
            Message.severe(sender, e.getMessage());
        }
    }
}
