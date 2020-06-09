// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Memoize function computation results
 */
public class Memoizer<I, O> {

    private final Map<I, O> cache = new ConcurrentHashMap<>();

    private Memoizer() {}

    public static <I, O> Function<I, O> memoize(Function<I, O> function) {
        return new Memoizer<I, O>().internalMemoize(function);
    }

    private Function<I, O> internalMemoize(Function<I, O> function) {
        return input -> cache.computeIfAbsent(input, function);
    }

}
