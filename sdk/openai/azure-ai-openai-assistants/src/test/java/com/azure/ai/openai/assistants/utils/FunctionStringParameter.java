package com.azure.ai.openai.assistants.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FunctionStringParameter {

    @JsonProperty(value = "type")
    private String type = "string";

    @JsonProperty(value = "description")
    private String description;

    @JsonCreator
    public FunctionStringParameter(String description) {
        this.description = description;
    }
}
