// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.models.SearchDocumentsResult;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.SearchRequest;
import com.azure.search.documents.models.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an HTTP response from the search API request that contains a list of items deserialized into a {@link
 * Page}. Each page contains additional information returned by the API request. In the Search API case the additional
 * information is: count - number of total documents returned. Will be returned only if isIncludeTotalResultCount is set
 * to true coverage - coverage value.
 */
@Immutable
public final class SearchPagedResponse extends PagedResponseBase<SearchResult, SearchResult> {
    private final int statusCode;
    private final HttpHeaders headers;
    private final HttpRequest request;
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
            documentSearchResponse.getValue().getResults(),
            setContinuationToken(documentSearchResponse, serviceVersion),
            null);
        this.statusCode = documentSearchResponse.getStatusCode();
        this.headers = documentSearchResponse.getHeaders();
        this.request = documentSearchResponse.getRequest();

        SearchDocumentsResult documentsResult = documentSearchResponse.getValue();
        this.value = documentsResult.getResults();
        this.facets = documentsResult.getFacets();
        this.count = documentsResult.getCount();
        this.coverage = documentsResult.getCoverage();
    }

    private static String setContinuationToken(SimpleResponse<SearchDocumentsResult> documentSearchResponse,
        SearchServiceVersion serviceVersion) {
        SearchDocumentsResult documentsResult = documentSearchResponse.getValue();
        if (documentsResult == null) {
            return null;
        }
        String nextParameters;
        try {
            nextParameters = new JacksonAdapter().serialize(getNextPageParameters(documentsResult),
                SerializerEncoding.JSON);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to serialize the search request.");
        }
        ObjectNode tokenJson = new ObjectMapper().createObjectNode();
        tokenJson.put("apiVersion", serviceVersion.getVersion());
        tokenJson.put("nextLink", documentsResult.getNextLink());
        tokenJson.put("nextPageParameters", nextParameters);

        return Base64.getEncoder().encodeToString(tokenJson.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static SearchRequest getNextPageParameters(SearchDocumentsResult result) {
        if (CoreUtils.isNullOrEmpty(result.getNextLink())
            || result.getNextPageParameters() == null
            || result.getNextPageParameters().getSkip() == null) {
            return null;
        }

        return result.getNextPageParameters();
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
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    @Override
    public List<SearchResult> getValue() {
        return value;
    }

    @Override
    public IterableStream<SearchResult> getElements() {
        return new IterableStream<>(value);
    }

    @Override
    public String getContinuationToken() {
        String decodedToken = new String(Base64.getDecoder().decode(super.getContinuationToken()));
        Map<String, String> fieldMap = new HashMap<>();
        try {
            fieldMap = new JacksonAdapter().deserialize(decodedToken, Map.class, SerializerEncoding.JSON);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fieldMap.get("nextPageParameters");
    }
}
