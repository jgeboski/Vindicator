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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jgeboski.vindicator.storage.sql.Database;
import org.jgeboski.vindicator.storage.sql.SQLStatement;
import org.jgeboski.vindicator.storage.sql.SQLType;
import org.jgeboski.vindicator.util.StrUtils;

public class StorageSQL implements Storage
{
    private String TABLE_RECORDS = "records";

    private String url;
    private String username;
    private String password;
    private String prefix;

    private Database database;

    public StorageSQL(String url, String username, String password,
                      String prefix)
        throws StorageException
    {
        SQLStatement stmt;
        String       ainc;

        this.url      = url;
        this.username = username;
        this.password = password;
        this.prefix   = prefix;

        if (prefix != null)
            TABLE_RECORDS = prefix + TABLE_RECORDS;

        try {
            database = new Database(url, username, password, prefix);
        } catch (SQLException e) {
            throw new StorageException(e);
        }

        stmt = database.createStatement();
        ainc = (database.getType() == SQLType.MYSQL) ? "AUTO_INCREMENT" : "";

        if (database.hasTable(TABLE_RECORDS))
            return;

        stmt.store(
            "CREATE TABLE", TABLE_RECORDS, "(",
                "id INTEGER PRIMARY KEY ", ainc, ",",
                "target VARCHAR(45) NOT NULL,",
                "issuer VARCHAR(16) NOT NULL,",
                "message VARCHAR(255) NOT NULL,",
                "timeout INTEGER(11) NOT NULL,",
                "time INTEGER(11) NOT NULL,",
                "flags SMALLINT(6) NOT NULL",
            ")");

        try {
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    public StorageSQL(String url)
        throws StorageException
    {
        this(url, null, null, null);
    }

    public void close()
    {
        if (database != null)
            database.close();
    }

    public void add(TargetObject to)
        throws StorageException
    {
        SQLStatement stmt;

        stmt = database.createStatement();
        stmt.store(
            "INSERT INTO", TABLE_RECORDS,
                "(target, issuer, message, timeout, time, flags)",
              "VALUES (?, ?, ?, ?, ?, ?)", null,
            to.target, to.issuer, to.message, to.timeout, to.time, to.flags);

        try {
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    public void remove(int id)
        throws StorageException
    {
        SQLStatement stmt;

        if (id < 1)
            return;

        stmt = database.createStatement();
        stmt.store(
            "DELETE FROM", TABLE_RECORDS,
              "WHERE id = ?", null,
            id);

        try {
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    public void remove(TargetObject to)
        throws StorageException
    {
        remove(to.id);
    }

    public void update(TargetObject to)
        throws StorageException
    {
        SQLStatement stmt;

        if (to.id < 1)
            return;

        stmt = database.createStatement();
        stmt.store(
            "UPDATE", TABLE_RECORDS, "SET",
                "target = ?,",
                "issuer = ?,",
                "message = ?,",
                "timeout = ?,",
                "time = ?,",
                "flags = ?",
              "WHERE id = ?", null,
            to.target, to.issuer, to.message, to.timeout, to.time, to.flags,
            to.id);

        try {
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    public List<TargetObject> getRecords(String target)
        throws StorageException
    {
        ArrayList<TargetObject> ret;

        SQLStatement stmt;
        ResultSet    rs;
        TargetObject to;

        ret = new ArrayList<TargetObject>();

        if (target == null)
            return ret;

        stmt = database.createStatement();
        stmt.store(
            "SELECT * FROM", TABLE_RECORDS,
              "WHERE target = ?", null,
            target);

        try {
            rs = stmt.executeQuery();

            while (rs.next()) {
                to = new TargetObject();

                to.id      = rs.getInt("id");
                to.target  = rs.getString("target");
                to.issuer  = rs.getString("issuer");
                to.message = rs.getString("message");
                to.timeout = rs.getLong("timeout");
                to.time    = rs.getLong("time");
                to.flags   = rs.getInt("flags");

                ret.add(to);
            }

            stmt.close();
        } catch (SQLException e) {
            throw new StorageException(e);
        }

        return ret;
    }

    public List<TargetObject> getRecords(TargetObject to)
        throws StorageException
    {
        return getRecords(to.target);
    }
}
