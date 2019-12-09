// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.models;

/**
 * TODO: add class desc
 */
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
     * Get count
     *
     * @return count
     */
    public Long getCount() {
        return count;
    }

    /**
     * Get from
     *
     * @return from
     */
    public Object getFrom() {
        return from;
    }

    /**
     * Get to
     *
     * @return to
     */
    public Object getTo() {
        return to;
    }
}
