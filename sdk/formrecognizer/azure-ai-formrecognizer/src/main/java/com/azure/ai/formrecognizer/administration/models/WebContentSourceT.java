// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/** Publicly accessible Internet content. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonTypeName("web")
@Fluent
public final class WebContentSourceT extends ContentSource {
    /*
     * Publicly accessible URL.
     */
    @JsonProperty(value = "url", required = true)
    private String url;

    /**
     * Get the url property: Publicly accessible URL.
     *
     * @return the url value.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the url property: Publicly accessible URL.
     *
     * @param url the url value to set.
     * @return the WebContentSourceT object itself.
     */
    public WebContentSourceT setUrl(String url) {
        this.url = url;
        return this;
    }
}
