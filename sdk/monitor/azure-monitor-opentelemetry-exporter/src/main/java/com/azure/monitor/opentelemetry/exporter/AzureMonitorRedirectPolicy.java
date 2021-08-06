// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;

/**
 * A HttpPipeline policy that retries when a HTTP Redirect is received as response.
 */
public final class AzureMonitorRedirectPolicy implements HttpPipelinePolicy {

    private static final int PERMANENT_REDIRECT_STATUS_CODE = 308;
    // Based on Stamp specific redirects design doc
    private static final int MAX_REDIRECT_RETRIES = 10;
    private final ClientLogger logger = new ClientLogger(AzureMonitorRedirectPolicy.class);
    private String redirectedEndpointUrl;

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
                if (shouldRetryWithRedirect(httpResponse.getStatusCode(), retryCount)) {
                    String responseLocation = httpResponse.getHeaderValue("Location");
                    if (responseLocation != null) {
                        this.redirectedEndpointUrl = responseLocation;
                        return attemptRetry(context, next, originalHttpRequest, retryCount + 1);
                    }
                }
                return Mono.just(httpResponse);
            });
    }

    /**
     * Determines if it's a valid retry scenario based on statusCode and tryCount.
     *
     * @param statusCode HTTP response status code
     * @param tryCount Redirect retries so far
     * @return True if statusCode corresponds to HTTP redirect response codes and redirect
     * retries is less than {@code MAX_REDIRECT_RETRIES}.
     */
    private boolean shouldRetryWithRedirect(int statusCode, int tryCount) {
        if (tryCount >= MAX_REDIRECT_RETRIES) {
            logger.verbose("Max redirect retries limit reached: {}.", MAX_REDIRECT_RETRIES);
            return false;
        }
        return statusCode == HttpURLConnection.HTTP_MOVED_TEMP
                || statusCode == HttpURLConnection.HTTP_MOVED_PERM
                || statusCode == PERMANENT_REDIRECT_STATUS_CODE;
    }

}
