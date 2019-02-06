package com.revolut.service;

import com.revolut.service.exception.AccountException;
import com.revolut.service.exception.DaoException;
import com.revolut.service.model.Account;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class AccountServiceImplTest {

    private AccountServiceImpl accountService;

    @Test
    void transferMoneyConcurrency() throws ExecutionException, InterruptedException {
        accountService = new AccountServiceImpl(new TestDao());
        ExecutorService executor = Executors.newFixedThreadPool(10);

        Future<Boolean> successTransfer = executor.submit(newTransferTask());
        Thread.sleep(500);
        Future<Boolean> failedTransfer = executor.submit(newTransferTask());

        if(!successTransfer.get()){
            fail("First thread should not be thrown");
        }
        if(failedTransfer.get()){
            fail("Second thread should throw exception");
        }
        executor.shutdown();
    }

    private Callable<Boolean> newTransferTask(){
        return () -> {
            try {
                accountService.transferMoney(1, 2, 60);
            } catch (AccountException e) {
                assertThat(e.getMessage(), is("Not enough money"));
                return false;
            }
            return true;
        };
    }

    class TestDao implements AccountDao {
        private Map<Long, Double> balances = new HashMap<>();

        private boolean shouldSleep = true;

        private TestDao() {
            balances.put(1L, 100d);
            balances.put(2L, 100d);
        }

        @Override
        public Optional<Account> getAccountByNumber(Long number) throws DaoException {
            if (shouldSleep) {
                shouldSleep = false;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return Optional.of(new Account(number, balances.getOrDefault(number, 100d)));
        }

        @Override
        public void createTransaction(long fromNumber, long toNumber, double amount) throws DaoException {
            balances.put(fromNumber, balances.get(fromNumber) - amount);
            balances.put(toNumber, balances.get(toNumber) + amount);
        }
    }
}