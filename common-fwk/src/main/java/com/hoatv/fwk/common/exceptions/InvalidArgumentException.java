package com.hoatv.fwk.common.exceptions;

public class InvalidArgumentException extends AppException {

    public InvalidArgumentException (String message) {
        super(message);
    }

    public InvalidArgumentException (Throwable throwable) {
        super(throwable);
    }

    public InvalidArgumentException (String message, Throwable throwable) {
        super(message, throwable);
    }
}
