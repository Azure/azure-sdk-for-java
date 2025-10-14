// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.paging;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

import java.util.List;

/**
 * Response of a REST API that returns page.
 *
 * @param <T> The type of items in the page.
 * @see Response
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class PagedResponse<T> extends Response<List<T>> {
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
     * @param items The items returned from the service within the response.
     */
    public PagedResponse(HttpRequest request, int statusCode, HttpHeaders headers, List<T> items) {
        this(request, statusCode, headers, items, null, null, null, null, null);
    }

    /**
     * Creates a new instance of the PagedResponse type.
     *
     * @param request The HttpRequest that was sent to the service whose response resulted in this response.
     * @param statusCode The status code from the response.
     * @param headers The headers from the response.
     * @param items The items returned from the service within the response.
     * @param continuationToken The continuation token returned from the service, to enable future requests to pick up
     * from the same place in the paged iteration.
     * @param nextLink The next page link returned from the service.
     * @param previousLink The previous page link returned from the service.
     * @param firstLink The first page link returned from the service.
     * @param lastLink The last page link returned from the service.
     */
    public PagedResponse(HttpRequest request, int statusCode, HttpHeaders headers, List<T> items,
        String continuationToken, String nextLink, String previousLink, String firstLink, String lastLink) {
        super(request, statusCode, headers, items);
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
}
