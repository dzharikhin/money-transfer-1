package com.revolut.service.exception;

public class DaoException extends RuntimeException{
    public DaoException(Throwable cause) {
        super(cause);
    }
}
