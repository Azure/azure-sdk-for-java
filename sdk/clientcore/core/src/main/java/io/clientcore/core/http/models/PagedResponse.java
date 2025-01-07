// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.util.binarydata.BinaryData;

import java.io.IOException;
import java.util.List;

/**
 * Response of a REST API that returns page.
 *
 * @see Response
 *
 * @param <T> The type of items in the page.
 */
public final class PagedResponse<T> implements Response<List<T>> {

    private final HttpRequest request;
    private final int statusCode;
    private final HttpHeaders headers;
    private final BinaryData body;

    private final List<T> items;
    private final String nextLink;
    //private final String continuationToken;

    /**
     * Creates a new instance of the PagedResponse type.
     *
     * @param request The HttpRequest that was sent to the service whose response resulted in this response.
     * @param statusCode The status code from the response.
     * @param headers The headers from the response.
     * @param body The body from the response.
     * @param items The items returned from the service within the response.
     * @param nextLink The next page reference returned from the service, to enable future requests to pick up
     *      from the same place in the paged iteration.
     */
    public PagedResponse(HttpRequest request, int statusCode, HttpHeaders headers, BinaryData body, List<T> items,
        String nextLink) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.items = items;
        this.nextLink = nextLink;
        //this.continuationToken = null;
    }

    /**
     * Gets the reference to the next page.
     *
     * @return The next page reference, or null if there isn't a next page.
     */
    public String getNextLink() {
        return nextLink;
    }

    /**
     * Gets the continuation token.
     *
     * @return The continuation token, or null if there isn't a next page.
     */
    public String getContinuationToken() {
        return null;
    }

    // TODO
    //public String getPreviousLink() {}
    //public String getFirstLink() {}
    //public String getLastLink() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> getValue() {
        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BinaryData getBody() {
        return body;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (body != null) {
            body.close();
        }
    }
}
