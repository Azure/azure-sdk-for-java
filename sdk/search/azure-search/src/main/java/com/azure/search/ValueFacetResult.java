// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.search.models.FacetResult;

public class ValueFacetResult {
    private final Long count;
    private final Object value;

    /**
     * Constructor
     *
     * @param facetResult facet result object
     */
    public ValueFacetResult(FacetResult facetResult) {
        this.count = facetResult.getCount();
        this.value = facetResult.getDocument().get("value");
    }

    /**
     * Constructor
     *
     * @param count count
     * @param value value
     */
    public ValueFacetResult(Long count, Object value) {
        this.count = count;
        this.value = value;
    }

    /**
     * Get count
     *
     * @return count
     */
    public Long count() {
        return count;
    }

    /**
     * Get value
     *
     * @return value
     */
    public Object value() {
        return value;
    }
}
