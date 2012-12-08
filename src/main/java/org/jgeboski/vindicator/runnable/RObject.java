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

import org.jgeboski.vindicator.exception.StorageException;
import org.jgeboski.vindicator.storage.TargetObject;
import org.jgeboski.vindicator.util.Log;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.VindicatorAPI;

public class RObject extends TargetObject
{
    protected VindicatorAPI api;
    protected CommandSender sender;

    public RObject(VindicatorAPI api, CommandSender sender, String target,
                     String message)
    {
        super(target, sender.getName(), message);

        this.api    = api;
        this.sender = sender;
    }

    public RObject(VindicatorAPI api, CommandSender sender, String target)
    {
        this(api, sender, target, null);
    }

    public RObject(VindicatorAPI api, CommandSender sender)
    {
        this(api, sender, null, null);
    }

    public CommandSender getSender()
    {
        return sender;
    }

    public void setSender(CommandSender sender)
    {
        this.sender = sender;
    }

    public void broadcast(String perm, String format, Object ... args)
    {
        api.vind.broadcast(perm, format, args);
    }

    public boolean add(TargetObject to)
    {
        try {
            api.storage.add(to);
            return true;
        } catch (StorageException e) {
            Message.severe(sender, "An error occurred! Check the server log!");
            Log.severe(e.getMessage());
        }

        return false;
    }

    public boolean remove(TargetObject to)
    {
        try {
            api.storage.remove(to);
            return true;
        } catch (StorageException e) {
            Message.severe(sender, "An error occurred! Check the server log!");
            Log.severe(e.getMessage());
        }

        return false;
    }

    public TargetObject[] getTargets(String target)
    {
        try {
            return api.storage.getTargets(target);
        } catch (StorageException e) {
            Message.severe(sender, "An error occurred! Check the server log!");
            Log.severe(e.getMessage());
        }

        return null;
    }
}
