package com.revolut.service;

import com.revolut.service.exception.AccountException;
import com.revolut.service.model.Account;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AccountServiceImpl implements AccountService {
    private final AccountDao accountDao;
    private ConcurrentMap<Long, Lock> locks = new ConcurrentHashMap<>();

    public AccountServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public void transferMoney(long fromAccountId, long toAccountId, double amount) throws AccountException {
        locks.putIfAbsent(fromAccountId, new ReentrantLock());
        Lock lock = locks.get(fromAccountId);
        lock.lock();
        try {
            Account from = getAccount(fromAccountId);
            getAccount(toAccountId);
            if (from.getBalance() < amount) {
                throw new AccountException("Not enough money");
            }
            accountDao.createTransaction(fromAccountId, toAccountId, amount);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Account getAccount(long accountId) throws AccountException {
        return accountDao.getAccountByNumber(accountId).orElseThrow(() -> new AccountException("Account " + accountId + " does not exist"));
    }
}
