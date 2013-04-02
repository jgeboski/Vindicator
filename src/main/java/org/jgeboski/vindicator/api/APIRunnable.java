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

import java.util.List;

import org.jgeboski.vindicator.storage.TargetObject;
import org.jgeboski.vindicator.util.Message;

public class APIRunnable
{
    public void run(APITask at, APIException expt)
    {
        if (expt != null)
            Message.severe(at.sender, expt.getMessage());
    }

    public void run(APITask at, List<TargetObject> tos, APIException expt)
    {

    }
}
