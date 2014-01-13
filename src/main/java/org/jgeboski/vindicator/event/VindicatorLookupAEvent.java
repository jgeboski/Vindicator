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

import java.util.List;

import org.bukkit.command.CommandSender;

import org.jgeboski.vindicator.storage.StorageAddress;
import org.jgeboski.vindicator.storage.StorageEntity;
import org.jgeboski.vindicator.storage.StorageLogin;
import org.jgeboski.vindicator.storage.StoragePlayer;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.VindicatorException;

import static org.jgeboski.vindicator.util.Message.hl;

public class VindicatorLookupAEvent extends VindicatorEvent
{
    public StorageEntity      entity;
    public List<StorageLogin> logins;

    public VindicatorLookupAEvent(StorageEntity entity, CommandSender sender)
    {
        super(sender, true);
        this.entity = entity;
    }

    public void task()
        throws VindicatorException
    {
        String time;
        String str;

        entity.validate(storage, vind.config.autoComplete);
        logins = storage.getLogins(entity);

        if (!eventContinue())
            return;

        if (logins.size() < 1) {
            Message.info(sender, "There are no logins for %s.",
                         hl(entity.alias));
            return;
        }

        if (!entity.ident.equalsIgnoreCase(entity.alias))
            str = String.format(" (%s)", hl(entity.ident));
        else
            str = new String();

        Message.info(sender, "Logins for %s%s:", hl(entity.alias), str);

        for (StorageLogin l : logins) {
            l.validate(storage, false);

            time = Utils.timestr(Utils.DATEF_SHORT, l.time);

            if (entity instanceof StorageAddress) {
                Message.info(sender, "[%s] %s logins from %s",
                             hl(time), hl(l.count), hl(l.player.alias));
            } else if (entity instanceof StoragePlayer) {
                Message.info(sender, "[%s] %s logins via %s",
                             hl(time), hl(l.count), hl(l.address.alias));
            }
        }
    }
}
