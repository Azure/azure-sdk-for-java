// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.common;

import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.search.data.generated.models.DocumentSearchResult;
import com.azure.search.data.generated.models.FacetResult;
import com.azure.search.data.generated.models.SearchResult;
import java.util.List;
import java.util.Map;

public class SearchPagedResponse extends PagedResponseBase<String, SearchResult> {

    /**
     * Get facets
     * @return  Map<String, List<FacetResult>>
     */
    public Map<String, List<FacetResult>> getFacets() {
        return facets;
    }

    private final Map<String, List<FacetResult>> facets;

    /**
     * Get count
     * @return Long
     */
    public Long getCount() {
        return count;
    }

    private final Long count;

    /**
     * Get coverage
     * @return Double
     */
    public Double getCoverage() {
        return coverage;
    }

    private final Double coverage;

    /**
     * Constructor
     * @param documentSearchResponse an http response with the results
     */
    public SearchPagedResponse(SimpleResponse<DocumentSearchResult> documentSearchResponse) {
        super(documentSearchResponse.request(),
            documentSearchResponse.statusCode(),
            documentSearchResponse.headers(),
            documentSearchResponse.value().results(),
            documentSearchResponse.value().nextLink(),
            "");

        this.facets = documentSearchResponse.value().facets();
        this.count = documentSearchResponse.value().count();
        this.coverage = documentSearchResponse.value().coverage();
    }

}
