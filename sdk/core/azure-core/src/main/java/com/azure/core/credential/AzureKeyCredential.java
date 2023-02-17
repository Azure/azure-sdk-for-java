// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Represents a credential that uses a key to authenticate to an Azure Service.
 */
public final class AzureKeyCredential {
    // AzureKeyCredential is a commonly used credential type, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(AzureKeyCredential.class);
    private volatile String[] keys;

    private final Object synchronizer = new Object();

    /**
     * Creates a credential that authorizes requests with the given key.
     *
     * @param key The key used to authorize requests.
     * @throws NullPointerException If {@code key} is null.
     * @throws IllegalArgumentException If {@code key} is an empty string.
     */
    public AzureKeyCredential(String key) {
        validateKey(key);

        this.keys = new String[] {key};
    }

    /**
     * Create a key credential that authorizes requests with multiple given keys.
     *
     * @param keys The keys used to authorize requests.
     * @throws NullPointerException If {@code keys} is null or if any key is null.
     * @throws IllegalArgumentException If {@code keys} is empty or any key is an empty string.
     */
    public AzureKeyCredential(String... keys) {
        Objects.requireNonNull(keys, "AzureKeyCredential requires 'keys' to be non-null.");
        if (keys.length == 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("AzureKeyCredential requires 'keys' to be"
                + "non-empty."));
        }

        for (String key : keys) {
            validateKey(key);
        }

        this.keys = CoreUtils.clone(keys);
    }

    /**
     * Retrieves the key associated to this credential.
     * <p>
     * If the credential has multiple keys this returns the first key.
     *
     * @return The key being used to authorize requests.
     */
    public String getKey() {
        return keys[0];
    }

    /**
     * Retrieves the keys associated to this credential.
     * <p>
     * Modifications to the array returned won't be reflected in the credential, if the credential needs to be updated
     * use {@link #update(String[])}.
     *
     * @return A read-only copy of the keys being used to authorize requests.
     */
    public String[] getKeys() {
        return CoreUtils.clone(keys);
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
        validateKey(key);

        synchronized (synchronizer) {
            // Should this throw if there was more than one key? Or should this set the first key in the keys?
            this.keys = new String[]{key};
        }

        return this;
    }

    /**
     * Rotates the key associated to this credential.
     *
     * @param keys The keys used to authorize requests.
     * @return The updated {@code AzureKeyCredential} object.
     * @throws NullPointerException If {@code keys} is null or if any key is null.
     * @throws IllegalArgumentException If {@code keys} is empty or any key is an empty string.
     */
    public AzureKeyCredential update(String... keys) {
        Objects.requireNonNull(keys, "AzureKeyCredential requires 'keys' to be non-null.");
        if (keys.length == 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("AzureKeyCredential requires 'keys' to be"
                + "non-empty."));
        }

        for (String key : keys) {
            validateKey(key);
        }

        synchronized (synchronizer) {
            // Should this throw if this.keys.length != keys.length?
            this.keys = CoreUtils.clone(keys);
        }

        return this;
    }

    private static void validateKey(String key) {
        Objects.requireNonNull(key, "AzureKeyCredential requires 'key' to be non-null.");

        if (key.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("AzureKeyCredential requires 'key' to be "
                + "non-empty."));
        }
    }
}
