// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.functions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Parameters {

    @JsonProperty(value = "type")
    private String type = "object";

    @JsonProperty(value = "properties")
    private Properties properties = new Properties();

    @JsonCreator
    public Parameters(
        @JsonProperty(value = "type")
        String type,
        @JsonProperty(value = "properties")
        Properties properties
    ) {
        this.type = type;
        this.properties = properties;
    }

    @JsonCreator
    public Parameters() {}

    @JsonGetter
    public String getType() {
        return type;
    }

    @JsonSetter
    public void setType(String type) {
        this.type = type;
    }

    @JsonGetter
    public Properties getProperties() {
        return properties;
    }

    @JsonSetter
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
