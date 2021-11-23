// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.util;

import java.util.Objects;

/**
 * Tuple class.
 * @param <T> The type of the first element in the tuple.
 * @param <U> The type of the second element in the tuple.
 */
public final class Tuple<T, U> {

    private final T first;
    private final U second;

    private Tuple(T first, U second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Creates a tuple from two value.
     *
     * @param first The first value.
     * @param second The second value.
     * @param <T> The type of the first value.
     * @param <U> The type of the second value.
     * @return A new tuple.
     */
    public static <T, U> Tuple<T, U> of(T first, U second) {
        return new Tuple<>(first, second);
    }

    /**
     * Gets the first value.
     *
     * @return The first value.
     */
    public T getFirst() {
        return first;
    }

    /**
     * Gets the second value.
     *
     * @return The second value.
     */
    public U getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(first, tuple.first) && Objects.equals(second, tuple.second);
    }

    @Override
    public int hashCode() {

        return Objects.hash(first, second);
    }
}
