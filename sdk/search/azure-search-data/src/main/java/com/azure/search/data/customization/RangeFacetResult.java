// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.search.data.generated.models.FacetResult;

public class RangeFacetResult {
    private final Long count;
    private final Object from;
    private final Object to;

    /**
     * Constructor
     * @param facetResult facet result object
     */
    public RangeFacetResult(FacetResult facetResult) {
        count = facetResult.count();
        from = facetResult.additionalProperties().get("from");
        to = facetResult.additionalProperties().get("to");
    }

    /**
     * Get cont
     * @return count
     */
    public long count() {
        return count;
    }

    /**
     * Get from
     * @return from
     */
    public Object from() {
        return from;
    }

    /**
     * Get to
     * @return to
     */
    public Object to() {
        return to;
    }
}
