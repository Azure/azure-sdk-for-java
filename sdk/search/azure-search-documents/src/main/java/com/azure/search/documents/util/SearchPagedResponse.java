// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.converters.FacetResultConverter;
import com.azure.search.documents.implementation.converters.SearchResultConverter;
import com.azure.search.documents.implementation.models.SearchContinuationToken;
import com.azure.search.documents.implementation.models.SearchDocumentsResult;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.SearchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an HTTP response from the search API request that contains a list of items deserialized into a {@link
 * Page}. Each page contains additional information returned by the API request. In the Search API case the additional
 * information is: count - number of total documents returned. Will be returned only if isIncludeTotalResultCount is set
 * to true coverage - coverage value.
 */
@Immutable
public final class SearchPagedResponse extends PagedResponseBase<Void, SearchResult> {
    private final List<SearchResult> value;

    private final Map<String, List<FacetResult>> facets;
    private final Long count;
    private final Double coverage;

    /**
     * Constructor
     *
     * @param documentSearchResponse An http response with the results.
     * @param serviceVersion The api version to build into continuation token.
     */
    public SearchPagedResponse(SimpleResponse<SearchDocumentsResult> documentSearchResponse,
        SearchServiceVersion serviceVersion) {
        super(documentSearchResponse.getRequest(),
            documentSearchResponse.getStatusCode(),
            documentSearchResponse.getHeaders(),
            documentSearchResponse.getValue().getResults().stream()
                .map(SearchResultConverter::map).collect(Collectors.toList()),
            createContinuationToken(documentSearchResponse, serviceVersion),
            null);

        SearchDocumentsResult documentsResult = documentSearchResponse.getValue();
        this.value = documentsResult.getResults().stream().map(SearchResultConverter::map)
            .collect(Collectors.toList());
        this.facets = documentsResult.getFacets() == null ? null : new HashMap<>();
        if (this.facets != null) {
            documentsResult.getFacets().forEach((key, values) -> {
                this.facets.put(key, values.stream().map(FacetResultConverter::map).collect(Collectors.toList()));
            });
        }
        this.count = documentsResult.getCount();
        this.coverage = documentsResult.getCoverage();
    }

    private static String createContinuationToken(SimpleResponse<SearchDocumentsResult> documentSearchResponse,
        SearchServiceVersion serviceVersion) {
        SearchDocumentsResult documentsResult = documentSearchResponse.getValue();
        if (documentsResult == null) {
            return null;
        }
        return SearchContinuationToken.serializeToken(serviceVersion.getVersion(), documentsResult.getNextLink(),
            documentsResult.getNextPageParameters());
    }

    /**
     * The percentage of the index covered in the search request.
     * <p>
     * If {@code minimumCoverage} wasn't supplied in the request this will be {@code null}.
     *
     * @return The percentage of the index covered in the search request if {@code minimumCoverage} was set in the
     * request, otherwise {@code null}.
     */
    Double getCoverage() {
        return coverage;
    }

    /**
     * The facet query results based on the search request.
     * <p>
     * If {@code facets} weren't supplied in the request this will be {@code null}.
     *
     * @return The facet query results if {@code facets} were supplied in the request, otherwise {@code null}.
     */
    Map<String, List<FacetResult>> getFacets() {
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
    Long getCount() {
        return count;
    }

    @Override
    public List<SearchResult> getValue() {
        return value;
    }
}
