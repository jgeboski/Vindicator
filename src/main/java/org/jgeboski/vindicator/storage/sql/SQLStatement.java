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

package org.jgeboski.vindicator.storage.sql;

import java.util.Arrays;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLStatement
{
    private Connection connection;

    private String sql;
    private Object args[];

    private enum Type
    {
        DEFAULT,
        QUERY,
        UPDATE
    }

    public SQLStatement(Connection connection)
    {
        this.connection = connection;
    }

    private Object prepareStatement(Type type)
        throws SQLException
    {
        PreparedStatement pstmt;
        Object            ret;

        if(sql == null)
            return null;

        ret   = null;
        pstmt = connection.prepareStatement(sql);

        for(int i = 0; i < args.length; i++)
            pstmt.setObject(i + 1, args[i]);

        switch(type) {
        case DEFAULT: ret = pstmt.execute();       break;
        case QUERY:   ret = pstmt.executeQuery();  break;
        case UPDATE:  ret = pstmt.executeUpdate(); break;
        }

        pstmt.close();
        return ret;
    }

    public void store(Object ... args)
    {
        int i;

        this.sql  = new String();
        this.args = new Object[0];

        if(args.length < 1)
            return;

        sql += (String) args[0];

        for(i = 1; (i < args.length) && (args[i] != null); i++)
            sql += " " + (String) args[i];

        if(sql.length() < 1) {
            sql = null;
            return;
        }

        if(!sql.endsWith(";"))
            sql += ";";

        i++;

        if(i < args.length)
            this.args = Arrays.copyOfRange(args, i, args.length);
    }

    public boolean execute()
        throws SQLException
    {
        return (Boolean) prepareStatement(Type.DEFAULT);
    }

    public ResultSet executeQuery(Object ... args)
        throws SQLException
    {
        return (ResultSet) prepareStatement(Type.QUERY);
    }

    public int executeUpdate(Object ... args)
        throws SQLException
    {
        return (Integer) prepareStatement(Type.UPDATE);
    }
}
