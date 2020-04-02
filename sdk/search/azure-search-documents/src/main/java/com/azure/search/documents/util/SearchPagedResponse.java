// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.models.SearchDocumentsResult;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.azure.search.documents.implementation.util.ContinuationTokenConstants.API_VERSION;
import static com.azure.search.documents.implementation.util.ContinuationTokenConstants.NEXT_LINK;
import static com.azure.search.documents.implementation.util.ContinuationTokenConstants.NEXT_PAGE_PARAMETERS;

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
    private final String nextParameters;

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
            documentSearchResponse.getValue().getResults(),
            createContinuationToken(documentSearchResponse, serviceVersion),
            null);

        SearchDocumentsResult documentsResult = documentSearchResponse.getValue();
        this.value = documentsResult.getResults();
        this.facets = documentsResult.getFacets();
        this.count = documentsResult.getCount();
        this.coverage = documentsResult.getCoverage();
        this.nextParameters = getNextPageParameters(documentsResult);
    }

    private static String createContinuationToken(SimpleResponse<SearchDocumentsResult> documentSearchResponse,
        SearchServiceVersion serviceVersion) {
        SearchDocumentsResult documentsResult = documentSearchResponse.getValue();
        if (documentsResult == null) {
            return null;
        }

        ObjectNode tokenJson = new ObjectMapper().createObjectNode();
        tokenJson.put(API_VERSION, serviceVersion.getVersion());
        tokenJson.put(NEXT_LINK, documentsResult.getNextLink());
        tokenJson.put(NEXT_PAGE_PARAMETERS, getNextPageParameters(documentsResult));

        return Base64.getEncoder().encodeToString(tokenJson.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String getNextPageParameters(SearchDocumentsResult result) {
        if (CoreUtils.isNullOrEmpty(result.getNextLink())
            || result.getNextPageParameters() == null
            || result.getNextPageParameters().getSkip() == null) {
            return null;
        }
        try {
            return new JacksonAdapter().serialize(result.getNextPageParameters(), SerializerEncoding.JSON);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to serialize the search request.");
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

    @Override
    public List<SearchResult> getValue() {
        return value;
    }
}
