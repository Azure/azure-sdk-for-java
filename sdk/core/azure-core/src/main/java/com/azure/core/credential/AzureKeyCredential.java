// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * <p>The {@link AzureKeyCredential} is used to authenticate and authorize requests made to Azure services.
 * It is specifically designed for scenarios where you need to authenticate using a key.</p>
 *
 * <p>A key is a unique identifier or token that is associated with a specific user or application. It serves as a
 * simple form of authentication to ensure that only authorized clients can access the protected resources or APIs.
 * This authentication is commonly used for accessing certain services, such as Azure Cognitive Services, Azure Search,
 * or Azure Management APIs. Each service may have its own specific way of using API keys, but the general concept
 * remains the same. The {@link com.azure.core.credential.AzureKeyCredential} allows you to authenticate
 * using a key.</p>
 *
 * <p><strong>Code Samples</strong></p>
 *
 * <p>Create a key credential for a service key.</p>
 *
 * <!-- src_embed com.azure.core.credential.azureKeyCredential -->
 * <pre>
 * AzureKeyCredential azureKeyCredential = new AzureKeyCredential&#40;&quot;AZURE-SERVICE-KEY&quot;&#41;;
 * </pre>
 * <!-- end com.azure.core.credential.azureKeyCredential -->
 *
 * @see com.azure.core.credential
 */
public final class AzureKeyCredential {
    // AzureKeyCredential is a commonly used credential type, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(AzureKeyCredential.class);
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
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'key' cannot be empty."));
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
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'key' cannot be empty."));
        }

        this.key = key;
        return this;
    }
}
