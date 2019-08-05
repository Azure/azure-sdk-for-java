// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.models;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 *  Secret is the resource consisting of name, value and its attributes inherited from {@link SecretBase}.
 *  It is managed by Secret Service.
 *
 *  @see SecretClient
 *  @see SecretAsyncClient
 */
public class Secret extends SecretBase {

    /**
     * The value of the secret.
     */
    @JsonProperty(value = "value")
    private final String value;

    /**
     * Creates an empty instance of the Secret.
     */
    public Secret() {
        this(null, null);
    }

    /**
     * Creates a Secret with {@code name} and {@code value}.
     *
     * @param name The name of the secret.
     * @param value the value of the secret.
     */
    public Secret(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Get the value of the secret.
     *
     * @return the secret value
     */
    public String value() {
        return this.value;
    }

    /**
     * Set enabled value.
     *
     * @param enabled The enabled value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret enabled(Boolean enabled) {
        super.enabled(enabled);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The not before time value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret notBefore(OffsetDateTime notBefore) {
        super.notBefore(notBefore);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expires The expiry time value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret expires(OffsetDateTime expires) {
        super.expires(expires);
        return this;
    }

    /**
     * Set the secret identifier.
     *
     * @param id The secret identifier value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret id(String id) {
        super.id(id);
        return this;
    }

    /**
     * Set the content type of the secret.
     *
     * @param contentType The content type value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret contentType(String contentType) {
        super.contentType(contentType);
        return this;
    }

    /**
     * Set the tags.
     *
     * @param tags The tags value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret tags(Map<String, String> tags) {
        super.tags(tags);
        return this;
    }

}

