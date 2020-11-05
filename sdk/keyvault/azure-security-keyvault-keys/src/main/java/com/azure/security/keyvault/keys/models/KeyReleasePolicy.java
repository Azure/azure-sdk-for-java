// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class that contains the policy rules under which the key can be exported.
 */
@Fluent
public class KeyReleasePolicy {
    /**
     * Content type and version of key release policy. The default value is:
     * 'application/json; charset=utf-8; version=1.0'.
     */
    @JsonProperty(value = "contentType")
    private String contentType = "application/json; charset=utf-8; version=1.0";

    /**
     * Blob encoding the policy rules under which the key can be exported.
     */
    @JsonProperty(value = "data")
    private byte[] data;

    /**
     * Creates an instance of {@link KeyReleasePolicy}.
     *
     * @param data Blob encoding the policy rules under which the key can be exported.
     */
    public KeyReleasePolicy(byte[] data) {
        this.data = new byte[data.length];

        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    /**
     * Get the content type of the key release policy.
     *
     * @return The content type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Set the content type of the key release policy. The default value is:
     * 'application/json; charset=utf-8; version=1.0'.
     *
     * @param contentType The content type to set.
     * @return The updated {@link KeyReleasePolicy} object.
     */
    public KeyReleasePolicy setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get the policy rules under which the key can be exported.
     *
     * @return The policy rules represented by a blob.
     */
    public byte[] getData() {
        return data.clone();
    }
}
