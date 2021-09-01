// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import java.util.Set;

/**
 * The interface for determining the {@link RedirectStrategy redirect strategy} used in {@link RedirectPolicy}.
 */
public interface RedirectStrategy {
    /**
     * Max number of redirect attempts to be made.
     *
     * @return The max number of redirect attempts.
     */
    int getMaxAttempts();

    /**
     * The header name to look up the value for the redirect url in response headers.
     *
     * @return the value of the header, or null if the header doesn't exist in the response.
     */
    String getLocationHeader();

    /**
     * The {@link HttpMethod http methods} that are allowed to be redirected.
     *
     * @return the set of redirect allowed methods.
     */
    Set<HttpMethod> getAllowedMethods();

    /**
     * Determines if the url should be redirected between each try.
     *
     * @param context the {@link HttpPipelineCallContext HTTP pipeline context}.
     * @param httpResponse the {@link HttpRequest} containing the redirect url present in the response headers
     * @param tryCount redirect attempts so far
     * @param attemptedRedirectUrls attempted redirect locations used so far.
     * @return {@code true} if the request should be redirected, {@code false} otherwise
     */
    boolean shouldAttemptRedirect(HttpPipelineCallContext context, HttpResponse httpResponse, int tryCount,
                                  Set<String> attemptedRedirectUrls);

    /**
     * Creates an {@link HttpRequest request} for the redirect attempt.
     *
     * @param httpResponse the {@link HttpResponse} containing the redirect url present in the response headers
     * @return the modified {@link HttpRequest} to redirect the incoming request.
     */
    HttpRequest createRedirectRequest(HttpResponse httpResponse);
}
