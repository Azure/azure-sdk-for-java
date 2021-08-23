// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A {@link HttpPipelinePolicy} that redirects a {@link HttpRequest} when an HTTP Redirect is received as response.
 */
public final class RedirectPolicy implements HttpPipelinePolicy {
    private final RedirectStrategy redirectStrategy;

    /**
     * Creates {@link RedirectPolicy} with default {@link DefaultRedirectStrategy} as {@link RedirectStrategy} and
     * use the provided {@code statusCode} to determine if this request should be redirected
     * and MAX_REDIRECT_ATTEMPTS for the try count.
     */
    public RedirectPolicy() {
        this(new DefaultRedirectStrategy());
    }

    /**
     * Creates {@link RedirectPolicy} with default {@link DefaultRedirectStrategy} as {@link RedirectStrategy} and
     * use the provided {@code statusCode} to determine if this request should be redirected.
     *
     * @param redirectStrategy The {@link RedirectStrategy} used for redirection.
     * @throws NullPointerException When {@code statusCode} is null.
     */
    public RedirectPolicy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = Objects.requireNonNull(redirectStrategy, "'redirectStrategy' cannot be null.");
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptRedirect(context, next, context.getHttpRequest(), 1);
    }

    /**
     * Function to process through the HTTP Response received in the pipeline
     * and redirect sending the request with new redirect url.
     */
    private Mono<HttpResponse> attemptRedirect(final HttpPipelineCallContext context,
                                               final HttpPipelineNextPolicy next,
                                               final HttpRequest originalHttpRequest,
                                               final int redirectAttempt) {
        context.setHttpRequest(originalHttpRequest.copy());

        return next.clone().process()
            .flatMap(httpResponse -> {
                if (redirectStrategy.shouldAttemptRedirect(httpResponse, redirectAttempt)) {
                    HttpRequest redirectRequestCopy = redirectStrategy.createRedirect(httpResponse);
                    return httpResponse.getBody()
                        .ignoreElements()
                        .then(attemptRedirect(context, next, redirectRequestCopy, redirectAttempt + 1));
                } else {
                    return Mono.just(httpResponse);
                }
            });
    }

}
