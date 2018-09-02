package de.themoep.serverstatsdb.storage;

import com.zaxxer.hikari.HikariDataSource;
import de.themoep.serverstatsdb.ServerStatsDB;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

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
    private HikariDataSource ds;
    private String table;

    public MySqlStorage(ServerStatsDB plugin, ConfigurationSection config) throws SQLException {
        this.config = config;
        table = config.getString("table");

        ds = new HikariDataSource();
        String dataSourceClassName = tryDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        if (dataSourceClassName == null) {
            dataSourceClassName = tryDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        }
        if (dataSourceClassName != null) {
            plugin.getLogger().log(Level.INFO, "Using " + dataSourceClassName + " database source");
            ds.setDataSourceClassName(dataSourceClassName);
        }

        if (dataSourceClassName == null) {
            String driverClassName = tryDriverClassName("org.mariadb.jdbc.Driver");
            if (driverClassName == null) {
                driverClassName = tryDriverClassName("com.mysql.cj.jdbc.Driver");
            }
            if (driverClassName == null) {
                driverClassName = tryDriverClassName("com.mysql.jdbc.Driver");
            }

            if (driverClassName != null) {
                plugin.getLogger().log(Level.INFO, "Using " + driverClassName + " database driver");
                ds.setDriverClassName(driverClassName);
            } else {
                throw new RuntimeException("Could not find database driver or data source class! Plugin wont work without a database!");
            }
        }

        ds.setJdbcUrl("jdbc:mysql://" + config.getString("host") + ":" + config.getString("port") + "/" +  config.getString("database") + config.getString("url-parameters"));
        ds.setUsername(config.getString("user"));
        ds.setPassword(config.getString("pass"));
        ds.setConnectionTimeout(5000);

        try (Connection conn = getConn(); Statement stmt = conn.createStatement()) {
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS `" + table + "` ("
                            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                            + "playercount INT NOT NULL, "
                            + "tps DOUBLE NOT NULL, "
                            + "playerlist VARCHAR(10240) NOT NULL DEFAULT '[]'"
                            + ");"
            );
            stmt.execute("SHOW COLUMNS FROM `" + table + "` LIKE 'playerlist';");
            ResultSet rs = stmt.getResultSet();
            if (!rs.next()) {
                stmt.execute("ALTER TABLE `" + table + "` ADD COLUMN playerlist VARCHAR(10240) NOT NULL DEFAULT '[]';");
            }
        }
    }

    private String tryDriverClassName(String className) {
        try {
            Class.forName(className).newInstance();
            return className;
        } catch (Exception ignored) {}
        return null;
    }

    private String tryDataSourceClassName(String className) {
        try {
            Class.forName(className);
            return className;
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public void log(int playerCount, double tps, String playerIds) throws Exception {
        String query = "INSERT INTO `" + table + "` "
                + "(playercount, tps, playerlist) "
                + "VALUES (?, ?, ?)";
        try (Connection conn = getConn(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, playerCount);
            stmt.setDouble(2, tps);
            stmt.setString(3, playerIds);
            stmt.execute();
        }
    }

    public Connection getConn() throws SQLException {
        return ds.getConnection();
    }

    @Override
    public void close() throws Exception {
        ds.close();
    }
}
