package com.revolut.service;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

class ConnectionFactoryTest {

    @Test
    void getConnection() throws SQLException {
        try(Connection connection = new ConnectionFactory("org.h2.Driver", "jdbc:h2:mem:test").getConnection();
            Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("select 11;");
            rs.next();
            assertThat(rs.getInt(1), is(11));
        }
    }
}