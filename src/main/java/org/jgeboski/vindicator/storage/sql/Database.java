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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database
{
    private SQLType type;
    private String  prefix;

    private Connection       connection;
    private DatabaseMetaData databasemd;

    public Database(String url, String username, String password, String prefix)
        throws SQLException
    {
        this.prefix = prefix;
        this.type   = SQLType.fromURL(url);

        if(type == null)
            throw new SQLException("No support found for " + url);

        if(type == SQLType.SQLITE) {
            try {
                Class.forName("org.sqlite.JDBC").newInstance();
            } catch(Exception e) {
                System.out.println(e.getMessage());
                return;
            }
        }

        connection = DriverManager.getConnection(url, username, password);
        databasemd = connection.getMetaData();
    }

    public Database(String url)
        throws SQLException
    {
        this(url, null, null, null);
    }

    public void close()
    {
        try {
            connection.close();
        } catch(SQLException e) { }
    }

    public SQLType getType()
    {
        return type;
    }

    public boolean hasTable(String table)
    {
        ResultSet rs;
        boolean   ret;

        try {
            rs  = databasemd.getTables(null, null, table, null);
            ret = rs.next();
            rs.close();
        } catch(SQLException e) {
            ret = false;
        }

        return ret;
    }

    public SQLStatement createStatement()
    {
        return new SQLStatement(connection);
    }
}
