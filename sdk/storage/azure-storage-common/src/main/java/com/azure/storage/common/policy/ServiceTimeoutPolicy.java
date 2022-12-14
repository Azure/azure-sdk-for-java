package com.azure.storage.common.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.UrlBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Pipeline policy that sets the {@code timeout} URI query parameter to cancel requests on the service side.
 *
 * <a href="https://learn.microsoft.com/rest/api/storageservices/setting-timeouts-for-blob-service-operations">Blobs timeout</a>
 */
public class ServiceTimeoutPolicy implements HttpPipelinePolicy {
    private final boolean applyTimeout;
    private final String timeoutInSeconds;

    /**
     * Creates a service timeout policy.
     * <p>
     * The service permits a timeout maximum of 30 seconds.
     *
     * @param timeout The timeout duration.
     * @throws IllegalArgumentException If the timeout is greater than 30 seconds.
     */
    public ServiceTimeoutPolicy(Duration timeout) {
        // This can be changed to require a valid timeout of 1-30 seconds.
        if (timeout == null) {
            applyTimeout = false;
            timeoutInSeconds = null;
        } else {
            long tempTimeoutInSeconds = timeout.getSeconds();
            if (tempTimeoutInSeconds > 30) {
                throw new IllegalArgumentException("'timeout' was greater than the maximum allowed 30 seconds. It was: "
                    + tempTimeoutInSeconds);
            }

            if (tempTimeoutInSeconds <= 0) {
                applyTimeout = false;
                timeoutInSeconds = null;
            } else {
                applyTimeout = true;
                timeoutInSeconds = String.valueOf(tempTimeoutInSeconds);
            }
        }
    }
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (applyTimeout) {
            // Add 'timeout' URI query parameter to request URL.
            UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
            urlBuilder.addQueryParameter("timeout", timeoutInSeconds);
            context.getHttpRequest().setUrl(urlBuilder.toString());
        }

        return next.process();
    }

    // This API is available in newer versions of the SDK, if there is a compiler warning about the just remove this
    // or comment it out.
    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (applyTimeout) {
            // Add 'timeout' URI query parameter to request URL.
            UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
            urlBuilder.addQueryParameter("timeout", timeoutInSeconds);
            context.getHttpRequest().setUrl(urlBuilder.toString());
        }

        return next.processSync();
    }
}

