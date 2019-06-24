// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.extensions;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;

import java.util.concurrent.ExecutionException;

/**
 * The key resolver that caches the key after resolving to {@link IKey}.
 */
public class CachingKeyResolver implements IKeyResolver {

    private final IKeyResolver keyResolver;
    private final LoadingCache<String, ListenableFuture<IKey>> cache;

    /**
     * Constructor.
     * @param capacity the cache size
     * @param keyResolver the key resolver
     */
    public CachingKeyResolver(int capacity, final IKeyResolver keyResolver) {
        this.keyResolver = keyResolver;
        cache = CacheBuilder.newBuilder().maximumSize(capacity)
                .build(new CachingKeyResolverCacheLoader(keyResolver));
    }

    @Override
    public ListenableFuture<IKey> resolveKeyAsync(String kid) {
        KeyIdentifier keyIdentifier = new KeyIdentifier(kid);
        if (keyIdentifier.version() == null) {
            final ListenableFuture<IKey> key = keyResolver.resolveKeyAsync(kid);
            key.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        cache.put(key.get().getKid(), key);
                    } catch (InterruptedException | ExecutionException e) {
                        // Key caching will occur on first read
                    }
                }
            }, MoreExecutors.directExecutor()
            );
            return key;
        } else {
            return cache.getUnchecked(kid);
        }
    }

    private static class CachingKeyResolverCacheLoader extends CacheLoader<String, ListenableFuture<IKey>> {

        private final IKeyResolver keyResolver;

        CachingKeyResolverCacheLoader(IKeyResolver keyResolver) {
            this.keyResolver = keyResolver;
        }

        @Override
        public ListenableFuture<IKey> load(String kid) {
            return keyResolver.resolveKeyAsync(kid);
        }
    }
}
