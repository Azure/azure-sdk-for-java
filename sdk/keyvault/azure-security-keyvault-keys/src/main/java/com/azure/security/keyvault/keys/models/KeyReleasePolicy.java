// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Base64Url;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A model that represents the policy rules under which the key can be exported.
 */
@Fluent
public final class KeyReleasePolicy {
    /*
     * Content type and version of key release policy.
     */
    @JsonProperty(value = "contentType")
    private String contentType;

    /*
     * Blob encoding the policy rules under which the key can be released.
     */
    @JsonProperty(value = "data")
    private Base64Url data;

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

    /**
     * Get a blob encoding the policy rules under which the key can be released.
     *
     * @return A blob encoding the policy rules under which the key can be released.
     */
    public byte[] getData() {
        if (this.data == null) {
            return null;
        }

        return this.data.decodedBytes();
    }

    /**
     * Set a blob encoding the policy rules under which the key can be released.
     *
     * @param data A blob encoding the policy rules under which the key can be released.
     *
     * @return The updated {@link KeyReleasePolicy} object.
     */
    public KeyReleasePolicy setData(byte[] data) {
        if (data == null) {
            this.data = null;
        } else {
            this.data = Base64Url.encode(CoreUtils.clone(data));
        }

        return this;
    }
}
