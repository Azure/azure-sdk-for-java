// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A HttpPipeline policy that retries when a HTTP Redirect is received as response.
 */
public final class RedirectPolicy implements HttpPipelinePolicy {

    private static final int PERMANENT_REDIRECT_STATUS_CODE = 308;
    Set<String> attemptedRedirectLocations = new HashSet<>();

    // Based on Stamp specific redirects design doc
    private static final int MAX_REDIRECT_RETRIES = 10;
    private String redirectedEndpointUrl;


    private final RedirectStrategy retryStrategy;

    /**
     * Creates {@link RedirectPolicy} with default {@link DefaultRedirectStrategy} as {@link RedirectStrategy} and
     * use the provided {@code statusCode} to determine if this request should be retried
     * and MAX_REDIRECT_RETRIES for the retry count.
     */
    public RedirectPolicy() {
        this(new DefaultRedirectStrategy(MAX_REDIRECT_RETRIES));
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
     * Function to process through the HTTP Response received in the pipeline
     * and retry sending the request with new redirect url.
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
                if (isRedirectableStatusCode(httpResponse.getStatusCode()) &&
                    isRedirectableMethod(httpResponse.getRequest().getHttpMethod()) &&
                    retryStrategy.shouldAttemptRedirect(httpResponse.getHeaders(), retryCount,
                        attemptedRedirectLocations)) {
                    String responseLocation =
                        tryGetRedirectHeader(httpResponse.getHeaders(), retryStrategy.getLocationHeader());
                    if (responseLocation != null) {
                        attemptedRedirectLocations.add(responseLocation);
                        this.redirectedEndpointUrl = responseLocation;
                        return attemptRetry(context, next, originalHttpRequest, retryCount + 1);
                    }
                }
                return Mono.just(httpResponse);
            });
    }

    private boolean isRedirectableMethod(HttpMethod httpMethod) {
        return retryStrategy.getRedirectableMethods().contains(httpMethod);
    }

    private boolean isRedirectableStatusCode(int statusCode) {
        return statusCode == HttpURLConnection.HTTP_MOVED_TEMP
            || statusCode == HttpURLConnection.HTTP_MOVED_PERM
            || statusCode == PERMANENT_REDIRECT_STATUS_CODE;
    }

    private static String tryGetRedirectHeader(HttpHeaders headers, String headerName) {
        String headerValue = headers.getValue(headerName);

        return CoreUtils.isNullOrEmpty(headerValue) ? null : headerValue;
    }
}
