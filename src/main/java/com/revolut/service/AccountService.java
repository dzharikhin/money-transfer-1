package com.revolut.service;

import com.revolut.service.exception.AccountException;
import com.revolut.service.model.Account;
import java.math.BigDecimal;

public interface AccountService {

    //double деньги хранить моветон. BigDecimal - хорошо, хуже - long число копеек, если валюта одна и та же
    void transferMoney(long fromAccountId, long toAccountId, BigDecimal amount) throws AccountException;

    Account getAccount(long accountId) throws AccountException;
}
