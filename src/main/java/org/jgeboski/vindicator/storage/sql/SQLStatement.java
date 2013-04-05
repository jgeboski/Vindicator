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

package org.jgeboski.vindicator.storage.sql;

import java.util.Arrays;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLStatement
{
    private Connection        connection;
    private PreparedStatement pstatement;
    private int               autogenkeys;

    private String sql;
    private Object args[];

    public SQLStatement(Connection connection, boolean autogenkeys)
    {
        this.connection  = connection;
        this.pstatement  = null;

        if (autogenkeys)
            this.autogenkeys = Statement.RETURN_GENERATED_KEYS;
        else
            this.autogenkeys = Statement.NO_GENERATED_KEYS;
    }

    private void prepareStatement()
        throws SQLException
    {
        if (sql == null)
            return;

        close();

        pstatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        for (int i = 0; i < args.length; i++)
            pstatement.setObject(i + 1, args[i]);
    }

    public void store(Object ... args)
    {
        int i;

        this.sql  = new String();
        this.args = new Object[0];

        if (args.length < 1)
            return;

        sql += (String) args[0];

        for (i = 1; (i < args.length) && (args[i] != null); i++)
            sql += " " + (String) args[i];

        if (sql.length() < 1) {
            sql = null;
            return;
        }

        if (!sql.endsWith(";"))
            sql += ";";

        i++;

        if (i < args.length)
            this.args = Arrays.copyOfRange(args, i, args.length);
    }

    public boolean execute()
        throws SQLException
    {
        prepareStatement();

        if (pstatement == null)
            return false;

        return pstatement.execute();
    }

    public ResultSet executeQuery()
        throws SQLException
    {
        prepareStatement();

        if (pstatement == null)
            return null;

        return pstatement.executeQuery();
    }

    public int executeUpdate()
        throws SQLException
    {
        prepareStatement();

        if (pstatement == null)
            return 0;

        return pstatement.executeUpdate();
    }

    public void close()
    {
        if (pstatement == null)
            return;

        try {
            pstatement.close();
        } catch (SQLException e) { }

        pstatement = null;
    }

    public ResultSet getGeneratedKeys()
        throws SQLException
    {
        if (pstatement == null)
            return null;

        return pstatement.getGeneratedKeys();
    }
}
