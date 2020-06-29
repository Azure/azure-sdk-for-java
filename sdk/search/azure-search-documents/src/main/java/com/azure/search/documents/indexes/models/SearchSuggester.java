// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Defines how the Suggest API should apply to a group of fields in the index.
 */
@Fluent
public final class SearchSuggester {
    /*
     * The name of the suggester.
     */
    @JsonProperty(value = "name", required = true)
    private String name;

    /*
     * The list of field names to which the suggester applies. Each field must
     * be searchable.
     */
    @JsonProperty(value = "sourceFields", required = true)
    private List<String> sourceFields;

    /**
     * Constructor of {@link SearchSuggester}.
     * @param name The name of the suggester.
     * @param sourceFields The list of field names to which the suggester applies. Each field must
     * be searchable.
     */
    @JsonCreator
    public SearchSuggester(
        @JsonProperty(value = "name") String name,
        @JsonProperty(value = "sourceFields") List<String> sourceFields) {
        this.name = name;
        this.sourceFields = sourceFields;
    }

    /**
     * Get the name property: The name of the suggester.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the sourceFields property: The list of field names to which the
     * suggester applies. Each field must be searchable.
     *
     * @return the sourceFields value.
     */
    public List<String> getSourceFields() {
        return this.sourceFields;
    }

}
