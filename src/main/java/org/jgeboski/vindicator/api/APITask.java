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

public class APITask<T> implements Runnable
{
    public Class<T>    type;
    public APIRunnable arun;

    public Object tObject;
    public Method tMethod;

    public APITask(Class<T> type, APIRunnable arun)
    {
        this.type    = type;
        this.arun    = arun;
        this.tObject = null;
        this.tMethod = null;
    }

    public void run()
    {
        Object       ret;
        APIException expt;
        Throwable    thab;

        Class  c;
        Class  r;
        Method m;

        if ((tObject == null) || (tMethod == null))
            return;

        ret  = null;
        expt = null;

        try {
            ret = tMethod.invoke(tObject, this);
        } catch (InvocationTargetException e) {
            thab = e.getCause();

            if (thab instanceof APIException)
                expt = (APIException) thab;
        } catch (Exception e) { }

        if (arun == null)
            return;

        c = arun.getClass();

        if (ret == null) {
            try {
                m = c.getMethod("run", type, APIException.class);
                m.invoke(arun, this, expt);
            } catch (Exception e) { }
            return;
        }

        for (r = ret.getClass(); r != null; r = r.getSuperclass()) {
            try {
                m = c.getMethod("run", type, r, APIException.class);
                m.invoke(arun, this, ret, expt);
                return;
            } catch (Exception e) { }

            for (Class i : r.getInterfaces()) {
                try {
                    m = c.getMethod("run", type, i, APIException.class);
                    m.invoke(arun, this, ret, expt);
                    return;
                } catch (Exception e) { }
            }
        }
    }

    public void setTask(Object obj, String method)
    {
        try {
            tMethod = obj.getClass().getDeclaredMethod(method, type);
            tObject = obj;

            if (!tMethod.isAccessible())
                tMethod.setAccessible(true);
        } catch (Exception e) { }
    }
}
