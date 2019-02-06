package com.revolut.service.model;

import java.util.Objects;

public class Account {

    private final Long number;
    private final Double balance;

    public Account(Long number, Double balance) {
        this.number = number;
        this.balance = balance;
    }


    public Double getBalance() {
        return balance;
    }

    public Long getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(getNumber(), account.getNumber()) &&
                Objects.equals(getBalance(), account.getBalance());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNumber(), getBalance());
    }

    @Override
    public String toString() {
        return "Account{" +
                "number=" + number +
                ", balance=" + balance +
                '}';
    }
}
