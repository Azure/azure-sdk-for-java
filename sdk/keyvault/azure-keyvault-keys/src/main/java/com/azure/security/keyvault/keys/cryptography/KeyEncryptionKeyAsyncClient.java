// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.http.HttpPipeline;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import reactor.core.publisher.Mono;

/**
 * A key client which is used to asynchronously encrypt, or wrap, another key.
 */
public final class KeyEncryptionKeyAsyncClient extends CryptographyAsyncClient implements AsyncKeyEncryptionKey {

    /**
     * Creates a KeyEncryptionKeyAsyncClient that uses {@code pipeline} to service requests
     *
     * @param keyId The identifier of the key to use for cryptography operations.
     * @param pipeline The HttpPipeline that the HTTP requests and responses flow through.
     */
    KeyEncryptionKeyAsyncClient(String keyId, HttpPipeline pipeline) {
        super(keyId, pipeline);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<String> getKeyId() {
        return Mono.just(key.getKid());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<byte[]> wrapKey(String algorithm, byte[] key) {
        KeyWrapAlgorithm wrapAlgorithm = KeyWrapAlgorithm.fromString(algorithm);
        return wrapKey(wrapAlgorithm, key).flatMap(keyWrapResult -> Mono.just(keyWrapResult.getEncryptedKey()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<byte[]> unwrapKey(String algorithm, byte[] encryptedKey) {
        KeyWrapAlgorithm wrapAlgorithm = KeyWrapAlgorithm.fromString(algorithm);
        return unwrapKey(wrapAlgorithm, encryptedKey).flatMap(keyUnwrapResult -> Mono.just(keyUnwrapResult.getKey()));
    }
}
