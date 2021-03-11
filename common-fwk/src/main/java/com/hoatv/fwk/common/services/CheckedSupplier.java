package com.hoatv.fwk.common.services;

import com.hoatv.fwk.common.exceptions.AppException;

import java.util.function.Supplier;

@FunctionalInterface
public interface CheckedSupplier<T> extends Supplier<T> {

    default T get() {
        try {
            return getThrows();
        } catch (Exception exception) {
            throw new AppException(exception);
        }
    }

    T getThrows() throws Exception;
}
