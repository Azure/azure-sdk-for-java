// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpPipelineCallContext;
import com.typespec.core.http.HttpPipelineNextPolicy;
import com.typespec.core.http.HttpPipelineNextSyncPolicy;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import reactor.core.publisher.Mono;

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
     * Creates {@link RedirectPolicy} with default {@link DefaultRedirectStrategy} as {@link RedirectStrategy} and
     * uses the redirect status response code (301, 302, 307, 308) to determine if this request should be redirected.
     */
    public RedirectPolicy() {
        this(new DefaultRedirectStrategy());
    }

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
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // Reset the attemptedRedirectUrls for each individual request.
        return attemptRedirect(context, next, context.getHttpRequest(), 1, new HashSet<>());
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        // Reset the attemptedRedirectUrls for each individual request.
        return attemptRedirectSync(context, next, context.getHttpRequest(), 1, new HashSet<>());
    }

    /**
     * Function to process through the HTTP Response received in the pipeline
     * and redirect sending the request with new redirect url.
     */
    private Mono<HttpResponse> attemptRedirect(final HttpPipelineCallContext context,
                                               final HttpPipelineNextPolicy next,
                                               final HttpRequest originalHttpRequest,
                                               final int redirectAttempt,
                                               Set<String> attemptedRedirectUrls) {
        // make sure the context is not modified during retry, except for the URL
        context.setHttpRequest(originalHttpRequest.copy());

        return next.clone().process()
            .flatMap(httpResponse -> {
                if (redirectStrategy.shouldAttemptRedirect(context, httpResponse, redirectAttempt,
                    attemptedRedirectUrls)) {

                    HttpRequest redirectRequestCopy = createRedirectRequest(httpResponse);
                    return attemptRedirect(context, next, redirectRequestCopy, redirectAttempt + 1, attemptedRedirectUrls);
                } else {
                    return Mono.just(httpResponse);
                }
            });
    }

    /**
     * Function to process through the HTTP Response received in the pipeline
     * and redirect sending the request with new redirect url.
     */
    private HttpResponse attemptRedirectSync(final HttpPipelineCallContext context,
                                             final HttpPipelineNextSyncPolicy next,
                                             final HttpRequest originalHttpRequest,
                                             final int redirectAttempt,
                                             Set<String> attemptedRedirectUrls) {
        // make sure the context is not modified during retry, except for the URL
        context.setHttpRequest(originalHttpRequest.copy());

        HttpResponse httpResponse = next.clone().processSync();

        if (redirectStrategy.shouldAttemptRedirect(context, httpResponse, redirectAttempt,
            attemptedRedirectUrls)) {

            HttpRequest redirectRequestCopy = createRedirectRequest(httpResponse);
            return attemptRedirectSync(context, next, redirectRequestCopy, redirectAttempt + 1,
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
