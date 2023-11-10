// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.policy.redirect;

import com.typespec.core.http.models.HttpHeaderName;
import com.typespec.core.http.models.HttpRequest;
import com.typespec.core.http.models.HttpResponse;
import com.typespec.core.http.pipeline.HttpPipelineNextPolicy;
import com.typespec.core.http.pipeline.HttpPipelinePolicy;
import com.typespec.core.http.policy.RedirectStrategy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A {@link HttpPipelinePolicy} that redirects a {@link HttpRequest} when an HTTP Redirect is received as
 * {@link HttpResponse response}.
 */
public final class RedirectPolicy implements HttpPipelinePolicy {
    private final RedirectStrategy redirectStrategy;

    /**
     * Creates {@link RedirectPolicy} with the provided {@code redirectStrategy} as {@link RedirectStrategy}
     * to determine if this request should be redirected.
     *
     * @param redirectStrategy The {@link RedirectStrategy} used for redirection.
     * @throws NullPointerException When {@code redirectStrategy} is {@code null}.
     */
    public RedirectPolicy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = Objects.requireNonNull(redirectStrategy, "'redirectStrategy' cannot be null.");
    }

    @Override
    public HttpResponse process(HttpRequest request, HttpPipelineNextPolicy next) {
        // Reset the attemptedRedirectUrls for each individual request.
        return attemptRedirect(next, request, 1, new HashSet<>());
    }

    /**
     * Function to process through the HTTP Response received in the pipeline
     * and redirect sending the request with new redirect url.
     */
    private HttpResponse attemptRedirect(final HttpPipelineNextPolicy next,
                                         final HttpRequest originalHttpRequest,
                                         final int redirectAttempt,
                                         Set<String> attemptedRedirectUrls) {
        // make sure the context is not modified during retry, except for the URL
        originalHttpRequest.getRequestContext().addData("cop", originalHttpRequest.copy());

        HttpResponse httpResponse = next.clone().process();

        if (redirectStrategy.shouldAttemptRedirect(originalHttpRequest, httpResponse, redirectAttempt,
            attemptedRedirectUrls)) {

            HttpRequest redirectRequestCopy = createRedirectRequest(httpResponse);
            return attemptRedirect(next, redirectRequestCopy, redirectAttempt + 1,
                attemptedRedirectUrls);
        } else {
            return httpResponse;
        }
    }

    private HttpRequest createRedirectRequest(HttpResponse redirectResponse) {
        // Clear the authorization header to avoid the client to be redirected to an untrusted third party server
        // causing it to leak your authorization token to.
        redirectResponse.getRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
        HttpRequest redirectRequestCopy = redirectStrategy.createRedirectRequest(redirectResponse);
        redirectResponse.close();

        return redirectRequestCopy;
    }
}
