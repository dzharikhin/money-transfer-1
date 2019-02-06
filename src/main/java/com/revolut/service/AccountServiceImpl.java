package com.revolut.service;

import com.revolut.service.exception.AccountException;
import com.revolut.service.model.Account;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AccountServiceImpl implements AccountService {
    private final AccountDao accountDao;
    private final ConcurrentMap<Long, Lock> locks = new ConcurrentHashMap<>();

    public AccountServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public void transferMoney(long fromAccountId, long toAccountId, BigDecimal amount) throws AccountException {
        Lock lock = locks.computeIfAbsent(fromAccountId, accountNumber -> new ReentrantLock());
        lock.lock();
        try {
            //java 11 же!
            var fromAccount = getAccount(fromAccountId);
            //тут может вылететь птичка - и лок как бы и не надо было брать бы, но уже берем. не самый изящный ход
            // я бы перенес во внутренний метод,
            // но ломается тест AccountServiceImplTest.transferMoneyConcurrency
            var toAccount = getAccount(toAccountId);
            transferMoney(fromAccount, toAccount, amount);
        } finally {
            lock.unlock();
        }
    }

    private void transferMoney(Account fromAccount, Account toAccount, BigDecimal amount) throws AccountException {
            //а это будет работать не на однопоточной h2, а на другой DB, с каким-нибудь уровнем изоляции READ_COMMITTED?
            //потому что если баланс будет обновляться неатомарно, то все развалится
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                //не информативное сообщение - мне что с таким ответом делать? на каком счету, сколько не хватило.
                // Мб конечно из соображений безопасности
                throw new AccountException("Not enough money");
            }
            accountDao.createTransaction(fromAccount.getNumber(), toAccount.getNumber(), amount);
    }

    @Override
    public Account getAccount(long accountId) throws AccountException {
        return accountDao.getAccountByNumber(accountId).orElseThrow(() -> new AccountException("Account " + accountId + " does not exist"));
    }
}
