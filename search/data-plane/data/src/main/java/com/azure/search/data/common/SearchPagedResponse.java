package com.azure.search.data.common;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.search.data.generated.models.DocumentSearchResult;
import com.azure.search.data.generated.models.FacetResult;
import com.azure.search.data.generated.models.SearchResult;

import java.util.List;
import java.util.Map;

public class SearchPagedResponse<T> extends PagedResponseBase {

    public Map<String, List<FacetResult>> facets() {
        return facets;
    }

    private final Map<String, List<FacetResult>> facets;

    public Long count() {
        return count;
    }

    private final Long count;


    public SearchPagedResponse(HttpRequest request, int statusCode, HttpHeaders headers, List<T> items, String nextLink, String deserializedHeaders, Map<String, List<FacetResult>> facets, Long count) {

        super(request, statusCode, headers, items, nextLink, deserializedHeaders);

        this.facets = facets;
        this.count = count;
    }

    public static SearchPagedResponse<SearchResult> fromDocumentSearchResultResponse(SimpleResponse<DocumentSearchResult> documentSearchResponse) {

        DocumentSearchResult searchResult = documentSearchResponse.value();

        return new SearchPagedResponse(documentSearchResponse.request(),
            documentSearchResponse.statusCode(),
            documentSearchResponse.headers(),
            searchResult.results(),
            searchResult.nextLink(),
            "",
            searchResult.facets(),
            searchResult.count());

    }
}
