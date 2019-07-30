package com.azure.data.cosmos.internal.query;

/**
 * A functional interface (callback) that computes a value based on multiple input values.
 * @param <T> the first value type
 * @param <U> the second value type
 * @param <V> the third value type
 * @param <R> the result type
 */

@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @param v the third function argument
     * @return the function result
     */
    R apply(T t, U u, V v);
}