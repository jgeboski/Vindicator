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

import org.jgeboski.vindicator.storage.StoragePlayer;
import org.jgeboski.vindicator.storage.StorageRecord;
import org.jgeboski.vindicator.util.Log;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.VindicatorException;

import static org.jgeboski.vindicator.util.Message.hl;

public class VindicatorCommandEvent extends VindicatorEvent
{
    public StoragePlayer player;
    public String        message;

    public VindicatorCommandEvent(StoragePlayer plyr, String message)
    {
        super(false);
        this.player  = plyr;
        this.message = message;
    }

    public void task()
        throws VindicatorException
    {
        StorageRecord mrecd;
        String        cmd;

        mrecd = vind.mutes.get(player.ident);

        if ((mrecd == null) || !eventContinue())
            return;

        cmd = message.split(" ", 2)[0].replaceAll("/", "").toLowerCase();

        if (!vind.config.muteCommands.contains(cmd))
            return;

        if ((mrecd.timeout < 1) || (mrecd.timeout > Utils.time())) {
            Log.info("Player %s attempted to speak muted: %s",
                     player.alias, message);
            throw new VindicatorException("You cannot speak while " +
                                          "being muted!");
        }

        mrecd.issuer = new StoragePlayer(Message.plugin);
        vind.queue(new VindicatorUnmuteEvent(mrecd, sender));
    }
}
