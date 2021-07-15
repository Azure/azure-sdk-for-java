// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model used for testing {@link JsonFlatten}.
 */
@Fluent
public final class JsonFlattenOnJsonIgnoredProperty {
    @JsonProperty("name")
    private String name;

    @JsonIgnore
    @JsonFlatten
    @JsonProperty("jsonflatten.ignored")
    private String ignored;

    public JsonFlattenOnJsonIgnoredProperty setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public JsonFlattenOnJsonIgnoredProperty setIgnored(String ignored) {
        this.ignored = ignored;
        return this;
    }

    public String getIgnored() {
        return ignored;
    }
}
