// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import com.typespec.core.http.models.HttpRequest;
import com.typespec.core.http.models.HttpResponse;
import com.typespec.core.implementation.http.policy.redirect.RedirectPolicy;
import com.typespec.core.models.Context;

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
     * @param httpRequest           the {@link Context} associated with the http request
     * @param httpResponse          the {@link HttpRequest} containing the redirect url present in the response headers
     * @param tryCount              redirect attempts so far
     * @param attemptedRedirectUrls attempted redirect locations used so far.
     * @return {@code true} if the request should be redirected, {@code false} otherwise
     */
    boolean shouldAttemptRedirect(HttpRequest httpRequest, HttpResponse httpResponse, int tryCount,
                                  Set<String> attemptedRedirectUrls);

    /**
     * Creates an {@link HttpRequest request} for the redirect attempt.
     *
     * @param httpResponse the {@link HttpResponse} containing the redirect url present in the response headers
     * @return the modified {@link HttpRequest} to redirect the incoming request.
     */
    HttpRequest createRedirectRequest(HttpResponse httpResponse);
}
