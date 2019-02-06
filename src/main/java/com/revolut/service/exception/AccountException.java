package com.revolut.service.exception;

//ожидаемое - можно и checked, но это такое, необязательное
public class AccountException extends RuntimeException {

    //нету инфы про аккаунт - скорее плохо, т.к. сложнее диагностировать
    public AccountException(String message) {
        super(message);
    }
}
