// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Representation of the response data from a moderation request. This is the result of checks of the input
 * against OpenAI's usage policies.
 */
@Immutable
public class Moderation {
    /*
     * A unique identifier associated with this chat completions response.
     */
    @Generated
    @JsonProperty(value = "id")
    private String id;

    /*
     * The model name that ran this request.
     */
    @Generated
    @JsonProperty(value = "model")
    private String model;

    /*
     * The collection of moderation results.
     */
    @Generated
    @JsonProperty(value = "results")
    private List<ModerationResults> results;

    /**
     * Creates a Moderation instance.
     *
     * @param id the id value to set
     * @param model the model value to set
     * @param results the results value to set
     */
    @Generated
    @JsonCreator
    public Moderation(@JsonProperty(value = "id") String id,
                      @JsonProperty(value = "model") String model,
                      @JsonProperty(value = "results") List<ModerationResults> results) {
        this.id = id;
        this.model = model;
        this.results = results;
    }

    /**
     * The unique id of the moderation request.
     *
     * @return the id value
     */
    @Generated
    public String getId() {
        return id;
    }

    /**
     * The model name used to verify the input.
     *
     * @return the model name
     */
    @Generated
    public String getModel() {
        return model;
    }

    /**
     * The list of results about the moderation request.
     *
     * @return the results of the moderation request
     */
    @Generated
    public List<ModerationResults> getResults() {
        return results;
    }
}
