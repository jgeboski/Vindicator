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

public class VindicatorNoteRemEvent extends VindicatorEvent
{
    public StorageRecord record;

    public VindicatorNoteRemEvent(StorageRecord recd, CommandSender sender)
    {
        super(sender, true);
        this.record = recd;
    }

    public void task()
        throws VindicatorException
    {
        StorageRecord nrecd;
        int i;

        nrecd = null;
        i     = 1;

        record.validate(storage, StorageRecord.NOTE, vind.config.autoComplete);

        for (StorageRecord r : storage.getRecords(record.target)) {
            if (!r.hasFlag(StorageRecord.NOTE))
                continue;

            if (i == record.id) {
                nrecd = r;
                break;
            }

            i++;
        }

        if (nrecd == null) {
            throw new VindicatorException("Note index %s not found.",
                                          hl(record.target.alias));
        }

        if (!eventContinue())
            return;

        storage.remove(nrecd);
        vind.broadcast("vindicator.message.noterem",
                       "Note removed from %s by %s.",
                       hl(nrecd.target.alias), hl(record.issuer.alias));
    }
}
