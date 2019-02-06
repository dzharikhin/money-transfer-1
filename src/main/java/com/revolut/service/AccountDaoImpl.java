package com.revolut.service;

import com.revolut.service.exception.DaoException;
import com.revolut.service.model.Account;

import java.sql.*;
import java.util.Optional;

public class AccountDaoImpl implements AccountDao {
    private final ConnectionFactory connectionFactory;


    private static final String CREATE_TRANSACTION_QUERY = "insert into transactions (from_account_number, to_account_number, amount) values (?, ?, ?)";
    private static final String SELECT_ACCOUNTS_QUERY = "select * from account_balance";
    private static final String WHERE_ACCOUNT_CAUSE = " where account_number = ";

    public AccountDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Optional<Account> getAccountByNumber(Long number) throws DaoException {
        try (Connection connection = connectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(SELECT_ACCOUNTS_QUERY + WHERE_ACCOUNT_CAUSE + number);
            if (rs.next()) {
                return Optional.of(new Account(rs.getLong("account_number"), rs.getDouble("balance")));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public void createTransaction(long fromNumber, long toNumber, double amount) throws DaoException {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_TRANSACTION_QUERY)) {
            statement.setLong(1, fromNumber);
            statement.setLong(2, toNumber);
            statement.setDouble(3, amount);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
}
