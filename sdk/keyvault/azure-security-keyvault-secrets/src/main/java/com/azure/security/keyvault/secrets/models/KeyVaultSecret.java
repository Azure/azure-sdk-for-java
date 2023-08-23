// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.models;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;

import java.util.Objects;

/**
 *  Secret is the resource consisting of name, value and its attributes specified in {@link SecretProperties}.
 *  It is managed by Secret Service.
 *
 *  @see SecretClient
 *  @see SecretAsyncClient
 */
@Fluent
public class KeyVaultSecret {

    /**
     * The value of the secret.
     */
    private String value;

    /**
     * The secret properties.
     */
    private SecretProperties properties;

    /**
     * Creates an empty instance of the Secret.
     */
    KeyVaultSecret() {
        properties = new SecretProperties();
    }

    /**
     * Creates a Secret with {@code name} and {@code value}.
     *
     * @param name The name of the secret.
     * @param value the value of the secret.
     */
    public KeyVaultSecret(String name, String value) {
        properties = new SecretProperties(name);
        this.value = value;
    }

    /**
     * Get the value of the secret.
     *
     * @return the secret value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Get the secret identifier.
     *
     * @return the secret identifier.
     */
    public String getId() {
        return properties.getId();
    }

    /**
     * Get the secret name.
     *
     * @return the secret name.
     */
    public String getName() {
        return properties.getName();
    }

    /**
     * Get the secret properties
     * @return the Secret properties
     */
    public SecretProperties getProperties() {
        return this.properties;
    }

    /**
     * Set the secret properties
     * @param properties The Secret properties
     * @throws NullPointerException if {@code properties} is null.
     * @return the updated secret object
     */
    public KeyVaultSecret setProperties(SecretProperties properties) {
        Objects.requireNonNull(properties);
        properties.name = this.properties.name;
        this.properties = properties;
        return this;
    }
}

