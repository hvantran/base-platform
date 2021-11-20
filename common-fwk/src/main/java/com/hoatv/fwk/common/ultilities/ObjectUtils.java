package com.hoatv.fwk.common.ultilities;

import com.hoatv.fwk.common.exceptions.AppException;

import java.util.function.Predicate;

public class ObjectUtils {

    private ObjectUtils() {

    }

    public static void checkThenThrow(boolean predicate) {
        checkThenThrow(predicate, "The condition must be TRUE");
    }

    public static void checkThenThrow(boolean predicate, String message) {
        if (predicate) {
            throw new AppException(message);
        }
    }

    public static <T> void checkThenThrow(Predicate<T> predicate, T instance, String message) {
        if (predicate.test(instance)) {
            throw new AppException(message);
        }
    }
}
