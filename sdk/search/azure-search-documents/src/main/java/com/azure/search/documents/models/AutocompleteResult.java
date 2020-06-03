// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The result of Autocomplete query.
 */
@Fluent
public final class AutocompleteResult {
    /*
     * A value indicating the percentage of the index that was considered by
     * the autocomplete request, or null if minimumCoverage was not specified
     * in the request.
     */
    @JsonProperty(value = "@search.coverage", access = JsonProperty.Access.WRITE_ONLY)
    private Double coverage;

    /*
     * The list of returned Autocompleted items.
     */
    @JsonProperty(value = "value", required = true, access = JsonProperty.Access.WRITE_ONLY)
    private List<AutocompleteItem> results;

    /**
     * Get the coverage property: A value indicating the percentage of the
     * index that was considered by the autocomplete request, or null if
     * minimumCoverage was not specified in the request.
     *
     * @return the coverage value.
     */
    public Double getCoverage() {
        return this.coverage;
    }

    /**
     * Get the results property: The list of returned Autocompleted items.
     *
     * @return the results value.
     */
    public List<AutocompleteItem> getResults() {
        return this.results;
    }
}
