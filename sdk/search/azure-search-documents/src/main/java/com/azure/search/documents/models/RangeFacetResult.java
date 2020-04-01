// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Immutable;

/**
 * A single bucket of a range facet query result that reports the number of documents
 * with a field value falling within a particular range.
 */
@Immutable
public class RangeFacetResult<T> {
    private static final String FROM = "from";
    private static final String TO = "to";
    private final Long count;
    private final T from;
    private final T to;

    /**
     * Constructor of RangeFacetResult.
     *
     * @param count The count of the result.
     * @param from Value indicates the lower bound of facet's range
     * @param to Value indicates the upper bound of facet's range
     */
    public RangeFacetResult(Long count, T from, T to) {
        this.count = count;
        this.from = from;
        this.to = to;
    }

    /**
     * Constructor from {@link FacetResult}
     *
     * @param facetResult {@link FacetResult}.
     */
    @SuppressWarnings("unchecked")
    public RangeFacetResult(FacetResult facetResult) {
        this.count = facetResult.getCount();
        this.from = (T) facetResult.getAdditionalProperties().get(FROM);
        this.to = (T) facetResult.getAdditionalProperties().get(TO);
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
    public T getFrom() {
        return from;
    }

    /**
     * Gets a value indicating the exclusive upper bound of the facet's range, or null to indicate that there is
     * no upper bound (i.e. -- for the last bucket).
     *
     * @return to
     */
    public T getTo() {
        return to;
    }
}
