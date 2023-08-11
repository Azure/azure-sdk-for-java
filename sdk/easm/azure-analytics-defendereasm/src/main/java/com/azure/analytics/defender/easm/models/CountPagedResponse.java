package com.azure.analytics.defender.easm.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedResponseBase;

import java.util.List;

public class CountPagedResponse<T> extends PagedResponseBase<Void, T> {
    private Long totalElements;

    public CountPagedResponse(HttpRequest request, int statusCode, HttpHeaders headers, List<T> items, String continuationToken, Void deserializedHeaders, Long totalElements) {
        super(request, statusCode, headers, items, continuationToken, deserializedHeaders);
        this.totalElements = totalElements;
    }

    public CountPagedResponse(HttpRequest request, int statusCode, HttpHeaders headers, List<T> items, String continuationToken, Void deserializedHeaders) {
        super(request, statusCode, headers, items, continuationToken, deserializedHeaders);
    }

    public Long getTotalElements() {
        return totalElements;
    }
}
