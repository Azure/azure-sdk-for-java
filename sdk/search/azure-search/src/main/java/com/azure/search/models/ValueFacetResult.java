// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.models;


import com.azure.core.annotation.Immutable;

/**
 * A single bucket of a simple or interval facet query result that reports the number of documents with a field
 * falling within a particular interval or having a specific value.
 */
@Immutable
public class ValueFacetResult<T> {
    private final Long count;
    private final T value;

    /**
     * Constructor
     *
     * @param count The approximate count of documents.
     * @param value The value of the facet.
     */
    public ValueFacetResult(Long count, T value) {
        this.count = count;
        this.value = value;
    }

    /**
     * Gets the approximate count of documents falling within the bucket described by this facet.
     *
     * @return count
     */
    public Long getCount() {
        return count;
    }

    /**
     * Gets the value of the facet, or the inclusive lower bound if it's an interval facet.
     *
     * @return value
     */
    public Object getValue() {
        return value;
    }
}
