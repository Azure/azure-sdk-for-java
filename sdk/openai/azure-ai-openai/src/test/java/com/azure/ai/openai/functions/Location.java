// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.functions;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Location {

    /**
     * type defines the JSON type of the value that the service will request if a @FunctionCall is requested
     */
    @JsonProperty(value = "type")
    private String type = "string";

    /**
     * Examples provided in the description appear to be used verbatim. Such as "San Francisco, CA"
     */
    @JsonProperty(value = "description")
    private String description = "The city and state, e.g. San Francisco, CA";

    @JsonGetter
    public String getType() {
        return type;
    }

    @JsonSetter
    public void setType(String type) {
        this.type = type;
    }

    @JsonGetter
    public String getDescription() {
        return description;
    }

    @JsonSetter
    public void setDescription(String description) {
        this.description = description;
    }
}
