// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.common.implementation.util.AutoRefreshingCache;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Creates layout caches with synchronous and asynchronous value providers.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class BlobLayoutCacheFactory {
    private BlobLayoutCacheFactory() {
    }

    /**
     * Creates a layout cache backed by the supplied value provider.
     *
     * @param valueSupplier The provider used to create layout values.
     * @return A layout cache.
     */
    public static AutoRefreshingCache<BlobLayoutCacheValue> create(Supplier<Mono<BlobLayoutCacheValue>> valueSupplier) {
        return new AutoRefreshingCache<>(new AutoRefreshingCache.ValueProvider<BlobLayoutCacheValue>() {
            @Override
            public Mono<BlobLayoutCacheValue> createAsync() {
                return valueSupplier.get();
            }

            @Override
            public BlobLayoutCacheValue createSync() {
                return valueSupplier.get().block();
            }
        });
    }
}
