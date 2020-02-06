// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.search.models.SuggestDocumentsResult;
import com.azure.search.models.SuggestResult;

import java.util.stream.Collectors;

/**
 * Represents an HTTP response from the suggest API request
 * that contains a list of items deserialized into a {@link Page}.
 * Each page contains additional information returned by the API request. In the Suggest API case
 * the additional information is:
 * coverage - coverage value.
 */
public class SuggestPagedResponse extends PagedResponseBase<String, SuggestResult> {

    /**
     * Get coverage
     *
     * @return Double
     */
    public Double getCoverage() {
        return coverage;
    }

    private final Double coverage;

    /**
     * Constructor
     *
     * @param documentSearchResponse an http response with the results
     */
    public SuggestPagedResponse(SimpleResponse<SuggestDocumentsResult> documentSearchResponse) {
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
