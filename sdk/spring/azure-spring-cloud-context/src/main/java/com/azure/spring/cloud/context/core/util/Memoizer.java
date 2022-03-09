// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Memorize function execution result
 *
 * @author Warren Zhu
 */
public class Memoizer {
    /**
     * Memoizes a function.
     *
     * @param fn The function to memoize.
     * @param <T> Input type for the function.
     * @param <R> Output type for the function.
     * @return The memoized function.
     */
    public static <T, R> Function<T, R> memoize(Function<T, R> fn) {
        Map<T, R> map = new ConcurrentHashMap<>();
        return (t) -> map.computeIfAbsent(t, fn);
    }

    /**
     * Memoizes a function into a specific cache map.
     *
     * @param map The memoize cache map.
     * @param fn The function to memoize.
     * @param <T> Input type for the function.
     * @param <R> Output type for the function.
     * @return The memoized function.
     */
    public static <T, R> Function<T, R> memoize(Map<T, R> map, Function<T, R> fn) {
        return (t) -> map.computeIfAbsent(t, fn);
    }

    /**
     * Memoizes a bi-function.
     *
     * @param biFunction The bi-function to memoize.
     * @param <T> First input type for the function.
     * @param <U> Second input type for the function.
     * @param <R> Output type for the function.
     * @return The memoized bi-function.
     */
    public static <T, U, R> BiFunction<T, U, R> memoize(BiFunction<T, U, R> biFunction) {
        Map<Tuple<T, U>, R> map = new ConcurrentHashMap<>();
        return (t, u) -> map.computeIfAbsent(Tuple.of(t, u), (k) -> biFunction.apply(k.getFirst(), k.getSecond()));
    }

    /**
     * Memoizes a bi-function into a specific cache map.
     *
     * @param map The memoize cache map.
     * @param biFunction The bi-function to memoize.
     * @param <T> First input type for the function.
     * @param <U> Second input type for the function.
     * @param <R> Output type for the function.
     * @return The memoized bi-function.
     */
    public static <T, U, R> BiFunction<T, U, R> memoize(Map<Tuple<T, U>, R> map, BiFunction<T, U, R> biFunction) {
        return (t, u) -> map.computeIfAbsent(Tuple.of(t, u), (k) -> biFunction.apply(k.getFirst(), k.getSecond()));
    }
}
