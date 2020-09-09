// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Provides parameter values to a tag scoring function.
 */
@Fluent
public final class TagScoringParameters {
    /*
     * The name of the parameter passed in search queries to specify the list
     * of tags to compare against the target field.
     */
    @JsonProperty(value = "tagsParameter", required = true)
    private String tagsParameter;

    /**
     * Constructor of {@link TagScoringParameters}
     * @param tagsParameter The name of the parameter passed in search queries to specify the list
     * of tags to compare against the target field.
     */
    @JsonCreator
    public TagScoringParameters(@JsonProperty(value = "tagsParameter", required = true) String tagsParameter) {
        this.tagsParameter = tagsParameter;
    }

    /**
     * Get the tagsParameter property: The name of the parameter passed in
     * search queries to specify the list of tags to compare against the target
     * field.
     *
     * @return the tagsParameter value.
     */
    public String getTagsParameter() {
        return this.tagsParameter;
    }

}
