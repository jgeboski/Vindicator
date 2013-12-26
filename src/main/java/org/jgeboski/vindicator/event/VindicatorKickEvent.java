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

package org.jgeboski.vindicator.event;

import org.bukkit.command.CommandSender;

import org.jgeboski.vindicator.storage.StorageRecord;
import org.jgeboski.vindicator.VindicatorException;

import static org.jgeboski.vindicator.util.Message.hl;

public class VindicatorKickEvent extends VindicatorEvent
{
    public StorageRecord record;

    public VindicatorKickEvent(StorageRecord recd, CommandSender sender)
    {
        super(sender, true);
        this.record = recd;
    }

    public void task()
        throws VindicatorException
    {
        record.validate(vind.config.autoComplete);

        if ((record.message == null) || (record.message.length() < 1)) {
            if (vind.config.mustReason)
                throw new VindicatorException("A reason must be provided.");

            record.message = vind.config.defKickReason;
        }

        if (!eventContinue())
            return;

        if (!kick(record.target, hl(record.message))) {
            throw new VindicatorException("No player(s) found for %s.",
                                          hl(record.target.alias));
        }

        vind.broadcast("vindicator.message.kick",
                       "Kick placed on %s by %s: %s",
                       hl(record.target.alias), hl(record.issuer.alias),
                       hl(record.message));
    }
}
