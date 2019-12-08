// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.search.models.DocumentSearchResult;
import com.azure.search.models.FacetResult;
import com.azure.search.models.SearchRequest;
import com.azure.search.models.SearchResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * TODO: Add class description
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
    public SearchPagedResponse(SimpleResponse<DocumentSearchResult> documentSearchResponse) {
        super(documentSearchResponse.getRequest(),
            documentSearchResponse.getStatusCode(),
            documentSearchResponse.getHeaders(),
            documentSearchResponse.getValue().getResults(),
            StringUtils.isBlank(documentSearchResponse.getValue().getNextLink())
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
