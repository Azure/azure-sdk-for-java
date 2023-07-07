// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.functions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;


//MyFunctionParameters parameters = new MyFunctionParameters();
//    parameters.type = "object";
//    parameters.properties = new HashMap<>();
//    parameters.properties.put(
//    "location",
//    new MyFunctionParameters.FunctionProperty(
//    "string",
//    "The city and state, e.g. San Francisco, CA",
//    null));
//    parameters.properties.put(
//    "unit",
//    new MyFunctionParameters.FunctionProperty(
//    "string",
//    "Unit temperature",
//    Arrays.asList("Celsius", "Fahrenheit")));
//    parameters.required = Arrays.asList("location");
//
//    FunctionDefinition functionDefinition = new FunctionDefinition("MyFunction");
//    functionDefinition.setParameters(parameters);

public class MyFunctionParameters {
    @JsonProperty
    public String type;

    @JsonProperty
    public Map<String, MyFunctionParameter> properties;

    @JsonProperty
    public List<String> required;

}

