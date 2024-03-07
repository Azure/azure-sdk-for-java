// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.pipeline.HttpPipelineNextPolicy;
import com.generic.core.http.pipeline.HttpPipelinePolicy;
import com.generic.core.models.HeaderName;
import com.generic.core.util.ClientLogger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A {@link HttpPipelinePolicy} that redirects a {@link HttpRequest} when an HTTP Redirect is received as a
 * {@link HttpResponse response}.
 */
public final class RedirectPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(RedirectPolicy.class);

    private final RedirectStrategy redirectStrategy;

    /**
     * Creates {@link RedirectPolicy} with the provided {@code redirectStrategy} as {@link RedirectStrategy} to
     * determine if this request should be redirected.
     *
     * @param redirectStrategy The {@link RedirectStrategy} used for redirection.
     *
     * @throws NullPointerException When {@code redirectStrategy} is {@code null}.
     */
    public RedirectPolicy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = Objects.requireNonNull(redirectStrategy, "'redirectStrategy' cannot be null.");
    }

    @Override
    public HttpResponse<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        // Reset the attemptedRedirectUrls for each individual request.
        return attemptRedirect(httpRequest, next, 1, new HashSet<>());
    }

    /**
     * Function to process through the HTTP Response received in the pipeline and redirect sending the request with a
     * new redirect URL.
     */
    private HttpResponse<?> attemptRedirect(final HttpRequest httpRequest, final HttpPipelineNextPolicy next,
                                        final int redirectAttempt, Set<String> attemptedRedirectUrls) {
        // Make sure the context is not modified during retry, except for the URL
        HttpResponse<?> httpResponse = next.clone().process();

        if (redirectStrategy.shouldAttemptRedirect(httpRequest, httpResponse, redirectAttempt, attemptedRedirectUrls)) {
            HttpRequest redirectRequestCopy = createRedirectRequest(httpResponse);

            return attemptRedirect(redirectRequestCopy, next, redirectAttempt + 1, attemptedRedirectUrls);
        } else {
            return httpResponse;
        }
    }

    private HttpRequest createRedirectRequest(HttpResponse<?> redirectResponse) {
        // Clear the authorization header to avoid the client to be redirected to an untrusted third party server
        // causing it to leak your authorization token to.
        redirectResponse.getRequest().getHeaders().remove(HeaderName.AUTHORIZATION);

        HttpRequest redirectRequestCopy = redirectStrategy.createRedirectRequest(redirectResponse);

        try {
            redirectResponse.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }

        return redirectRequestCopy;
    }
}
