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

import org.jgeboski.vindicator.storage.TargetObject;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.VindicatorAPI;

public class RBan extends RObject implements Runnable
{
    public RBan(VindicatorAPI api, CommandSender sender, String target,
                String message)
    {
        super(api, sender, target, message);
        addFlag(RObject.BAN);
    }

    public void run()
    {
        TargetObject[] tos;

        tos = getTargets(target);

        if (tos == null)
            return;

        for (TargetObject to : tos) {
            if (!to.hasFlag(TargetObject.BAN))
                continue;

            Message.severe(sender, "Ban already exists for %s", target);
            return;
        }

        if (!add(this))
            return;

        broadcast("vindicator.message.ban",
                  "Banned placed for %s by %s: %s",
                  target, issuer, message);

        if (timeout > 0) {
            broadcast("vindicator.message.ban",
                      "Temporary ban will be removed: %s",
                      Utils.timestr("EEE, MMM d 'at' h:m a z", timeout));
        }
    }
}
