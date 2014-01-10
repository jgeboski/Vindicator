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
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.VindicatorException;

import static org.jgeboski.vindicator.util.Message.hl;

public class VindicatorMuteEvent extends VindicatorEvent
{
    public StorageRecord record;

    public VindicatorMuteEvent(StorageRecord recd, CommandSender sender)
    {
        super(sender, true);
        this.record = recd;
    }

    public void task()
        throws VindicatorException
    {
        StorageRecord mrecd;
        String        str;

        if ((record.message == null) || (record.message.length() < 1)) {
            if (vind.config.mustReason)
                throw new VindicatorException("A reason must be provided.");

            record.message = vind.config.defMuteReason;
        }

        record.validate(storage, StorageRecord.MUTE, vind.config.autoComplete);

        mrecd = null;

        for (StorageRecord r : storage.getRecords(record)) {
            if (!r.hasFlag(StorageRecord.MUTE))
                continue;

            mrecd = r;
            break;
        }

        if ((mrecd != null) && !vind.config.muteUpdate) {
            throw new VindicatorException("Mute already exists on %s.",
                                          hl(record.target.alias));
        }

        if (record.timeout > 0)
            record.timeout += Utils.time();

        if (!eventContinue())
            return;

        if (mrecd == null) {
            str = "placed";
            storage.add(record);
        } else {
            str = "updated";

            record.id = mrecd.id;
            storage.update(record);
        }

        vind.mutes.put(record.target.ident, record);
        vind.broadcast("vindicator.message.mute",
                       "Mute %s on %s by %s: %s",
                       str, hl(record.target.alias), hl(record.issuer.alias),
                       hl(record.message));

        if (record.timeout < 1)
            return;

        vind.broadcast("vindicator.message.mute",
                       "Temporary mute will be removed: %s",
                       hl(Utils.timestr(Utils.DATEF_LONG, record.timeout)));
    }
}
