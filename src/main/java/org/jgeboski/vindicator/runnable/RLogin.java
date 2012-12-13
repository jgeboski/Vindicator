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

import java.net.InetAddress;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import org.jgeboski.vindicator.storage.TargetObject;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.util.Utils;
import org.jgeboski.vindicator.VindicatorAPI;

public class RLogin extends RObject implements Runnable
{
    protected AsyncPlayerPreLoginEvent event;
    protected String address;

    private TargetObject ban;
    private TargetObject mute;
    private int          notes;

    public RLogin(VindicatorAPI api, AsyncPlayerPreLoginEvent event)
    {
        super(api, event.getName());

        this.event   = event;
        this.address = event.getAddress().getHostAddress();
    }

    public void run()
    {
        String msg;

        mute  = ban = null;
        notes = 0;

        checkIP();

        if (ban != null) {
            msg = ban.getMessage();

            broadcast("vindicator.message.notify",
                      "Player %s attempted to join with a banned IP: %s",
                      target, msg);

            event.disallow(Result.KICK_OTHER, "Banned: " + msg);
            return;
        }

        checkPlayer();

        if (ban != null) {
            msg = ban.getMessage();

            broadcast("vindicator.message.notify",
                      "Player %s attempted to join banned: %s",
                      target, ban.getMessage());

            event.disallow(Result.KICK_OTHER, "Banned: " + msg);
            return;
        }

        if ((notes < 1) && (mute == null))
            return;

        msg = String.format("%s has %d note(s)", target, notes);

        if (mute != null)
            msg += ", and is muted";

        broadcast("vindicator.message.notify", msg);
    }

    private void checkIP()
    {
        TargetObject[] tos;

        tos = getTargets(address);

        if (tos == null)
            return;

        for (TargetObject to : tos) {
            if (to.hasFlag(TargetObject.BAN)) {
                ban = to;
                return;
            } else if (to.hasFlag(TargetObject.NOTE)) {
                notes++;
            }
        }
    }

    private void checkPlayer()
    {
        TargetObject[] tos;

        tos = getTargets(target);

        if (tos == null)
            return;

        for (TargetObject to : tos) {
            if (to.hasFlag(TargetObject.BAN)) {
                System.out.println("HAS A BAN");
                ban = to;
                return;
            } else if (to.hasFlag(TargetObject.MUTE)) {
                mute = to;
            } else if (to.hasFlag(TargetObject.NOTE)) {
                notes++;
            }
        }
    }
}
