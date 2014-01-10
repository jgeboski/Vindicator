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

package org.jgeboski.vindicator.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jgeboski.vindicator.util.StrUtils;

public class SQLStatement
{
    private Connection        connection;
    private PreparedStatement pstatement;
    private int               autogenkeys;

    private String       query;
    private List<Object> parameters;

    public SQLStatement(Connection connection, boolean autogenkeys)
    {
        this.connection  = connection;
        this.pstatement  = null;
        this.autogenkeys = Statement.NO_GENERATED_KEYS;
        this.query       = new String();
        this.parameters  = new ArrayList<Object>();

        if (autogenkeys)
            this.autogenkeys = Statement.RETURN_GENERATED_KEYS;
    }

    public void close()
    {
        query = new String();
        parameters.clear();

        if (pstatement == null)
            return;

        try {
            pstatement.close();
        } catch (SQLException e) { }

        pstatement = null;
    }

    public void query(String ... args)
    {
        if (args.length < 1)
            return;

        if (query.length() > 0)
            query += " ";

        query += StrUtils.join(args, " ");
    }

    public void param(Object ... args)
    {
        for (Object a : args)
            parameters.add(a);
    }

    public boolean execute()
        throws SQLException
    {
        if (!prepareStatement())
            return false;

        return pstatement.execute();
    }

    public ResultSet executeQuery()
        throws SQLException
    {
        if (!prepareStatement())
            return null;

        return pstatement.executeQuery();
    }

    public int executeUpdate()
        throws SQLException
    {
        if (!prepareStatement())
            return 0;

        return pstatement.executeUpdate();
    }

    public ResultSet getGeneratedKeys()
        throws SQLException
    {
        if (pstatement == null)
            return null;

        return pstatement.getGeneratedKeys();
    }

    private boolean prepareStatement()
        throws SQLException
    {
        int i;

        if (query == null)
            return false;

        if (!query.endsWith(";"))
            query += ";";

        pstatement = connection.prepareStatement(query, autogenkeys);

        for (i = 0; i < parameters.size(); i++)
            pstatement.setObject(i + 1, parameters.get(i));

        return (pstatement != null);
    }
}
