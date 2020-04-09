// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.KeyEncryptionKey;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import reactor.core.publisher.Mono;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * LocalKeyEncryptionKeyAsyncClient AsyncKeyEncryptionKey} and {@link LocalKeyEncryptionKeyClient KeyEncryptionKey},
 * by calling {@link LocalKeyEncryptionKeyClientBuilder#buildAsyncKeyEncryptionKey(JsonWebKey)} and {@link
 * LocalKeyEncryptionKeyClientBuilder#buildKeyEncryptionKey(JsonWebKey)} respectively.
 * It constructs an instance of the desired client.
 *
 * <p> The minimal configuration options required by {@link LocalKeyEncryptionKeyClientBuilder} to
 * build {@link LocalKeyEncryptionKeyAsyncClient} or {@link LocalKeyEncryptionKeyClient} is
 * {@link JsonWebKey json web key}).</p>
 *
 * @see LocalKeyEncryptionKeyAsyncClient
 * @see LocalKeyEncryptionKeyClient
 */
public final class LocalKeyEncryptionKeyClientBuilder {
    private final ClientLogger logger = new ClientLogger(LocalKeyEncryptionKeyClientBuilder.class);

    /**
     * Creates a {@link LocalKeyEncryptionKeyClient} based on options set in the builder.
     * Every time {@code buildKeyEncryptionKey(String)} is called, a new instance of {@link KeyEncryptionKey}
     * is created.
     *
     * <p>The {@code key} is required to build the {@link LocalKeyEncryptionKeyClient client}.</p>
     *
     * @param key the key to be used for crypto operations.
     *
     * @return A {@link LocalKeyEncryptionKeyClient} with the options set from the builder.
     * @throws IllegalStateException If {{@code key} is not set.
     */
    public KeyEncryptionKey buildKeyEncryptionKey(JsonWebKey key) {
        return new LocalKeyEncryptionKeyClient((LocalKeyEncryptionKeyAsyncClient) buildAsyncKeyEncryptionKey(key).block());
    }

    /**
     * Creates a {@link LocalKeyEncryptionKeyClient} based on options set in the builder.
     * Every time {@code buildAsyncKeyEncryptionKey(String)} is called, a new instance of
     * {@link LocalKeyEncryptionKeyAsyncClient} is created.
     *
     * <p>The {@code key} is required to build the {@link LocalKeyEncryptionKeyAsyncClient client}.</p>
     *
     * @param key the key to be used for crypto operations.
     *
     * @return A {@link LocalKeyEncryptionKeyAsyncClient} with the options set from the builder.
     * @throws IllegalStateException If {@code key} is not set.
     */
    public Mono<? extends AsyncKeyEncryptionKey> buildAsyncKeyEncryptionKey(JsonWebKey key) {
        if (key == null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Json Web key value cannot be null and is required to create local key encryption key async client."));
        }

        return Mono.defer(() -> Mono.just(new LocalKeyEncryptionKeyAsyncClient(key)));
    }
}
