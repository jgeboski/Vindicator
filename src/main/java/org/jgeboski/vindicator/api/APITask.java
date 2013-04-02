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

package org.jgeboski.vindicator.api;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.jgeboski.vindicator.storage.TargetObject;

public class APITask extends TargetObject implements Runnable
{
    public APIRunnable   task;
    public CommandSender sender;

    public Object hObject;
    public Method hMethod;

    public APITask(APIRunnable task, CommandSender sender, String target)
    {
        super();

        this.task    = task;
        this.sender  = sender;
        this.target  = target;
        this.hObject = null;
        this.hMethod = null;

        if (sender != null)
            this.issuer = sender.getName();
    }

    public APITask(APIRunnable task, String target)
    {
        this(task, null, target);
    }

    public void run()
    {
        Object       ret;
        APIException expt;
        Throwable    thab;

        if ((hObject == null) || (hMethod == null))
            return;

        ret  = null;
        expt = null;

        try {
            ret = hMethod.invoke(hObject, this);
        } catch (InvocationTargetException e) {
            thab = e.getCause();

            if (thab instanceof APIException)
                expt = (APIException) thab;
        } catch (Exception e) { }

        task.run(this, expt);

        if (ret instanceof List)
            task.run(this, (List<TargetObject>) ret, expt);
    }

    public void setHandler(Object obj, String method)
    {
        try {
            hMethod = obj.getClass().getMethod(method, APITask.class);
            hObject = obj;
        } catch (Exception e) { }
    }

    public void setTargetObject(TargetObject to)
    {
        Object v;

        for (Field f : to.getClass().getDeclaredFields()) {
            try {
                v = f.get(to);
                f.set(this, v);
            } catch (Exception e) { }
        }
    }
}
