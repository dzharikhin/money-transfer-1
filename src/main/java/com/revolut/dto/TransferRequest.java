package com.revolut.dto;

public class TransferRequest {
    private final Long fromAccountNumber;
    private final Long toAccountNumber;
    private final Double amount;

    public TransferRequest(Long fromAccountNumber, Long toAccountNumber, Double amount) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
    }

    public Long getFromAccountNumber() {
        return fromAccountNumber;
    }

    public Long getToAccountNumber() {
        return toAccountNumber;
    }

    public Double getAmount() {
        return amount;
    }

}
