// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Base64Url;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class that contains the policy rules under which the key can be exported.
 */
@Fluent
public class KeyReleasePolicy {
    /**
     * Content type and version of key release policy.
     */
    @JsonProperty(value = "contentType")
    private String contentType;

    /**
     * Blob encoding the policy rules under which the key can be exported.
     */
    @JsonProperty(value = "data")
    private Base64Url data;

    /**
     * Get the content type of the release policy.
     *
     * @return The content type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Set the content type of the release policy.
     *
     * @param contentType The content type to set.
     * @return The updated {@link KeyReleasePolicy} object.
     */
    public KeyReleasePolicy setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set the blob encoding the policy rules under which the key can be exported.
     *
     * @param data Blob encoding the policy rules under which the key can be exported.
     * @return The updated {@link KeyReleasePolicy} object.
     */
    public KeyReleasePolicy setData(byte[] data) {
        this.data = Base64Url.encode(data);
        return this;
    }

    /**
     * Get the policy rules under which the key can be exported.
     *
     * @return The policy rules represented by a blob.
     */
    public byte[] getData() {
        return this.data == null ? null : this.data.decodedBytes();
    }
}
