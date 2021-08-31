// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;
import reactor.core.publisher.Mono;

/**
 * Extremely basic key resolver to test client side encryption
 */
public final class FakeKeyResolver implements AsyncKeyEncryptionKeyResolver, IKeyResolver {

    FakeKey key;

    public FakeKeyResolver(FakeKey key) {
        this.key = key;
    }

    @Override
    public Mono<? extends AsyncKeyEncryptionKey> buildAsyncKeyEncryptionKey(String keyId) {
        return key.getKeyId().flatMap(kid -> keyId.equals(kid)
            ? Mono.just(key)
            : Mono.error(new IllegalArgumentException("Key does not exist")));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public ListenableFuture<IKey> resolveKeyAsync(String s) {
        return Futures.immediateFuture(this.key);
    }
}
