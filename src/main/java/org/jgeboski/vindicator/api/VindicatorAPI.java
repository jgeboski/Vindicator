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

package org.jgeboski.vindicator.api;

public class VindicatorAPI
{
    /* Banning */

    public void ban(String issuer, String target, String reason)
        throws APIException
    {
        
    }

    public void banIP(String issuer, String target, String reason)
        throws APIException
    {
        
    }

    public void banPlayer(String issuer, String target, String reason)
        throws APIException
    {
        
    }


    /* Kicking */

    public void kick(String issuer, String target, String reason)
        throws APIException
    {
        
    }

    public void kickIP(String issuer, String target, String reason)
        throws APIException
    {
        
    }

    public void kickPlayer(String issuer, String target, String reason)
        throws APIException
    {
        
    }


    /* Lookups */

    public TargetObject[] lookup(String issuer, String target)
        throws APIException
    {
        return new TargetObject[0];
    }


    /* Note adding */

    public void noteAdd(String issuer, String target, String note, boolean pub)
        throws APIException
    {
        
    }

    public void noteAddIP(String issuer, String target, String note)
        throws APIException
    {
        
    }

    public void noteAddPlayer(String issuer, String target, String note)
        throws APIException
    {
        
    }


    /* Note remove */

    public void noteRem(String issuer, String target, int index)
        throws APIException
    {
        
    }


    /* Unbanning */

    public void unban(String issuer, String target)
        throws APIException
    {
        
    }
}
