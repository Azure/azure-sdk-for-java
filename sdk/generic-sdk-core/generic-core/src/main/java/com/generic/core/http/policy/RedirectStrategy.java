// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;

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
     * Determines if the url should be redirected between each try.
     *
     * @param httpRequest The {@link HttpRequest HTTP request}.
     * @param httpResponse The {@link HttpResponse} containing the redirect URL present in the headers.
     * @param tryCount Redirect attempts so far.
     * @param attemptedRedirectUrls Attempted redirect locations used so far.
     *
     * @return {@code true} if the request should be redirected, {@code false} otherwise.
     */
    boolean shouldAttemptRedirect(HttpRequest httpRequest, HttpResponse<?> httpResponse, int tryCount,
                                  Set<String> attemptedRedirectUrls);

    /**
     * Creates an {@link HttpRequest request} for the redirect attempt.
     *
     * @param httpResponse The {@link HttpResponse} containing the redirect url present in the response headers
     *
     * @return The modified {@link HttpRequest} to redirect the incoming request.
     */
    HttpRequest createRedirectRequest(HttpResponse<?> httpResponse);
}
