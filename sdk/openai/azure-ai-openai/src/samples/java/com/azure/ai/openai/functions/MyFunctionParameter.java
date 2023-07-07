// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.functions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MyFunctionParameter {

    @JsonCreator
    public MyFunctionParameter(String type, String description, List<String> enumValues) {
        this.type = type;
        this.description = description;
        this.enumValues = enumValues;
    }

    @JsonProperty
    public final String type;
    @JsonProperty
    public final String description;
    @JsonProperty(value = "enum")
    public final List<String> enumValues;
}
