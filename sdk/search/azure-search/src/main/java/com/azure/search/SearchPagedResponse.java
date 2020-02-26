// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

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


/**
 * Represents an HTTP response from the search API request that contains a list of items deserialized into a {@link
 * Page}. Each page contains additional information returned by the API request. In the Search API case the additional
 * information is: count - number of total documents returned. Will be returned only if isIncludeTotalResultCount is set
 * to true coverage - coverage value.
 */
public final class SearchPagedResponse extends PagedResponseBase<Void, SearchResult> {

    private final Map<String, List<FacetResult>> facets;
    private final Long count;
    private final Double coverage;

    /**
     * Constructor
     *
     * @param documentSearchResponse an http response with the results
     */
    SearchPagedResponse(SimpleResponse<SearchDocumentsResult> documentSearchResponse) {
        super(documentSearchResponse.getRequest(),
            documentSearchResponse.getStatusCode(),
            documentSearchResponse.getHeaders(),
            documentSearchResponse.getValue().getResults(),
            CoreUtils.isNullOrEmpty(documentSearchResponse.getValue().getNextLink())
                || documentSearchResponse.getValue().getNextPageParameters() == null
                || documentSearchResponse.getValue().getNextPageParameters().getSkip() == null
                ? null : serialize(documentSearchResponse.getValue().getNextPageParameters()),
            null);

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
     * The percentage of the index covered in the search request.
     * <p>
     * If {@code minimumCoverage} wasn't supplied in the request this will be {@code null}.
     *
     * @return The percentage of the index covered in the search request if {@code minimumCoverage} was set in the
     * request, otherwise {@code null}.
     */
    public Double getCoverage() {
        return coverage;
    }

    /**
     * The facet query results based on the search request.
     * <p>
     * If {@code facets} weren't supplied in the request this will be {@code null}.
     *
     * @return The facet query results if {@code facets} were supplied in the request, otherwise {@code null}.
     */
    public Map<String, List<FacetResult>> getFacets() {
        return facets;
    }

    /**
     * The approximate number of documents that matched the search and filter parameters in the request.
     * <p>
     * If {@code count} is set to {@code false} in the request this will be {@code null}.
     *
     * @return The approximate number of documents that match the request if {@code count} is {@code true}, otherwise
     * {@code null}.
     */
    public Long getCount() {
        return count;
    }
}
