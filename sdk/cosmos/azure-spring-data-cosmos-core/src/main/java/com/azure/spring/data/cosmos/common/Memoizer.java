// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Memoize function computation results
 */
public final class Memoizer<I, O> {

    private final Map<I, O> cache = new ConcurrentHashMap<>();

    private Memoizer() {
    }

    /**
     * Put function computation results into Memoizer
     *
     * @param <I> the type of the input to the function
     * @param <O> the type of the output of the function
     * @param function represents a function that accepts one argument and produces a result
     * @return Function
     */
    public static <I, O> Function<I, O> memoize(Function<I, O> function) {
        return new Memoizer<I, O>().internalMemoize(function);
    }

    private Function<I, O> internalMemoize(Function<I, O> function) {
        return input -> cache.computeIfAbsent(input, function);
    }

}
