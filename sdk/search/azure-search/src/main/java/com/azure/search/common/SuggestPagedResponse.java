// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.common;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.search.models.DocumentSuggestResult;
import com.azure.search.models.SuggestResult;

import java.util.stream.Collectors;

public class SuggestPagedResponse extends PagedResponseBase<String, SuggestResult> {

    /**
     * Get coverage
     *
     * @return Double
     */
    public Double coverage() {
        return coverage;
    }

    private final Double coverage;

    /**
     * Constructor
     *
     * @param documentSearchResponse an http response with the results
     */
    public SuggestPagedResponse(SimpleResponse<DocumentSuggestResult> documentSearchResponse) {
        super(documentSearchResponse.getRequest(),
            documentSearchResponse.getStatusCode(),
            documentSearchResponse.getHeaders(),
            documentSearchResponse.getValue().getResults(),
            null,
            deserializeHeaders(documentSearchResponse.getHeaders()));
        this.coverage = documentSearchResponse.getValue().getCoverage();
    }

    private static String deserializeHeaders(HttpHeaders headers) {
        return headers.toMap().entrySet().stream().map((entry) ->
            entry.getKey() + "," + entry.getValue()
        ).collect(Collectors.joining(","));
    }
}
