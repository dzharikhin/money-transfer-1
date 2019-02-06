package com.revolut.service;

import com.revolut.service.exception.AccountException;
import com.revolut.service.model.Account;

public interface AccountService {

    void transferMoney(long fromAccountId, long toAccountId, double amount) throws AccountException;

    Account getAccount(long accountId);
}
