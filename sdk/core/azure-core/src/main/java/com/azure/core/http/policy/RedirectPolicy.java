// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A HttpPipeline policy that retries when a HTTP Redirect is received as response.
 */
public final class RedirectPolicy implements HttpPipelinePolicy {

    // http methods to consider
    // Location header string lookup
    //

    private static final int PERMANENT_REDIRECT_STATUS_CODE = 308;
    // Based on Stamp specific redirects design doc
    private static final int MAX_REDIRECT_RETRIES = 10;
    private final ClientLogger logger = new ClientLogger(RedirectPolicy.class);
    private String redirectedEndpointUrl;

    private final RedirectStrategy retryStrategy;

    /**
     * Creates {@link RedirectPolicy} with default {@link DefaultRedirectStrategy} as {@link RedirectStrategy} and
     * use the provided {@code statusCode} to determine if this request should be retried
     * and MAX_REDIRECT_RETRIES for the retry count.
     *
     * @param statusCode the {@code HttpResponse} status code.
     * @throws NullPointerException When {@code statusCode} is null.
     */
    public RedirectPolicy(int statusCode) {
        this(new DefaultRedirectStrategy(statusCode, MAX_REDIRECT_RETRIES));
    }

    /**
     * Creates {@link RedirectPolicy} with default {@link DefaultRedirectStrategy} as {@link RedirectStrategy} and
     * use the provided {@code statusCode} to determine if this request should be retried.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @throws NullPointerException When {@code statusCode} is null.
     */
    public RedirectPolicy(RedirectStrategy retryStrategy) {
        this.retryStrategy = Objects.requireNonNull(retryStrategy, "'retryStrategy' cannot be null.");
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptRetry(context, next, context.getHttpRequest(), 0);
    }

    /**
     *  Function to process through the HTTP Response received in the pipeline
     *  and retry sending the request with new redirect url.
     */
    private Mono<HttpResponse> attemptRetry(final HttpPipelineCallContext context,
                                            final HttpPipelineNextPolicy next,
                                            final HttpRequest originalHttpRequest,
                                            final int retryCount) {
        // make sure the context is not modified during retry, except for the URL
        context.setHttpRequest(originalHttpRequest.copy());
        if (this.redirectedEndpointUrl != null) {
            context.getHttpRequest().setUrl(this.redirectedEndpointUrl);
        }
        return next.clone().process()
            .flatMap(httpResponse -> {
                if (retryStrategy.shouldAttemptRedirect()) {
                    String responseLocation = httpResponse.getHeaderValue("Location");
                    if (responseLocation != null) {
                        this.redirectedEndpointUrl = responseLocation;
                        return attemptRetry(context, next, originalHttpRequest, retryCount + 1);
                    }
                }
                return Mono.just(httpResponse);
            });
    }
}
