// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.functions;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Arrays;
import java.util.List;

public class Unit {
    @JsonProperty(value = "type")
    private String type = "string";

    @JsonProperty(value = "enum")
    private List<String> enumValues = Arrays.asList("CELSIUS", "FAHRENHEIT");

    @JsonGetter
    public String getType() {
        return type;
    }

    @JsonSetter
    public void setType(String type) {
        this.type = type;
    }

    @JsonGetter
    public List<String> getEnumValues() {
        return enumValues;
    }

    @JsonSetter
    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }
}
