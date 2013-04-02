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

package org.jgeboski.vindicator.storage;

import java.util.List;

public interface Storage
{
    public void close();

    public void add(TargetObject to)
        throws StorageException;

    public void remove(int id)
        throws StorageException;

    public void remove(TargetObject to)
        throws StorageException;

    public List<TargetObject> getTargets(String target)
        throws StorageException;

    public List<TargetObject> getTargets(TargetObject to)
        throws StorageException;
}
