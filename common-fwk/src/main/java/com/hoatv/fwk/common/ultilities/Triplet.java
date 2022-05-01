package com.hoatv.fwk.common.ultilities;

import lombok.Getter;

@Getter
public class Triplet<T, U, V> {

    private final T first;
    private final U second;
    private final V third;

    private Triplet(T first, U second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public static <T, U, V> Triplet<T, U, V> of(T first, U second, V third) {
        return new Triplet<>(first, second, third);
    }
}
