// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Synchronizes reactor threads accessing/instantiating a common value {@code T}.
 *
 * @param <T> The value being instantiated / accessed.
 */
public class SynchronizedAccessor<T> {
    private Mono<T> monoCache;

    public SynchronizedAccessor(Supplier<Mono<T>> supplier) {
        monoCache = supplier.get().cache();
    }

    /**
     * Get the value from the configured supplier.
     *
     * @return the output {@code T}
     */
    public Mono<T> getValue() {
        return monoCache;
    }
}
