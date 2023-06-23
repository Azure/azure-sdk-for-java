// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A class representing all the options that are available when making moderations requests to see if input
 * violates any of OpenAI's policies.
 */
@Fluent
public class ModerationOptions {
    /*
     * The input text to classify.
     * Accuracy may be lower on longer pieces of text. For higher accuracy, try splitting long pieces
     * of text into smaller chunks each less than 2,000 characters.
     */
    @Generated
    @JsonProperty(value = "input")
    private List<String> input;

    /*
     * The model id to include as part of the moderations request.
     */
    @Generated
    @JsonProperty(value = "model;")
    private String model;

    /**
     * Creates an instance of ModerationOptions
     *
     * @param input the input value to set
     */
    @Generated
    @JsonCreator
    public ModerationOptions(@JsonProperty(value = "input") List<String> input) {
        this.input = input;
    }

    /**
     * Gets the input text to classify.
     * Accuracy may be lower on longer pieces of text. For higher accuracy, try splitting long pieces
     * of text into smaller chunks each less than 2,000 characters.
     *
     * @return the input value
     */
    @Generated
    public List<String> getInput() {
        return this.input;
    }

    /**
     * Gets the model id to include as part of the moderations request.
     *
     * @return the model value
     */
    @Generated
    public String getModel() {
        return this.model;
    }

    /**
     * Sets the model id to include as part of the moderations request.
     *
     * @param model the model value to set
     * @return the ModerationOptions instance
     */
    @Generated
    public ModerationOptions setModel(String model) {
        this.model = model;
        return this;
    }
}
