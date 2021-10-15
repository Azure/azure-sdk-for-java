// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.keys.implementation.Base64UrlJsonDeserializer;
import com.azure.security.keyvault.keys.implementation.Base64UrlJsonSerializer;
import com.azure.security.keyvault.keys.implementation.ByteExtensions;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

/**
 * A model that represents the policy rules under which the key can be exported.
 */
@Fluent
public final class KeyReleasePolicy {
    /*
     * Blob encoding the policy rules under which the key can be released.
     */
    @JsonProperty(value = "data")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    private byte[] data;

    /*
     * Content type and version of key release policy.
     */
    @JsonProperty(value = "contentType")
    private String contentType;

    KeyReleasePolicy() {
        // Empty constructor for Jackson Deserialization
    }

    /**
     * Creates an instance of {@link KeyReleasePolicy}.
     *
     * @param data A blob encoding the policy rules under which the key can be released.
     */
    public KeyReleasePolicy(byte[] data) {
        Objects.requireNonNull(data, "'data' cannot be null.");

        this.data = ByteExtensions.clone(data);
    }

    /**
     * Get a blob encoding the policy rules under which the key can be released.
     *
     * @return A blob encoding the policy rules under which the key can be released.
     */
    public byte[] getData() {
        return ByteExtensions.clone(this.data);
    }

    /**
     * Get the content type and version of key release policy.
     *
     * @return The content type and version of key release policy.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the content type and version of key release policy.
     *
     * <p>The service default is "application/json; charset=utf-8".</p>
     *
     * @param contentType The content type and version of key release policy to set.
     *
     * @return The updated {@link KeyReleasePolicy} object.
     */
    public KeyReleasePolicy setContentType(String contentType) {
        this.contentType = contentType;

        return this;
    }
}
