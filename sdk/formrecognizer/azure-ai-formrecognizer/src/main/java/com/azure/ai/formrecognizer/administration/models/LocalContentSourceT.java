// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/** Local storage content. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonTypeName("local")
@Fluent
public final class LocalContentSourceT extends ContentSource {
    /*
     * Local Path.
     */
    @JsonProperty(value = "path", required = true)
    private String path;

    /**
     * Get the path property: Local Path.
     *
     * @return the path value.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Set the path property: Local Path.
     *
     * @param path the path value to set.
     * @return the LocalContentSourceT object itself.
     */
    public LocalContentSourceT setPath(String path) {
        this.path = path;
        return this;
    }
}
