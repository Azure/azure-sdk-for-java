// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.Response;

import java.util.Collections;
import java.util.Set;

/**
 * Information about the request that failed, used to determine whether a redirect should be attempted.
 */
public final class HttpRequestRedirectCondition {
    private final Response<?> response;
    private final Set<String> redirectedUris;
    private final int tryCount;

    /**
     * Creates a new should redirect request object
     *
     * @param response The HTTP response of the request that failed.
     * @param tryCount The number of tries that have been attempted.
     * @param redirectedUris The set of URIs that have been attempted redirect.
     */
    HttpRequestRedirectCondition(Response<?> response, int tryCount, Set<String> redirectedUris) {
        this.response = response;
        this.tryCount = tryCount;
        this.redirectedUris = redirectedUris == null ? Collections.emptySet() : redirectedUris;
    }

    /**
     * Gets the HTTP response of the request that failed.
     * <p>
     * This may be null if the request failed with a throwable and no response was received.
     *
     * @return The HTTP response of the request that failed.
     */
    public Response<?> getResponse() {
        return response;
    }

    /**
     * Gets the number of tries that have been attempted.
     *
     * @return The number of tries that have been attempted.
     */
    public int getTryCount() {
        return tryCount;
    }

    /**
     * Gets the unmodifiable set of uris that have been attempted redirect.
     *
     * @return The unmodifiable list of exceptions that have been attempted redirect.
     */
    public Set<String> getRedirectedUris() {
        return redirectedUris;
    }
}
