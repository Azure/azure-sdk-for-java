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
    private String value;

    /**
     * Creates an empty instance of the Secret.
     */
    public Secret() {

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
    public String getValue() {
        return this.value;
    }

    /**
     * Set enabled value.
     *
     * @param enabled The enabled value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret setEnabled(Boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The not before time value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret setNotBefore(OffsetDateTime notBefore) {
        super.setNotBefore(notBefore);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expires The expiry time value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret setExpires(OffsetDateTime expires) {
        super.setExpires(expires);
        return this;
    }

    /**
     * Set the secret identifier.
     *
     * @param id The secret identifier value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret setId(String id) {
        super.setId(id);
        return this;
    }

    /**
     * Set the content type of the secret.
     *
     * @param contentType The content type value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret setContentType(String contentType) {
        super.setContentType(contentType);
        return this;
    }

    /**
     * Set the tags.
     *
     * @param tags The tags value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret setTags(Map<String, String> tags) {
        super.setTags(tags);
        return this;
    }

}

