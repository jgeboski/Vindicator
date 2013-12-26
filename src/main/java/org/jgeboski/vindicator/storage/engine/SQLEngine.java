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

package org.jgeboski.vindicator.storage.engine;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jgeboski.vindicator.storage.Storage;
import org.jgeboski.vindicator.storage.StorageAddress;
import org.jgeboski.vindicator.storage.StorageException;
import org.jgeboski.vindicator.storage.StorageLogin;
import org.jgeboski.vindicator.storage.StoragePlayer;
import org.jgeboski.vindicator.storage.StorageRecord;
import org.jgeboski.vindicator.util.sql.Database;
import org.jgeboski.vindicator.util.sql.SQLStatement;
import org.jgeboski.vindicator.util.sql.SQLType;
import org.jgeboski.vindicator.util.StrUtils;

public class SQLEngine extends Storage
{
    private String TABLE_ADDRESSES = "addresses";
    private String TABLE_RECORDS   = "records";

    private String url;
    private String username;
    private String password;
    private String prefix;

    private Database database;

    public SQLEngine(String url, String username, String password,
                     String prefix)
        throws StorageException
    {
        SQLStatement stmt;
        String       ainc;

        this.url      = url;
        this.username = username;
        this.password = password;
        this.prefix   = prefix;

        if (prefix != null) {
            TABLE_ADDRESSES = prefix + TABLE_ADDRESSES;
            TABLE_RECORDS   = prefix + TABLE_RECORDS;
        }

        try {
            database = new Database(url, username, password, prefix);
        } catch (SQLException e) {
            throw new StorageException(e);
        }

        stmt = database.createStatement();
        ainc = (database.getType() == SQLType.MYSQL) ? "AUTO_INCREMENT" : "";

        if (!database.hasTable(TABLE_ADDRESSES)) {
            stmt.store(
                "CREATE TABLE", TABLE_ADDRESSES, "(",
                    "id INTEGER PRIMARY KEY ", ainc, ",",
                    "player VARCHAR(16) NOT NULL,",
                    "address VARCHAR(45) NOT NULL,",
                    "logins SMALLINT(6) NOT NULL,",
                    "time INTEGER(11) NOT NULL",
                ")");

            try {
                stmt.executeUpdate();
                stmt.close();
            } catch (SQLException e) {
                stmt.close();
                throw new StorageException(e);
            }
        }

        if (!database.hasTable(TABLE_RECORDS)) {
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
                stmt.close();
                throw new StorageException(e);
            }
        }
    }

    public SQLEngine(String url)
        throws StorageException
    {
        this(url, null, null, null);
    }

    public void close()
    {
        if (database != null)
            database.close();
    }

    public void add(StorageLogin login)
        throws StorageException
    {
        SQLStatement stmt;

        stmt = database.createStatement();
        stmt.store(
            "INSERT INTO", TABLE_ADDRESSES,
                "(player, address, logins, time)",
              "VALUES (?, ?, ?, ?)", null,
            login.player.ident, login.address.ident, login.count, login.time);

        try {
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            stmt.close();
            throw new StorageException(e);
        }
    }

    public void add(StorageRecord recd)
        throws StorageException
    {
        SQLStatement stmt;

        stmt = database.createStatement();
        stmt.store(
            "INSERT INTO", TABLE_RECORDS,
                "(target, issuer, message, timeout, time, flags)",
              "VALUES (?, ?, ?, ?, ?, ?)", null,
            recd.target.ident, recd.issuer.ident, recd.message,
            recd.timeout, recd.time, recd.flags);

        try {
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            stmt.close();
            throw new StorageException(e);
        }
    }

    public void remove(StorageLogin login)
        throws StorageException
    {
        SQLStatement stmt;

        if (login.id < 1)
            return;

        stmt = database.createStatement();
        stmt.store(
            "DELETE FROM", TABLE_ADDRESSES,
              "WHERE id = ?", null,
            login.id);

        try {
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            stmt.close();
            throw new StorageException(e);
        }
    }

    public void remove(StorageRecord recd)
        throws StorageException
    {
        SQLStatement stmt;

        if (recd.id < 1)
            return;

        stmt = database.createStatement();
        stmt.store(
            "DELETE FROM", TABLE_RECORDS,
              "WHERE id = ?", null,
            recd.id);

        try {
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            stmt.close();
            throw new StorageException(e);
        }
    }

    public void update(StorageLogin login)
        throws StorageException
    {
        SQLStatement stmt;

        if (login.id < 1)
            return;

        stmt = database.createStatement();
        stmt.store(
            "UPDATE", TABLE_ADDRESSES, "SET",
                "player = ?,",
                "address = ?,",
                "logins = ?,",
                "time = ?",
              "WHERE id = ?", null,
            login.player.ident, login.address.ident,
            login.count, login.time, login.id);

        try {
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            stmt.close();
            throw new StorageException(e);
        }
    }

    public void update(StorageRecord recd)
        throws StorageException
    {
        SQLStatement stmt;

        if (recd.id < 1)
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
            recd.target.ident, recd.issuer.ident, recd.message,
            recd.timeout, recd.time, recd.flags, recd.id);

        try {
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            stmt.close();
            throw new StorageException(e);
        }
    }

    public StorageLogin getLogin(StorageLogin login)
        throws StorageException
    {
        List<StorageLogin> logins;
        SQLStatement stmt;

        stmt = database.createStatement();
        stmt.store(
            "SELECT * FROM", TABLE_ADDRESSES,
              "WHERE player = ? AND address = ?",
              "LIMIT 1", null,
            login.player.ident, login.address.ident);

        logins = getLogins(stmt);
        return (logins.size() > 0) ? logins.get(0) : null;
    }

    public List<StorageLogin> getLogins(StorageAddress addr)
        throws StorageException
    {
        SQLStatement stmt;

        stmt = database.createStatement();
        stmt.store(
            "SELECT * FROM", TABLE_ADDRESSES,
              "WHERE address = ?", null,
            addr.ident);

        return getLogins(stmt);
    }

    public List<StorageLogin> getLogins(StoragePlayer plyr)
        throws StorageException
    {
        SQLStatement stmt;

        stmt = database.createStatement();
        stmt.store(
            "SELECT * FROM", TABLE_ADDRESSES,
              "WHERE player = ?", null,
            plyr.ident);

        return getLogins(stmt);
    }

    public List<StorageRecord> getRecords(StorageAddress addr)
        throws StorageException
    {
        return getRecords(addr.ident);
    }

    public List<StorageRecord> getRecords(StoragePlayer plyr)
        throws StorageException
    {
        return getRecords(plyr.ident);
    }

    private List<StorageLogin> getLogins(SQLStatement stmt)
        throws StorageException
    {
        ArrayList<StorageLogin> logins;
        StorageLogin login;
        ResultSet    rs;
        String       name;
        String       addr;

        logins = new ArrayList<StorageLogin>();

        try {
            rs = stmt.executeQuery();

            while (rs.next()) {
                name = rs.getString("player");
                addr = rs.getString("address");

                login = new StorageLogin(name, addr);

                login.id    = rs.getInt("id");
                login.count = rs.getInt("logins");
                login.time  = rs.getLong("time");

                logins.add(login);
            }

            stmt.close();
        } catch (SQLException e) {
            stmt.close();
            throw new StorageException(e);
        }

        return logins;
    }

    private List<StorageRecord> getRecords(String target)
        throws StorageException
    {
        ArrayList<StorageRecord> recds;
        StorageRecord recd;
        SQLStatement  stmt;
        ResultSet     rs;
        String        issuer;

        recds = new ArrayList<StorageRecord>();
        stmt  = database.createStatement();

        stmt.store(
            "SELECT * FROM", TABLE_RECORDS,
              "WHERE target = ?", null,
            target);

        try {
            rs = stmt.executeQuery();

            while (rs.next()) {
                target = rs.getString("target");
                issuer = rs.getString("issuer");

                recd = new StorageRecord(target, issuer);

                recd.id      = rs.getInt("id");
                recd.message = rs.getString("message");
                recd.timeout = rs.getLong("timeout");
                recd.time    = rs.getLong("time");
                recd.flags   = rs.getInt("flags");

                recds.add(recd);
            }

            stmt.close();
        } catch (SQLException e) {
            stmt.close();
            throw new StorageException(e);
        }

        return recds;
    }
}
