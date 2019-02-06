package com.revolut.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private final String dbUrl;

    public ConnectionFactory(String jdbcDriver, String jdbcUrl) {
        this.dbUrl = jdbcUrl;
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver not found");
        }
    }

    public Connection getConnection() {

        try {
            return DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't connect to DB");
        }
    }
}
