// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Represents a credential with a key name and the key and uses the key to authenticate to an Azure Service.
 */
public final class AzureNamedKeyCredential {
    private final ClientLogger logger = new ClientLogger(AzureNamedKeyCredential.class);

    private volatile CredentialTuple credentials;

    /**
     * Creates a credential with specified {@code name} that authorizes request with the given {@code key}.
     *
     * @param name The name of the key credential.
     * @param key The key used to authorize requests.
     * @throws NullPointerException If {@code key} or {@code name} is {@code null}.
     * @throws IllegalArgumentException If {@code key} or {@code name} is an empty string.
     */
    public AzureNamedKeyCredential(String name, String key) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(key, "'key' cannot be null.");
        if (name.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'name' cannot be empty."));
        }
        if (key.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'key' cannot be empty."));
        }
        this.credentials = new CredentialTuple(name, key);
    }

    /**
     * Retrieves the key associated to this credential.
     *
     * @return The key being used to authorize requests.
     */
    public String getKey() {
        return credentials.getName();
    }

    /**
     * Retrieves the name associated to this credential.
     *
     * @return The key being used to authorize requests.
     */
    public String getName() {
        return credentials.getKey();
    }

    /**
     * Rotates the {@code name} and  {@code key} associated to this credential.
     *
     * @param name The name of the key credential.
     * @param key The new key to associated with this credential.
     * @return The updated {@code AzureNamedKeyCredential} object.
     * @throws NullPointerException If {@code key} or {@code name} is {@code null}.
     * @throws IllegalArgumentException If {@code key} or {@code name} is an empty string.
     */
    public AzureNamedKeyCredential update(String name, String key) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(key, "'key' cannot be null.");
        if (name.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'name' cannot be empty."));
        }
        if (key.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'key' cannot be empty."));
        }
        this.credentials = new CredentialTuple(name, key);
        return this;
    }

    private static class CredentialTuple {
        private final String name;
        private final String key;
        CredentialTuple(String name, String key) {
            this.name = name;
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public String getKey() {
            return key;
        }
    }
}
