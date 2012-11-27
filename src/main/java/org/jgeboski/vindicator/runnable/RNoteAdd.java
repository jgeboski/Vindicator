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

import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.VindicatorAPI;

public class RNoteAdd extends RObject implements Runnable
{
    public RNoteAdd(VindicatorAPI api, CommandSender sender, String target,
                    String message)
    {
        super(api, sender, target, message);
        addFlag(RObject.NOTE);
    }

    public void run()
    {
        String perm;

        perm = "vindicator.message.noteadd";

        if(hasFlag(RObject.PUBLIC))
            perm += ".public";

        if(!add(this))
            return;

        broadcast(perm, "Note added for %s by %s: %s", target, issuer, message);
    }
}
