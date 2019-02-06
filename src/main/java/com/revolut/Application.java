package com.revolut;


import com.revolut.service.AccountDaoImpl;
import com.revolut.service.AccountServiceImpl;
import com.revolut.service.DataSourceFactory;
import java.util.Optional;
import javax.sql.DataSource;
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
    //можно поюзать lombok - хипстеры любят
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final String DEFAULT_JDBC_URL = "jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1";
    private static final String DEFAULT_BASE_PATH = "/api";
    private static final int DEFAULT_PORT = 8313;

    private final DataSource dataSource;
    private final String basePath;
    private final int port;

    public Application(int port, String basePath, String jdbcDriver, String jdbcUrl) {
        this.port = port;
        this.basePath = basePath;
        this.dataSource = DataSourceFactory.create(jdbcDriver, jdbcUrl);
    }


    public static void main(String[] params) {
        try {
            //ну или как-то иначе, но порт точно надо уметь конфигурить - 8313 тупо может быть занят у проверяющего
            //оно не умеет порт 0?
            //NFE у Integer#valueOf
            int port = Optional.of(params).filter(args -> args.length > 0).map(args -> args[0]).map(Integer::valueOf).orElse(DEFAULT_PORT);
            new Application(port, DEFAULT_BASE_PATH, DataSourceFactory.H2_JDBC_DRIVER, DEFAULT_JDBC_URL).start();
        } catch (SQLException e) {
            log.error("Failed to init database", e);
        } catch (IOException | URISyntaxException e) {
            log.error("Failed to read file", e);
        }
    }

    public void start() throws SQLException, IOException, URISyntaxException {
        initDB(dataSource);
        new ServerStarter(port, basePath, new AccountServiceImpl(new AccountDaoImpl(dataSource))).start();
    }

    public static void initDB(DataSource dataSource) throws SQLException, URISyntaxException, IOException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(readFileFromResources("dbSchema.sql"));
            statement.execute(readFileFromResources("demoData.sql"));
        }
    }

    //ты проверял - это работает когда запускаешь jar?
    private static String readFileFromResources(String fileName) throws URISyntaxException, IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(Application.class.getClassLoader()
                .getResource(fileName).toURI()));
        return new String(encoded);
    }
}
