package com.revolut;


import com.revolut.service.AccountDaoImpl;
import com.revolut.service.ConnectionFactory;
import com.revolut.service.AccountServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final String DEFAULT_JDBC_URL = "jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1";
    private static final String DEFAULT_JDBC_DRIVER = "org.h2.Driver";
    private static final String DEFAULT_BASE_PATH = "/api";
    private static final int DEFAULT_PORT = 8313;

    private final String jdbcUrl;
    private final String jdbcDriver;
    private final String basePath;
    private final int port;

    public Application(int port, String basePath, String jdbcDriver, String jdbcUrl) {
        this.port = port;
        this.basePath = basePath;
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
    }


    public static void main(String[] args) {
        try {
            new Application(DEFAULT_PORT, DEFAULT_BASE_PATH, DEFAULT_JDBC_DRIVER, DEFAULT_JDBC_URL).start();
        } catch (SQLException e) {
            log.error("Failed to init database", e);
        } catch (IOException | URISyntaxException e) {
            log.error("Failed to read file", e);
        }
    }

    public void start() throws SQLException, IOException, URISyntaxException {
        ConnectionFactory connectionFactory = new ConnectionFactory(jdbcDriver, jdbcUrl);
        initDB(connectionFactory);
        new ServerStarter(port, basePath, new AccountServiceImpl(new AccountDaoImpl(connectionFactory))).start();
    }

    public static void initDB(ConnectionFactory connectionFactory) throws SQLException, URISyntaxException, IOException {
        try (Connection connection = connectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(readFileFromResources("dbInit.sql"));
            statement.execute(readFileFromResources("demoData.sql"));
        }
    }

    private static String readFileFromResources(String fileName) throws URISyntaxException, IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(Application.class.getClassLoader()
                .getResource(fileName).toURI()));
        return new String(encoded);
    }


}
