// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedResponseBase;

import java.util.List;

/**
 * Represents a paged response model that includes additional information about the total count
 * of elements available in the entire collection. This class extends the base class
 * {@link PagedResponseBase} and provides the means to access the total count information
 * @param <T> The type of elements contained in the paged response
 */
public class CountPagedResponse<T> extends PagedResponseBase<Void, T> {
    /**
     * The total number of elements in the entire collection.
     */
    private Long totalElements;

    /**
     * Constructs a CountPagedResponse with specified elements, continuation token, and total count.
     * @param request The HTTP request that resulted in this paged response.
     * @param statusCode The HTTP status code of the response.
     * @param headers The HTTP headers of the response.
     * @param items The elements in the current page.
     * @param continuationToken A token that can be used to retrieve the next page of elements.
     * @param deserializedHeaders The deserialized headers (maybe of type Void).
     * @param totalElements The total count of elements available in the entire collection.
     */
    public CountPagedResponse(HttpRequest request, int statusCode, HttpHeaders headers, List<T> items, String continuationToken, Void deserializedHeaders, Long totalElements) {
        super(request, statusCode, headers, items, continuationToken, deserializedHeaders);
        this.totalElements = totalElements;
    }

    /**
     * Constructs a CountPagedResponse with specified elements, continuation token, and total count.
     * @param request The HTTP request that resulted in this paged response.
     * @param statusCode The HTTP status code of the response.
     * @param headers The HTTP headers of the response.
     * @param items The elements in the current page.
     * @param continuationToken A token that can be used to retrieve the next page of elements.
     * @param deserializedHeaders The deserialized headers (maybe of type Void).
     */
    public CountPagedResponse(HttpRequest request, int statusCode, HttpHeaders headers, List<T> items, String continuationToken, Void deserializedHeaders) {
        super(request, statusCode, headers, items, continuationToken, deserializedHeaders);
    }

    /**
     * Retrieve the total count of elements in the entire collection.
     * @return The total count of elements in the collection
     */
    public Long getTotalElements() {
        return totalElements;
    }
}
