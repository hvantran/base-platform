package com.hoatv.fwk.common.exceptions;

public class DuplicateResourceException extends AppException {
    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(Throwable throwable) {
        super(throwable);
    }

    public DuplicateResourceException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
