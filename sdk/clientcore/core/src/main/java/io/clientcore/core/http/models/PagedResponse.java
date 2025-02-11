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
    private final String continuationToken;
    private final String nextLink;
    private final String previousLink;
    private final String firstLink;
    private final String lastLink;

    /**
     * Creates a new instance of the PagedResponse type.
     *
     * @param request The HttpRequest that was sent to the service whose response resulted in this response.
     * @param statusCode The status code from the response.
     * @param headers The headers from the response.
     * @param body The body from the response.
     * @param items The items returned from the service within the response.
     */
    public PagedResponse(HttpRequest request, int statusCode, HttpHeaders headers, BinaryData body, List<T> items) {
        this(request, statusCode, headers, body, items, null, null, null, null, null);
    }

    /**
     * Creates a new instance of the PagedResponse type.
     *
     * @param request The HttpRequest that was sent to the service whose response resulted in this response.
     * @param statusCode The status code from the response.
     * @param headers The headers from the response.
     * @param body The body from the response.
     * @param items The items returned from the service within the response.
     * @param continuationToken The continuation token returned from the service, to enable future requests to pick up
     *      from the same place in the paged iteration.
     * @param nextLink The next page link returned from the service.
     * @param previousLink The previous page link returned from the service.
     * @param firstLink The first page link returned from the service.
     * @param lastLink The last page link returned from the service.
     */
    public PagedResponse(HttpRequest request, int statusCode, HttpHeaders headers, BinaryData body, List<T> items,
        String continuationToken, String nextLink, String previousLink, String firstLink, String lastLink) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.items = items;
        this.continuationToken = continuationToken;
        this.nextLink = nextLink;
        this.previousLink = previousLink;
        this.firstLink = firstLink;
        this.lastLink = lastLink;
    }

    /**
     * Gets the continuation token.
     *
     * @return The continuation token, or null if there isn't a next page.
     */
    public String getContinuationToken() {
        return continuationToken;
    }

    /**
     * Gets the link to the next page.
     *
     * @return The next page link, or null if there isn't a next page.
     */
    public String getNextLink() {
        return nextLink;
    }

    /**
     * Gets the link to the previous page.
     *
     * @return The previous page link, or null if there isn't a previous page.
     */
    public String getPreviousLink() {
        return previousLink;
    }

    /**
     * Gets the link to the first page.
     *
     * @return The first page link
     */
    public String getFirstLink() {
        return firstLink;
    }

    /**
     * Gets the link to the last page.
     *
     * @return The last page link
     */
    public String getLastLink() {
        return lastLink;
    }

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
