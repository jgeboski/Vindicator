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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import org.jgeboski.vindicator.storage.StorageEntity;
import org.jgeboski.vindicator.storage.StorageRecord;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.VindicatorException;

import static org.jgeboski.vindicator.util.Message.hl;

public class VindicatorLookupEvent extends VindicatorEvent
{
    public StorageEntity       entity;
    public List<StorageRecord> records;

    public VindicatorLookupEvent(StorageEntity entity, CommandSender sender)
    {
        super(sender, true);
        this.entity = entity;
    }

    public void task()
        throws VindicatorException
    {
        String type;
        String time;
        String str;

        int m;
        int b;
        int n;

        records = new ArrayList<StorageRecord>();
        m = b = n = 0;

        entity.validate(storage, vind.config.autoComplete);

        for (StorageRecord r : storage.getRecords(entity)) {
            if (r.hasFlag(StorageRecord.BAN)) {
                records.add(b, r);
                b++;
            } else if (r.hasFlag(StorageRecord.NOTE)) {
                r.id = n + 1;
                records.add(b + m + n, r);
                n++;
            } else if (r.hasFlag(StorageRecord.MUTE)) {
                records.add(b + m, r);
                m++;
            }
        }

        if (!eventContinue())
            return;

        if (records.size() < 1) {
            Message.info(sender, "There are no records for %s.",
                         hl(entity.alias));
            return;
        }

        if (entity.ident != entity.alias)
            str = String.format(" (%s)", hl(entity.ident));
        else
            str = new String();

        Message.info(sender, "Records for %s%s:", hl(entity.alias), str);

        for (StorageRecord r : records) {
            r.validate(storage, false);

            type = r.hasFlag(StorageRecord.ADDRESS) ? "Address" : "Player";
            time = Utils.timestr(Utils.DATEF_SHORT, r.time);

            if (r.hasFlag(StorageRecord.BAN)) {
                Message.severe(sender, "[%s] %s ban by %s: %s",
                               hl(time), type, hl(r.issuer.alias),
                               hl(r.message));

                if (r.timeout < 1)
                    continue;

                Message.severe(sender, "Ban will be removed: %s",
                               hl(Utils.timestr(Utils.DATEF_LONG, r.timeout)));
            } else if (r.hasFlag(StorageRecord.MUTE)) {
                Message.severe(sender, "[%s] Mute by %s: %s",
                               hl(time), hl(r.issuer.alias), hl(r.message));

                if (r.timeout < 1)
                    continue;

                Message.severe(sender, "Mute will be removed: %s",
                               hl(Utils.timestr(Utils.DATEF_LONG, r.timeout)));
            } else if (r.hasFlag(StorageRecord.NOTE)) {
                Message.warning(sender, "[%s] %s note #%s by %s: %s",
                                hl(time), type, hl(r.id), hl(r.issuer.alias),
                                hl(r.message));
            }
        }
    }
}
