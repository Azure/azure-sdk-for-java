// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Defines how the Suggest API should apply to a group of fields in the index.
 */
@Fluent
public final class SearchSuggester {
    /*
     * A value indicating the capabilities of the suggester.
     */
    @JsonProperty(value = "searchMode", required = true)
    private static final String SEARCH_MODE = "analyzingInfixMatching";

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
     * Get the name property: The name of the suggester.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: The name of the suggester.
     *
     * @param name the name value to set.
     * @return the Suggester object itself.
     */
    public SearchSuggester setName(String name) {
        this.name = name;
        return this;
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

    /**
     * Set the sourceFields property: The list of field names to which the
     * suggester applies. Each field must be searchable.
     *
     * @param sourceFields the sourceFields value to set.
     * @return the Suggester object itself.
     */
    public SearchSuggester setSourceFields(List<String> sourceFields) {
        this.sourceFields = sourceFields;
        return this;
    }
}
