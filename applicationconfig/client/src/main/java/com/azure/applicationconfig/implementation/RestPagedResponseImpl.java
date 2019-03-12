// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.implementation;

import com.microsoft.rest.v3.RestPagedResponse;
import com.microsoft.rest.v3.http.HttpRequest;

import java.util.List;
import java.util.Map;

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

    private final Map<String, String> headers;

    private final int statusCode;

    public RestPagedResponseImpl(final List<T> items, final String nextPageLink, final HttpRequest request, final Map<String, String> headers, final int statusCode) {
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
    public List<T> items() {
        return items;
    }

    /**
     * Get the link to retrieve RestPagedResponse containing next page.
     *
     * @return the next page link
     */
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
    public Map<String, String> headers() {
        return headers;
    }

    @Override
    public void close() {
    }
}
