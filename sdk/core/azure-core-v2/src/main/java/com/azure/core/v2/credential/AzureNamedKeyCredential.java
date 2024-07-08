// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.credential;

import io.clientcore.core.util.ClientLogger;

import java.util.Objects;

/**
 * <p>
 * The {@link AzureNamedKeyCredential} is used to authenticate and authorize requests made to Azure services.
 * It is specifically designed for scenarios where you need to authenticate using a key with a name identifier
 * associated with it.
 * </p>
 *
 * <p>
 * A key is a unique identifier or token that is associated with a specific user or application. It serves as a
 * simple form of authentication to ensure that only authorized clients can access the protected resources or APIs.
 * This authentication is commonly used for accessing certain services, such as Azure Tables and Azure Event Hubs.
 * Each service may have its own specific way of using API keys, but the general concept remains the same.
 * </p>
 *
 * <p>
 * The {@link com.azure.core.v2.credential.AzureNamedKeyCredential} can be created for keys which have a name
 * identifier associated with them.
 * </p>
 *
 * <p>
 * <strong>Code Samples</strong>
 * </p>
 *
 * <p>
 * Create a named credential for a service specific sas key.
 * </p>
 *
 * <!-- src_embed com.azure.core.credential.azureNamedKeyCredenialSasKey -->
 * <pre>
 * AzureNamedKeyCredential azureNamedKeyCredential =
 *     new AzureNamedKeyCredential&#40;&quot;AZURE-SERVICE-SAS-KEY-NAME&quot;, &quot;AZURE-SERVICE-SAS-KEY&quot;&#41;;
 * </pre>
 * <!-- end com.azure.core.credential.azureNamedKeyCredenialSasKey -->
 *
 * @see com.azure.core.credential
 */
public final class AzureNamedKeyCredential {
    // AzureNamedKeyCredential is a commonly used credential type, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(AzureNamedKeyCredential.class);

    private volatile AzureNamedKey credentials;

    /**
     * Creates a credential with specified {@code name} that authorizes request with the given {@code key}.
     *
     * @param name The name of the key credential.
     * @param key The key used to authorize requests.
     * @throws NullPointerException If {@code key} or {@code name} is {@code null}.
     * @throws IllegalArgumentException If {@code key} or {@code name} is an empty string.
     */
    public AzureNamedKeyCredential(String name, String key) {
        validateInputParameters(name, key);
        this.credentials = new AzureNamedKey(name, key);
    }

    /**
     * Retrieves the {@link AzureNamedKey} containing the name and key associated with this credential.
     *
     * @return The {@link AzureNamedKey} containing the name and key .
     */
    public AzureNamedKey getAzureNamedKey() {
        return this.credentials;
    }

    /**
     * Rotates the {@code name} and  {@code key} associated to this credential.
     *
     * @param name The new name of the key credential.
     * @param key The new key to be associated with this credential.
     * @return The updated {@code AzureNamedKeyCredential} object.
     * @throws NullPointerException If {@code key} or {@code name} is {@code null}.
     * @throws IllegalArgumentException If {@code key} or {@code name} is an empty string.
     */
    public AzureNamedKeyCredential update(String name, String key) {
        validateInputParameters(name, key);
        this.credentials = new AzureNamedKey(name, key);
        return this;
    }

    private void validateInputParameters(String name, String key) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(key, "'key' cannot be null.");
        if (name.isEmpty()) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'name' cannot be empty."));
        }
        if (key.isEmpty()) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'key' cannot be empty."));
        }
    }
}
