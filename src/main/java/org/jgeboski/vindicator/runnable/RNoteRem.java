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

package org.jgeboski.vindicator.runnable;

import org.bukkit.command.CommandSender;

import org.jgeboski.vindicator.storage.TargetObject;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.VindicatorAPI;

public class RNoteRem extends RObject implements Runnable
{
    public RNoteRem(VindicatorAPI api, CommandSender sender, String target,
                    int index)
    {
        super(api, sender, target);
        setId(index);
    }

    public void run()
    {
        TargetObject[] tos;
        String         perm;

        int i;
        int n;

        tos = getTargets(target);
        id--;

        if (tos == null)
            return;

        for (i = n = 0; (n < id) && (i < tos.length); i++) {
            if (tos[i].hasFlag(TargetObject.NOTE))
                n++;
        }

        if ((n != id) || (i >= tos.length)) {
            Message.severe(sender, "Note index %d not found", (id + 1));
            return;
        }

        perm = "vindicator.message.noterem";

        if (tos[i].hasFlag(TargetObject.PUBLIC))
            perm += ".public";

        if (!remove(tos[i]))
            return;

        broadcast(perm, "Note removed for %s by %s: %s",
                  tos[i].getTarget(), issuer, tos[i].getMessage());
    }
}
