package com.revolut.service;

import com.revolut.service.exception.AccountException;
import com.revolut.service.exception.DaoException;
import com.revolut.service.model.Account;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class AccountServiceImplTest {

    private AccountServiceImpl accountService;

    //нужен, пожалуй, классический тест, где tx 1->2 входит, потом входит 2->1
    //У меня
    @Test
    void transferMoneyConcurrency() throws ExecutionException, InterruptedException {
        accountService = new AccountServiceImpl(new TestDao());
        ExecutorService executor = Executors.newFixedThreadPool(2);

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
                accountService.transferMoney(1, 2, BigDecimal.valueOf(60));
            } catch (AccountException e) {
                assertThat(e.getMessage(), is("Not enough money"));
                return false;
            }
            return true;
        };
    }

    static final class TestDao implements AccountDao {
        private final Map<Long, BigDecimal> balances = new ConcurrentHashMap<>();

        private boolean shouldSleep = true;

        private TestDao() {
            balances.put(1L, BigDecimal.valueOf(100d));
            balances.put(2L, BigDecimal.valueOf(100d));
        }

        @Override
        public Optional<Account> getAccountByNumber(long number) throws DaoException {
            if (shouldSleep) {
                shouldSleep = false;
                try {
                    //тесты на слипах - тупиковая ветвь, дурной тон. Лучше добавить какие-нибудь exchanger'ы или другую надежную синхронизацию
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
            return Optional.of(new Account(number, balances.computeIfAbsent(number, n -> BigDecimal.valueOf(100))));
        }

        @Override
        public void createTransaction(long fromNumber, long toNumber, BigDecimal amount) throws DaoException {
            balances.compute(fromNumber, (from, balance) -> balance.subtract(amount));
            balances.compute(toNumber, (to, balance) -> balance.add(amount));
        }
    }
}
