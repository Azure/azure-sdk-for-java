// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.models;

import com.azure.core.annotation.Immutable;

/**
 * A single bucket of a range facet query result that reports the number of documents
 * with a field value falling within a particular range.
 */
@Immutable
public class RangeFacetResult {
    private final Long count;
    private final Object from;
    private final Object to;

    /**
     * Constructor
     *
     * @param facetResult facet result object
     */
    public RangeFacetResult(FacetResult facetResult) {
        count = facetResult.getCount();
        from = facetResult.getDocument().get("from");
        to = facetResult.getDocument().get("to");
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
     * Gets a value indicating the inclusive lower bound of the facet's range, or null to indicate that there is
     * no lower bound (i.e. -- for the first bucket).
     * @return from
     */
    public Object getFrom() {
        return from;
    }

    /**
     * Gets a value indicating the exclusive upper bound of the facet's range, or null to indicate that there is
     * no upper bound (i.e. -- for the last bucket).
     *
     * @return to
     */
    public Object getTo() {
        return to;
    }
}
