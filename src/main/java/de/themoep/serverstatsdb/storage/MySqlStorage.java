package de.themoep.serverstatsdb.storage;

import de.themoep.serverstatsdb.ServerStatsDB;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Copyright 2016 Max Lee (https://github.com/Phoenix616/)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License as published by
 * the Mozilla Foundation, version 2.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v2.0 for more details.
 * <p/>
 * You should have received a copy of the Mozilla Public License v2.0
 * along with this program. If not, see <http://mozilla.org/MPL/2.0/>.
 */
public class MySqlStorage implements Storage {

    private final ConfigurationSection config;
    private Connection conn = null;
    private String table;

    public MySqlStorage(ConfigurationSection config) throws SQLException {
        this.config = config;
        table = config.getString("table");

        Statement stmt = getConn().createStatement();
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS `" + table + "` ("
                        + "timestamp TIMESTAMP CURRENT_TIMESTAMP, "
                        + "playercount INT NOT NULL, "
                        + "tps DOUBLE NOT NULL "
                        + ");"
        );
        stmt.close();
    }

    @Override
    public void log(int playerCount, double tps) throws Exception {
        PreparedStatement stmt = getConn().prepareStatement(
                "INSERT INTO `" + table + "` "
                        + "(playercount, tps) "
                        + "VALUES (?, ?)"
        );
        stmt.setInt(1, playerCount);
        stmt.setDouble(2, tps);
    }

    public Connection getConn() throws SQLException {
        if(conn == null || conn.isClosed() || !conn.isValid(10)) {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://" + config.getString("host")
                            + ":" + config.getString("port")
                            + "/" + config.getString("database")
                            + "?user=" + config.getString("user")
                            + "&password=" + config.getString("pass")
            );
        }
        return conn;
    }

    @Override
    public void close() throws Exception {
        if(conn != null) {
            conn.close();
        }
    }
}
