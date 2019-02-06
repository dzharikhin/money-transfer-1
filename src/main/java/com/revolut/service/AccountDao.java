package com.revolut.service;

import com.revolut.service.exception.DaoException;
import com.revolut.service.model.Account;

import java.util.Optional;

public interface AccountDao {

    Optional<Account> getAccountByNumber(Long number) throws DaoException;

    void createTransaction(long fromNumber, long toNumber, double amount) throws DaoException;
}
