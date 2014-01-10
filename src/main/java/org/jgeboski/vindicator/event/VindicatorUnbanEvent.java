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

public class VindicatorUnbanEvent extends VindicatorEvent
{
    public StorageRecord record;

    public VindicatorUnbanEvent(StorageRecord recd, CommandSender sender)
    {
        super(sender, true);
        this.record = recd;
    }

    public void task()
        throws VindicatorException
    {
        StorageRecord brecd;

        record.validate(storage, StorageRecord.BAN, vind.config.autoComplete);
        brecd = null;

        for (StorageRecord r : storage.getRecords(record.target)) {
            if (!r.hasFlag(StorageRecord.BAN))
                continue;

            brecd = r;
            break;
        }

        if (brecd == null) {
            throw new VindicatorException("Ban for %s not found.",
                                          hl(record.target.alias));
        }

        if (!eventContinue())
            return;

        storage.remove(brecd);
        vind.broadcast("vindicator.message.unban",
                       "Ban removed from %s by %s.",
                       hl(brecd.target.alias), hl(record.issuer.alias));

        if (!vind.config.unbanNote)
            return;

        brecd.target  = record.target;
        brecd.issuer  = record.issuer;
        brecd.message = "Unbanned: " + brecd.message;

        vind.execute(new VindicatorNoteAddEvent(brecd, sender));
    }
}
