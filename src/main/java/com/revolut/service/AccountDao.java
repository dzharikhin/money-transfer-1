package com.revolut.service;

import com.revolut.service.exception.DaoException;
import com.revolut.service.model.Account;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountDao {

    Optional<Account> getAccountByNumber(long number) throws DaoException;

    void createTransaction(long fromNumber, long toNumber, BigDecimal amount) throws DaoException;
}
