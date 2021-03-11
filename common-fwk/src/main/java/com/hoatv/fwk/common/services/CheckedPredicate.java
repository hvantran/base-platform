package com.hoatv.fwk.common.services;


import com.hoatv.fwk.common.exceptions.AppException;

import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface CheckedPredicate<T> extends Predicate<T> {

    default boolean test(T input) {
        try {
            Objects.requireNonNull(input);
            return testThrows(input);
        } catch (Exception exception) {
            throw new AppException(exception);
        }
    }

    boolean testThrows(T input) throws Exception;
}
