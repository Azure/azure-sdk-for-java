// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import com.azure.core.util.CoreUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Parameters for performing vector searches.
 */
public final class VectorSearchOptions {
    private final List<Float> value;
    private Integer kNearestNeighborsCount;
    private List<String> fields;

    /**
     * Creates a new instance of {@link VectorSearchOptions} with the vector representation of a search query.
     *
     * @param value The vector representation of a search query.
     */
    public VectorSearchOptions(List<Float> value) {
        this.value = value;
    }

    /**
     * Get the value property: The vector representation of a search query.
     *
     * @return the value value.
     */
    public List<Float> getValue() {
        return this.value;
    }

    /**
     * Get the kNearestNeighborsCount property: Number of nearest neighbors to return as top hits.
     *
     * @return the kNearestNeighborsCount value.
     */
    public Integer getKNearestNeighborsCount() {
        return this.kNearestNeighborsCount;
    }

    /**
     * Set the kNearestNeighborsCount property: Number of nearest neighbors to return as top hits.
     *
     * @param kNearestNeighborsCount the kNearestNeighborsCount value to set.
     * @return the SearchQueryVector object itself.
     */
    public VectorSearchOptions setKNearestNeighborsCount(Integer kNearestNeighborsCount) {
        this.kNearestNeighborsCount = kNearestNeighborsCount;
        return this;
    }

    /**
     * Get the fields property: Vector Fields of type Collection(Edm.Single) to be included in the vector searched.
     *
     * @return the fields value.
     */
    public List<String> getFields() {
        return this.fields;
    }

    /**
     * Set the fields property: Vector Fields of type Collection(Edm.Single) to be included in the vector searched.
     *
     * @param fields the fields value to set.
     * @return the SearchQueryVector object itself.
     */
    public VectorSearchOptions setFields(List<String> fields) {
        this.fields = fields;
        return this;
    }

    /**
     * Set the fields property: Vector Fields of type Collection(Edm.Single) to be included in the vector searched.
     *
     * @param fields the fields value to set.
     * @return the SearchQueryVector object itself.
     */
    public VectorSearchOptions setFields(String... fields) {
        this.fields = CoreUtils.isNullOrEmpty(fields) ? null : Arrays.asList(fields);
        return this;
    }
}
