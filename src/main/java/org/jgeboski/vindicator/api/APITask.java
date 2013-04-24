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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.command.CommandSender;

public class APITask<T> implements Runnable
{
    public Class<T>      type;
    public APIRunnable   task;
    public CommandSender sender;

    public Object hObject;
    public Method hMethod;

    public APITask(Class<T> type, APIRunnable task, CommandSender sender)
    {
        this.type    = type;
        this.task    = task;
        this.sender  = sender;
        this.hObject = null;
        this.hMethod = null;
    }

    public APITask(Class<T> type, APIRunnable task)
    {
        this(type, task, null);
    }

    public void run()
    {
        Object       ret;
        APIException expt;
        Throwable    thab;

        Class  c;
        Class  r;
        Method m;

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

        if (task == null)
            return;

        c = task.getClass();

        if (ret == null) {
            try {
                m = c.getMethod("run", type, APIException.class);
                m.invoke(task, this, expt);
            } catch (Exception e) { }
            return;
        }

        for (r = ret.getClass(); r != null; r = r.getSuperclass()) {
            try {
                m = c.getMethod("run", type, r, APIException.class);
                m.invoke(task, this, ret, expt);
                return;
            } catch (Exception e) { }

            for (Class i : r.getInterfaces()) {
                try {
                    m = c.getMethod("run", type, i, APIException.class);
                    m.invoke(task, this, ret, expt);
                    return;
                } catch (Exception e) { }
            }
        }
    }

    public void setHandler(Object obj, String method)
    {
        try {
            hMethod = obj.getClass().getMethod(method, type);
            hObject = obj;
        } catch (Exception e) { }
    }
}
