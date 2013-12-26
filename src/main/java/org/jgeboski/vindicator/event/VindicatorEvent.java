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
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import org.jgeboski.vindicator.storage.Storage;
import org.jgeboski.vindicator.storage.StorageAddress;
import org.jgeboski.vindicator.storage.StorageEntity;
import org.jgeboski.vindicator.storage.StorageException;
import org.jgeboski.vindicator.storage.StoragePlayer;
import org.jgeboski.vindicator.util.Kick;
import org.jgeboski.vindicator.util.Log;
import org.jgeboski.vindicator.util.Message;
import org.jgeboski.vindicator.Vindicator;
import org.jgeboski.vindicator.VindicatorException;

public class VindicatorEvent extends Event implements Cancellable, Runnable
{
    public Vindicator          vind;
    public Storage             storage;
    public CommandSender       sender;
    public VindicatorException exception;

    private HandlerList handlers;
    private boolean     cancelled;
    private boolean     called;

    protected VindicatorEvent(CommandSender sender, boolean async)
    {
        super(async);

        this.vind      = null;
        this.storage   = null;
        this.sender    = sender;
        this.exception = null;
        this.handlers  = new HandlerList();
        this.cancelled = false;
        this.called    = false;
    }

    protected VindicatorEvent(CommandSender sender)
    {
        this(sender, false);
    }

    protected VindicatorEvent(boolean async)
    {
        this(null, async);
    }

    protected VindicatorEvent()
    {
        this(null, false);
    }

    public void task()
        throws VindicatorException
    { }

    public void execute()
        throws VindicatorException
    {
        try {
            task();
            eventContinue();
        } catch (VindicatorException e) {
            exception = e;
            eventContinue();

            if (!(e instanceof StorageException))
                throw e;

            Log.severe(e.getMessage());
            throw new VindicatorException("An internal error occurred! " +
                                          "Notify the administrator.");
        }
    }

    public void run()
    {
        try {
            execute();
        } catch (VindicatorException e) {
            if (sender != null)
                Message.severe(sender, e.getMessage());
        }
    }

    public boolean eventContinue()
    {
        if (!called) {
            called = !called;
            vind.getServer().getPluginManager().callEvent(this);
        }

        return !cancelled;
    }

    public boolean kick(StorageEntity entity, String format, String ... args)
    {
        String str;

        str = Message.format(format, args);

        if (entity instanceof StorageAddress)
            return Kick.address(vind, entity.ident, str);

        if (entity instanceof StoragePlayer)
            return Kick.player(vind, entity.ident, str);

        return false;
    }

    public HandlerList getHandlers()
    {
        return handlers;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    public void setCancelled(boolean cancel)
    {
        cancelled = cancel;
    }
}
