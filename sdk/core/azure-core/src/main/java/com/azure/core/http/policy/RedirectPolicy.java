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
 * A HttpPipeline policy that redirects when an HTTP Redirect is received as response.
 */
public final class RedirectPolicy implements HttpPipelinePolicy {

    private static final int PERMANENT_REDIRECT_STATUS_CODE = 308;

    // Based on Stamp specific redirects design doc
    private static final int MAX_REDIRECT_ATTEMPTS = 10;
    private final RedirectStrategy redirectStrategy;

    private Set<String> attemptedRedirectUrls = new HashSet<>();
    private String redirectedEndpointUrl;

    /**
     * Creates {@link RedirectPolicy} with default {@link MaxAttemptRedirectStrategy} as {@link RedirectStrategy} and
     * use the provided {@code statusCode} to determine if this request should be redirected
     * and MAX_REDIRECT_ATTEMPTS for the try count.
     */
    public RedirectPolicy() {
        this(new MaxAttemptRedirectStrategy(MAX_REDIRECT_ATTEMPTS));
    }

    /**
     * Creates {@link RedirectPolicy} with default {@link MaxAttemptRedirectStrategy} as {@link RedirectStrategy} and
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
        // make sure the context is not modified during redirect, except for the URL
        context.setHttpRequest(originalHttpRequest.copy());
        if (this.redirectedEndpointUrl != null) {
            context.getHttpRequest().setUrl(this.redirectedEndpointUrl);
        }
        return next.clone().process()
            .flatMap(httpResponse -> {
                String responseLocation =
                    tryGetRedirectHeader(httpResponse.getHeaders(), redirectStrategy.getLocationHeader());
                if (isValidRedirectStatusCode(httpResponse.getStatusCode()) &&
                    isAllowedRedirectMethod(httpResponse.getRequest().getHttpMethod()) &&
                    responseLocation != null &&
                    redirectStrategy.shouldAttemptRedirect(responseLocation, redirectAttempt,
                        redirectStrategy.getMaxAttempts(), attemptedRedirectUrls)) {
                    attemptedRedirectUrls.add(responseLocation);
                    this.redirectedEndpointUrl = responseLocation;
                    return attemptRedirect(context, next, originalHttpRequest, redirectAttempt + 1);
                }
                return Mono.just(httpResponse);
            });
    }

    private boolean isAllowedRedirectMethod(HttpMethod httpMethod) {
        return redirectStrategy.getAllowedMethods().contains(httpMethod);
    }

    private boolean isValidRedirectStatusCode(int statusCode) {
        return statusCode == HttpURLConnection.HTTP_MOVED_TEMP
            || statusCode == HttpURLConnection.HTTP_MOVED_PERM
            || statusCode == PERMANENT_REDIRECT_STATUS_CODE;
    }

    private static String tryGetRedirectHeader(HttpHeaders headers, String headerName) {
        String headerValue = headers.getValue(headerName);
        return CoreUtils.isNullOrEmpty(headerValue) ? null : headerValue;
    }
}
