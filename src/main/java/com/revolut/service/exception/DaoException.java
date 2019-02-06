package com.revolut.service.exception;

public class DaoException extends RuntimeException{
    //Нету инфы полезной для диагностики - ни таблицы, ни id - мб из соображений безопасности
    public DaoException(Throwable cause) {
        super(cause);
    }
}
