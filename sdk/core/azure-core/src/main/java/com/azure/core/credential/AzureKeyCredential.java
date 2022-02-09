// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Represents a credential that uses a key to authenticate to an Azure Service.
 */
public final class AzureKeyCredential {
    private final ClientLogger logger = new ClientLogger(AzureKeyCredential.class);
    private volatile String key;

    /**
     * Creates a credential that authorizes request with the given key.
     *
     * @param key The key used to authorize requests.
     * @throws NullPointerException If {@code key} is {@code null}.
     * @throws IllegalArgumentException If {@code key} is an empty string.
     */
    public AzureKeyCredential(String key) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        if (key.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'key' cannot be empty."));
        }

        this.key = key;
    }

    /**
     * Retrieves the key associated to this credential.
     *
     * @return The key being used to authorize requests.
     */
    public String getKey() {
        return key;
    }

    /**
     * Rotates the key associated to this credential.
     *
     * @param key The new key to associated with this credential.
     * @return The updated {@code AzureKeyCredential} object.
     * @throws NullPointerException If {@code key} is {@code null}.
     * @throws IllegalArgumentException If {@code key} is an empty string.
     */
    public AzureKeyCredential update(String key) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        if (key.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'key' cannot be empty."));
        }

        this.key = key;
        return this;
    }
}
