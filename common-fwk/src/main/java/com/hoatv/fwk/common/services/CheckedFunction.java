package com.hoatv.fwk.common.services;


import com.hoatv.fwk.common.exceptions.AppException;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<T, R> extends Function<T, R> {

    default R apply(T input) {
        try {
            Objects.requireNonNull(input);
            return applyThrows(input);
        } catch (Exception exception) {
            throw new AppException(exception);
        }
    }

    R applyThrows(T input) throws Exception;
}
