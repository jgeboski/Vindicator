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

package org.jgeboski.Vindicator.api;

public class Ban extends TargetObject
{
    public Ban(String target, String issuer, String message, long timeout)
    {
        super(target, issuer, message, timeout, TargetObject.PLAYER_BAN);
    }

    public Ban(String target, String issuer, String message)
    {
        super(target, issuer, message, 0, TargetObject.PLAYER_BAN);
    }
}
