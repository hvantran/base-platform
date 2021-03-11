package com.hoatv.fwk.common.services;

import com.hoatv.fwk.common.exceptions.AppException;

import java.util.Objects;
import java.util.function.BiFunction;

@FunctionalInterface
public interface BiCheckedFunction<T, U, R> extends BiFunction<T, U, R> {

    default R apply(T input, U input2) {
        try {
            Objects.requireNonNull(input);
            Objects.requireNonNull(input2);
            return applyThrows(input, input2);
        } catch (Exception exception) {
            throw new AppException(exception);
        }
    }

    R applyThrows(T input, U input2) throws Exception;
}