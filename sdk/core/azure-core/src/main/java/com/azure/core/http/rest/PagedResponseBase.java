// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.IterableStream;

import java.util.List;

/**
 * Represents an HTTP response that contains a list of items deserialized into a {@link Page}.
 *
 * @param <H> The HTTP response headers
 * @param <T> The type of items contained in the {@link Page}
 * @see com.azure.core.http.rest.PagedResponse
 */
public class PagedResponseBase<H, T> implements PagedResponse<T> {
    private final HttpRequest request;
    private final int statusCode;
    private final H deserializedHeaders;
    private final HttpHeaders headers;
    private final List<T> items;
    private final String continuationToken;

    /**
     * Creates a new instance of the PagedResponseBase type.
     *
     * @param request The HttpRequest that was sent to the service whose response resulted in this response.
     * @param statusCode The status code from the response.
     * @param headers The headers from the response.
     * @param page The page of content returned from the service within the response.
     * @param deserializedHeaders The headers, deserialized into an instance of type H.
     */
    @SuppressWarnings("deprecation")
    public PagedResponseBase(HttpRequest request, int statusCode, HttpHeaders headers, Page<T> page,
                             H deserializedHeaders) {
        this(request, statusCode, headers, page.getItems(), page.getContinuationToken(), deserializedHeaders);
    }

    /**
     * Creates a new instance of the PagedResponseBase type.
     *
     * @param request The HttpRequest that was sent to the service whose response resulted in this response.
     * @param statusCode The status code from the response.
     * @param headers The headers from the response.
     * @param items The items returned from the service within the response.
     * @param continuationToken The continuation token returned from the service, to enable future requests to pick up
     *      from the same place in the paged iteration.
     * @param deserializedHeaders The headers, deserialized into an instance of type H.
     */
    public PagedResponseBase(HttpRequest request, int statusCode, HttpHeaders headers, List<T> items,
                             String continuationToken, H deserializedHeaders) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.items = items;
        this.continuationToken = continuationToken;
        this.deserializedHeaders = deserializedHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IterableStream<T> getElements() {
        return IterableStream.of(items);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContinuationToken() {
        return continuationToken;
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
     * @return the request which resulted in this PagedRequestResponse.
     */
    @Override
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * Get the headers from the HTTP response, transformed into the header type H.
     *
     * @return an instance of header type H, containing the HTTP response headers.
     */
    public H getDeserializedHeaders() {
        return deserializedHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
    }
}
