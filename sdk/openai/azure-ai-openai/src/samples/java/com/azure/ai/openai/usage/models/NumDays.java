// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.usage.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class NumDays {

    @JsonProperty(value = "type")
    private String type = "integer";
    @JsonProperty(value = "description")
    private String description = "The number of days to forecast";

    @JsonGetter
    public String getType() {
        return type;
    }

    @JsonSetter
    public NumDays setType(String type) {
        this.type = type;
        return this;
    }

    @JsonGetter
    public String getDescription() {
        return description;
    }

    @JsonSetter
    public NumDays setDescription(String description) {
        this.description = description;
        return this;
    }
}
