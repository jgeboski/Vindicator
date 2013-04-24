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

import org.jgeboski.vindicator.api.APIRecord;
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

    public void add(APIRecord ar)
        throws StorageException
    {
        SQLStatement stmt;

        stmt = database.createStatement();
        stmt.store(
            "INSERT INTO", TABLE_RECORDS,
                "(target, issuer, message, timeout, time, flags)",
              "VALUES (?, ?, ?, ?, ?, ?)", null,
            ar.target, ar.issuer, ar.message, ar.timeout, ar.time, ar.flags);

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

    public void remove(APIRecord ar)
        throws StorageException
    {
        remove(ar.id);
    }

    public void update(APIRecord ar)
        throws StorageException
    {
        SQLStatement stmt;

        if (ar.id < 1)
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
            ar.target, ar.issuer, ar.message, ar.timeout, ar.time, ar.flags,
            ar.id);

        try {
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    public List<APIRecord> getRecords(String target)
        throws StorageException
    {
        ArrayList<APIRecord> ars;

        SQLStatement stmt;
        ResultSet    rs;
        APIRecord    ar;

        ars = new ArrayList<APIRecord>();

        if (target == null)
            return ars;

        stmt = database.createStatement();
        stmt.store(
            "SELECT * FROM", TABLE_RECORDS,
              "WHERE target = ?", null,
            target);

        try {
            rs = stmt.executeQuery();

            while (rs.next()) {
                ar = new APIRecord();

                ar.id      = rs.getInt("id");
                ar.target  = rs.getString("target");
                ar.issuer  = rs.getString("issuer");
                ar.message = rs.getString("message");
                ar.timeout = rs.getLong("timeout");
                ar.time    = rs.getLong("time");
                ar.flags   = rs.getInt("flags");

                ars.add(ar);
            }

            stmt.close();
        } catch (SQLException e) {
            throw new StorageException(e);
        }

        return ars;
    }

    public List<APIRecord> getRecords(APIRecord ar)
        throws StorageException
    {
        return getRecords(ar.target);
    }
}
