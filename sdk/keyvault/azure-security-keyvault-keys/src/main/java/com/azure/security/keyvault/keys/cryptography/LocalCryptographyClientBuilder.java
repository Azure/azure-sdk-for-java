// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.models.JsonWebKey;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * LocalCryptographyAsyncClient cryptography async client} and {@link LocalCryptographyClient cryptography sync client},
 * by calling {@link LocalCryptographyClientBuilder#buildAsyncClient() buildAsyncClient} and {@link
 * LocalCryptographyClientBuilder#buildClient() buildClient} respectively
 * It constructs an instance of the desired client.
 *
 * <p> The minimal configuration options required by {@link LocalCryptographyClientBuilder cryptographyClientBuilder} to
 * build {@link LocalCryptographyAsyncClient} or {@link LocalCryptographyClient} is ({@link JsonWebKey jsonWebKey}.
 * </p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.async.LocalCryptographyAsyncClient.instantiation}
 *
 * @see LocalCryptographyAsyncClient
 * @see LocalCryptographyClient
 */
public final class LocalCryptographyClientBuilder {
    private final ClientLogger logger = new ClientLogger(LocalCryptographyClientBuilder.class);
    private JsonWebKey jsonWebKey;

    /**
     * Creates a {@link LocalCryptographyClient} based on options set in the builder.
     * Every time {@code buildClient()} is called, a new instance of {@link LocalCryptographyClient} is created.
     *
     * <p> The LocalCryptographyClientBuilder#key(JsonWebKey) jsonWebKey identifier} is required to build the {@link
     * LocalCryptographyClient async client}.</p>
     *
     * @return A {@link LocalCryptographyClient} with the options set from the builder.
     * @throws IllegalStateException If ({@link LocalCryptographyClientBuilder#key(JsonWebKey)} is not set.
     */
    public LocalCryptographyClient buildClient() {
        return new LocalCryptographyClient(buildAsyncClient());
    }

    /**
     * Creates a {@link LocalCryptographyAsyncClient} based on options set in the builder. Every time
     * {@link #buildAsyncClient()} is called, a new instance of {@link LocalCryptographyAsyncClient} is created.
     *
     * <p> The LocalCryptographyClientBuilder#key(JsonWebKey) jsonWebKey} is required to build the {@link
     * LocalCryptographyAsyncClient async client}.</p>
     *
     * @return A {@link LocalCryptographyAsyncClient} with the options set from the builder.
     * @throws NullPointerException If {@link LocalCryptographyClientBuilder#key(JsonWebKey)} is not set.
     */
    public LocalCryptographyAsyncClient buildAsyncClient() {
        if (jsonWebKey == null) {
            throw logger.logExceptionAsError(new NullPointerException(
                "Json Web Key is required to create local cryptography client"));
        }

        return new LocalCryptographyAsyncClient(jsonWebKey);
    }

    /**
     * Sets the jsonWebKey to be used for cryptography operations.
     *
     * @param key The key to be used for cryptography operations.
     * @return the updated builder object.
     */
    public LocalCryptographyClientBuilder key(JsonWebKey key) {
        this.jsonWebKey = key;
        return this;
    }
}
