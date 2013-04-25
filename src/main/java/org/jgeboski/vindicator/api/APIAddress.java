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
import org.bukkit.command.CommandSender;
import org.jgeboski.vindicator.util.Utils;

public class APIAddress extends APITask<APIAddress>
{
    public int    id;
    public String player;
    public String address;
    public int    logins;
    public long   time;

    public APIAddress(APIRunnable arun, String player, String address)
    {
        super(APIAddress.class, arun);
        init(player, address);
    }

    public APIAddress()
    {
        super(APIAddress.class, null);
        init(null, null);
    }

    private void init(String player, String address)
    {
        this.id      = 0;
        this.player  = player;
        this.address = address;
        this.logins  = 1;
        this.time    = Utils.time();
    }

    public void setObject(APIAddress aa)
    {
        Object v;

        for (Field f : aa.getClass().getDeclaredFields()) {
            try {
                v = f.get(aa);
                f.set(this, v);
            } catch (Exception e) { }
        }
    }
}
