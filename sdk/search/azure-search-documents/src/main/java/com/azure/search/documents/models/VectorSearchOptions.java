// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import java.util.Arrays;
import java.util.List;

/**
 * Parameters for performing vector searches.
 */
public final class VectorSearchOptions {
    private VectorFilterMode filterMode;
    private List<VectorQuery> queries;

    /**
     * Creates a new instance of {@link VectorSearchOptions}.
     */
    public VectorSearchOptions() {
    }

    /**
     * Gets the filter mode to apply to vector queries.
     *
     * @return The filter mode to apply to vector queries.
     */
    public VectorFilterMode getFilterMode() {
        return filterMode;
    }

    /**
     * Sets the filter mode to apply to vector queries.
     *
     * @param filterMode The filter mode to apply to vector queries.
     * @return The VectorSearchOptions object itself.
     */
    public VectorSearchOptions setFilterMode(VectorFilterMode filterMode) {
        this.filterMode = filterMode;
        return this;
    }

    /**
     * Gets the list of vector queries to perform.
     *
     * @return The list of vector queries to perform.
     */
    public List<VectorQuery> getQueries() {
        return queries;
    }

    /**
     * Sets the list of vector queries to perform.
     *
     * @param queries The list of vector queries to perform.
     * @return The VectorSearchOptions object itself.
     */
    public VectorSearchOptions setQueries(VectorQuery... queries) {
        this.queries = queries == null ? null : Arrays.asList(queries);
        return this;
    }

    /**
     * Sets the list of vector queries to perform.
     *
     * @param queries The list of vector queries to perform.
     * @return The VectorSearchOptions object itself.
     */
    public VectorSearchOptions setQueries(List<VectorQuery> queries) {
        this.queries = queries;
        return this;
    }
}
