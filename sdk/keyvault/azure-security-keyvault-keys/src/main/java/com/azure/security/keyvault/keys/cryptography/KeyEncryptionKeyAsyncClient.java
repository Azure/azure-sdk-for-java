// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import static com.azure.core.util.FluxUtil.monoError;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import reactor.core.publisher.Mono;

/**
 * A key client which is used to asynchronously encrypt, or wrap, another key.
 */
public final class KeyEncryptionKeyAsyncClient extends CryptographyAsyncClient implements AsyncKeyEncryptionKey {

    private final ClientLogger logger = new ClientLogger(KeyEncryptionKeyAsyncClient.class);

    /**
     * Creates a KeyEncryptionKeyAsyncClient that uses {@code pipeline} to service requests
     *
     * @param keyId The identifier of the key to use for cryptography operations.
     * @param pipeline The HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link CryptographyServiceVersion} of the service to be used when making requests.
     */
    KeyEncryptionKeyAsyncClient(String keyId, HttpPipeline pipeline, CryptographyServiceVersion version) {
        super(keyId, pipeline, version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<String> getKeyId() {
        try {
            return Mono.just(key.getId());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<byte[]> wrapKey(String algorithm, byte[] key) {
        try {
            KeyWrapAlgorithm wrapAlgorithm = KeyWrapAlgorithm.fromString(algorithm);
            return wrapKey(wrapAlgorithm, key).flatMap(keyWrapResult -> Mono.just(keyWrapResult.getEncryptedKey()));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<byte[]> unwrapKey(String algorithm, byte[] encryptedKey) {
        try {
            KeyWrapAlgorithm wrapAlgorithm = KeyWrapAlgorithm.fromString(algorithm);
            return unwrapKey(wrapAlgorithm, encryptedKey).flatMap(keyUnwrapResult -> Mono.just(keyUnwrapResult.getKey()));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
