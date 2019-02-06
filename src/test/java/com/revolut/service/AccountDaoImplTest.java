package com.revolut.service;

import com.revolut.service.model.Account;
import java.math.BigDecimal;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import static com.revolut.Application.initDB;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountDaoImplTest {
    private AccountDaoImpl accountDao;

    private DataSource dataSource;

    @BeforeEach
    void setUp() throws IOException, SQLException, URISyntaxException {
        dataSource = DataSourceFactory.create("org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        initDB(dataSource);
        accountDao = new AccountDaoImpl(dataSource);
        try(Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {
            statement.execute("insert into transactions (from_account_number, to_account_number, amount) values (1,2,34.5634);\n" +
                    "insert into transactions (from_account_number, to_account_number, amount) values (3,1,50.7313);\n" +
                    "insert into transactions (from_account_number, to_account_number, amount) values (2,3,130);\n" +
                    "insert into transactions (from_account_number, to_account_number, amount) values (1,2,68);");
        }
    }

    @AfterEach
    void eraseDB() throws SQLException {
        try(Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()){
            statement.execute("DROP ALL OBJECTS");
        }
    }

    @Test
    void getExistedAccountById() {
        Optional<Account> account = accountDao.getAccountByNumber(1L);
        assertTrue(account.isPresent());
        assertThat(account.get(), is(new Account(1L, BigDecimal.valueOf(48.1679d))));
    }

    @Test
    void getNotExistedAccountById() {
        Optional<Account> account = accountDao.getAccountByNumber(4L);
        assertTrue(account.isEmpty());
    }

    @Test
    void createTransaction() {
        accountDao.createTransaction(1L, 2L, BigDecimal.valueOf(0.16));
        Optional<Account> account = accountDao.getAccountByNumber(1L);
        assertTrue(account.isPresent());
        assertThat(account.get(), is(new Account(1L, BigDecimal.valueOf(48.0079d))));
    }


}
