// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.implementation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
public class FunctionEnumParameter {

    @JsonProperty(value = "type")
    private String type = "string";

    @JsonProperty(value = "enum")
    private List<String> enumValues;

    @JsonCreator
    public FunctionEnumParameter(List<String> enumValues) {
        this.enumValues = enumValues;
    }
}
