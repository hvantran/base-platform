package com.hoatv.fwk.common.services;

import com.hoatv.fwk.common.exceptions.AppException;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface BiCheckedConsumer<T, U> extends BiConsumer<T, U> {

    default void accept (T input, U inputArg) {
        try {
            acceptThrows(input, inputArg);
        } catch (Exception exception) {
            throw new AppException(exception);
        }
    }

    void acceptThrows(T input, U inputArg) throws Exception;
}
