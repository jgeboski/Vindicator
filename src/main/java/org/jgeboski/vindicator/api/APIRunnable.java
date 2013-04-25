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
import org.jgeboski.vindicator.util.Message;

public class APIRunnable
{
    public void run(APIAddress aa, List<APIAddress> aas, APIException expt)
    {

    }

    public void run(APIRecord ar, APIException expt)
    {
        if (expt != null)
            Message.severe(ar.sender, expt.getMessage());
    }

    public void run(APIRecord ar, List<APIRecord> ars, APIException expt)
    {

    }
}
