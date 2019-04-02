// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.implementation;

import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.rest.RestPagedResponse;

import java.util.List;

/**
 * Response of a REST API that returns page.
 *
 * @param <T> the type items in the page
 */
public class RestPagedResponseImpl<T> implements RestPagedResponse<T> {
    /**
     * The link to the next page.
     */
    private final String nextPageLink;

    /**
     * The list of items.
     */
    private final List<T> items;

    private final HttpRequest request;

    private final HttpHeaders headers;

    private final int statusCode;

    public RestPagedResponseImpl(final List<T> items, final String nextPageLink, final HttpRequest request,
                                 final HttpHeaders headers, final int statusCode) {
        this.nextPageLink = nextPageLink;
        this.items = items;
        this.request = request;
        this.headers = headers;
        this.statusCode = statusCode;
    }

    /**
     * Gets the items in the page.
     *
     * @return the items
     */
    @Override
    public List<T> items() {
        return items;
    }

    /**
     * Get the link to retrieve RestPagedResponse containing next page.
     *
     * @return the next page link
     */
    @Override
    public String nextLink() {
        return nextPageLink;
    }

    /**
     * Get the request resulted in this RestPagedResponse.
     *
     * @return the request
     */
    public HttpRequest request() {
        return request;
    }

    /**
     * Get the response status code.
     *
     * @return the status code of the HTTP response
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * Get the response header as a map.
     *
     * @return a Map containing the raw HTTP response headers.
     */
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public void close() {
    }
}
