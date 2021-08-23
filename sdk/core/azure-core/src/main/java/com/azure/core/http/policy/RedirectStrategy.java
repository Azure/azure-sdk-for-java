// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import java.util.Map;

/**
 * The interface for determining the redirect strategy used in {@link RedirectPolicy}.
 */
public interface RedirectStrategy {
    /**
     * Max number of redirect attempts to be made.
     *
     * @return The max number of redirect attempts.
     */
    int getMaxAttempts();

    /**
     * @return the value of the header, or null if the header doesn't exist in the response.
     */
    String getLocationHeader();

    /**
     * @return the set of redirect allowed methods.
     */
    Map<Integer, HttpMethod> getAllowedMethods();

    /**
     * Determines if the url should be redirected between each try.
     *
     * @param httpResponse the {@link HttpRequest} containing the redirect url present in the response headers
     * @param tryCount redirect attempts so far
     * @return {@code true} if the request should be redirected, {@code false}
     * otherwise
     */
    boolean shouldAttemptRedirect(HttpResponse httpResponse, int tryCount);

    /**
     * Creates the {@link HttpRequest request} for the redirect attempt.
     *
     * @param httpResponse the {@link HttpRequest} containing the redirect url present in the response headers
     * @return the modified {@link HttpRequest} to redirect the incoming request.
     */
    HttpRequest createRedirect(HttpResponse httpResponse);
}
