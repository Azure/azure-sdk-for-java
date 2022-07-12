// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/** Content represented via Base64 encoding. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonTypeName("base64")
@Fluent
public final class Base64ContentSource extends ContentSource {
    /*
     * Base64 encoded content data.
     */
    @JsonProperty(value = "data")
    private byte[] data;

    /**
     * Get the data property: Base64 encoded content data.
     *
     * @return the data value.
     */
    public byte[] getData() {
        return CoreUtils.clone(this.data);
    }

    /**
     * Set the data property: Base64 encoded content data.
     *
     * @param data the data value to set.
     * @return the Base64ContentSource object itself.
     */
    public Base64ContentSource setData(byte[] data) {
        this.data = CoreUtils.clone(data);
        return this;
    }
}
