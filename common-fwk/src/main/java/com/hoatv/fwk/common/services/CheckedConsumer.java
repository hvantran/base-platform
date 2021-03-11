package com.hoatv.fwk.common.services;

import com.hoatv.fwk.common.exceptions.AppException;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface CheckedConsumer<T> extends Consumer<T> {

    default void accept (T input) {
        try {
            Objects.requireNonNull(input);
            acceptThrows(input);
        } catch (Exception exception) {
            throw new AppException(exception);
        }
    }

    void acceptThrows(T input) throws Exception;
}
