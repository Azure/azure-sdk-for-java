// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A key client which is used to asynchronously wrap or unwrap another key.
 *
 * <p>When a {@link KeyEncryptionKeyAsyncClient} gets created using a {@code Azure Key Vault key identifier}, the first
 * time a cryptographic operation is attempted, the client will attempt to retrieve the key material from the service,
 * cache it, and perform all future cryptographic operations locally, deferring to the service when that's not possible.
 * If key retrieval and caching fails because of a non-retryable error, the client will not make any further attempts
 * and will fall back to performing all cryptographic operations on the service side. Conversely, when a
 * {@link KeyEncryptionKeyAsyncClient} created using a {@link JsonWebKey JSON Web Key}, all cryptographic operations
 * will be performed locally.</p>
 */
@ServiceClient(builder = KeyEncryptionKeyClientBuilder.class, isAsync = true)
public final class KeyEncryptionKeyAsyncClient extends CryptographyAsyncClient implements AsyncKeyEncryptionKey {
    private static final ClientLogger LOGGER = new ClientLogger(KeyEncryptionKeyAsyncClient.class);

    /**
     * Creates a {@link KeyEncryptionKeyAsyncClient} that uses {@code pipeline} to service requests.
     *
     * @param keyId The identifier of the key to use for cryptography operations.
     * @param pipeline The {@link HttpPipeline} that the HTTP requests and responses flow through.
     * @param version {@link CryptographyServiceVersion} of the service to be used when making requests.
     * @param disableKeyCaching Indicates if local key caching should be disabled and all cryptographic operations
     * deferred to the service.
     */
    KeyEncryptionKeyAsyncClient(String keyId, HttpPipeline pipeline, CryptographyServiceVersion version,
                                boolean disableKeyCaching) {
        super(keyId, pipeline, version, disableKeyCaching);
    }

    /**
     * Creates a {@link KeyEncryptionKeyAsyncClient} that uses {@code pipeline} to service requests.
     *
     * @param jsonWebKey The {@link JsonWebKey} to use for local cryptography operations.
     */
    KeyEncryptionKeyAsyncClient(JsonWebKey jsonWebKey) {
        super(jsonWebKey);
    }

    /**
     * Get the identifier of the key to use for cryptography operations.
     *
     * @return A {@link Mono} containing the key identifier.
     */
    @Override
    public Mono<String> getKeyId() {
        return Mono.defer(() -> Mono.just(this.keyId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<byte[]> wrapKey(String algorithm, byte[] key) {
        try {
            KeyWrapAlgorithm wrapAlgorithm = KeyWrapAlgorithm.fromString(algorithm);

            return wrapKey(wrapAlgorithm, key).flatMap(keyWrapResult -> Mono.just(keyWrapResult.getEncryptedKey()));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<byte[]> unwrapKey(String algorithm, byte[] encryptedKey) {
        try {
            KeyWrapAlgorithm wrapAlgorithm = KeyWrapAlgorithm.fromString(algorithm);

            return unwrapKey(wrapAlgorithm, encryptedKey)
                .flatMap(keyUnwrapResult -> Mono.just(keyUnwrapResult.getKey()));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }
}
