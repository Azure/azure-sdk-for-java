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

    private final Map<String, List<FacetResult>> facets;
    private final Long count;

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
    }

}
