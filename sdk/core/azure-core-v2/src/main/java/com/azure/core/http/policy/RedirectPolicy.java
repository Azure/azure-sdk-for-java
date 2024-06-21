// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The {@code RedirectPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This policy
 * handles HTTP redirects by determining if an HTTP request should be redirected based on the received
 * {@link HttpResponse}.
 *
 * <p>This class is useful when you need to handle HTTP redirects in a pipeline. It uses a {@link RedirectStrategy} to
 * decide if a request should be redirected. By default, it uses the {@link DefaultRedirectStrategy}, which redirects
 * the request based on the HTTP status code of the response.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code RedirectPolicy} is constructed and can be added to a pipeline. For a request sent by the
 * pipeline, if the server responds with a redirect status code, the request will be redirected according
 * to the {@link RedirectStrategy} used by the {@code RedirectPolicy}.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.RedirectPolicy.constructor -->
 * <pre>
 * RedirectPolicy redirectPolicy = new RedirectPolicy&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.RedirectPolicy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.HttpPipelinePolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 * @see com.azure.core.http.policy.RedirectStrategy
 * @see com.azure.core.http.policy.DefaultRedirectStrategy
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
    private Mono<HttpResponse> attemptRedirect(final HttpPipelineCallContext context, final HttpPipelineNextPolicy next,
        final HttpRequest originalHttpRequest, final int redirectAttempt, Set<String> attemptedRedirectUrls) {
        // make sure the context is not modified during retry, except for the URL
        context.setHttpRequest(originalHttpRequest.copy());

        return next.clone().process().flatMap(httpResponse -> {
            if (redirectStrategy.shouldAttemptRedirect(context, httpResponse, redirectAttempt, attemptedRedirectUrls)) {
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
        final HttpPipelineNextSyncPolicy next, final HttpRequest originalHttpRequest, final int redirectAttempt,
        Set<String> attemptedRedirectUrls) {
        // make sure the context is not modified during retry, except for the URL
        context.setHttpRequest(originalHttpRequest.copy());

        HttpResponse httpResponse = next.clone().processSync();

        if (redirectStrategy.shouldAttemptRedirect(context, httpResponse, redirectAttempt, attemptedRedirectUrls)) {
            HttpRequest redirectRequestCopy = createRedirectRequest(httpResponse);
            return attemptRedirectSync(context, next, redirectRequestCopy, redirectAttempt + 1, attemptedRedirectUrls);
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
