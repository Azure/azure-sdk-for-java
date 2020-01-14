// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.CoreUtils;
import com.azure.search.models.FacetResult;
import com.azure.search.models.SearchDocumentsResult;
import com.azure.search.models.SearchRequest;
import com.azure.search.models.SearchResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Represents an HTTP response from the search API request
 * that contains a list of items deserialized into a {@link Page}.
 * Each page contains additional information returned by the API request. In the Search API case
 * the additional information is:
 * count - number of total documents returned. Will be returned only if isIncludeTotalResultCount is set to true
 * coverage - coverage value.
 */
public class SearchPagedResponse extends PagedResponseBase<String, SearchResult> {

    private final Map<String, List<FacetResult>> facets;
    private final Long count;
    private final Double coverage;

    /**
     * Constructor
     *
     * @param documentSearchResponse an http response with the results
     */
    public SearchPagedResponse(SimpleResponse<SearchDocumentsResult> documentSearchResponse) {
        super(documentSearchResponse.getRequest(),
            documentSearchResponse.getStatusCode(),
            documentSearchResponse.getHeaders(),
            documentSearchResponse.getValue().getResults(),
            CoreUtils.isNullOrEmpty(documentSearchResponse.getValue().getNextLink())
                || documentSearchResponse.getValue().getNextPageParameters() == null
                || documentSearchResponse.getValue().getNextPageParameters().getSkip() == null
                ? null : serialize(documentSearchResponse.getValue().getNextPageParameters()),
            deserializeHeaders(documentSearchResponse.getHeaders()));

        this.facets = documentSearchResponse.getValue().getFacets();
        this.count = documentSearchResponse.getValue().getCount();
        this.coverage = documentSearchResponse.getValue().getCoverage();
    }

    private static String serialize(SearchRequest nextPageParameters) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(nextPageParameters);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * Get coverage
     *
     * @return Double
     */
    public Double getCoverage() {
        return coverage;
    }

    /**
     * Get facets
     *
     * @return {@link Map}{@code <}{@link String}{@code ,}{@link List}{@code <}{@link FacetResult}{@code >}{@code >}
     */
    public Map<String, List<FacetResult>> getFacets() {
        return facets;
    }

    /**
     * Get documents count
     *
     * @return long
     */
    public Long getCount() {
        return count;
    }

    private static String deserializeHeaders(HttpHeaders headers) {
        return headers.toMap().entrySet().stream().map((entry) ->
            entry.getKey() + "," + entry.getValue()
        ).collect(Collectors.joining(","));
    }
}
