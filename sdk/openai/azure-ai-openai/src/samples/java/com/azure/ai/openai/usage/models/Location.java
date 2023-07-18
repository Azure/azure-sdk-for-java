// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Location {
    @JsonProperty(value = "type")
    private String type = "string";
    @JsonProperty(value = "description")
    private String description = "The city and state, e.g. San Francisco, CA";

    @JsonGetter
    public String getType() {
        return type;
    }

    @JsonSetter
    public Location setType(String type) {
        this.type = type;
        return this;
    }

    @JsonGetter
    public String getDescription() {
        return description;
    }

    @JsonSetter
    public Location setDescription(String description) {
        this.description = description;
        return this;
    }
}
