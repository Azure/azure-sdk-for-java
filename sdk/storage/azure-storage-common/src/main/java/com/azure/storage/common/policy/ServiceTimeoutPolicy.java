// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.UrlBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Pipeline policy that sets the timeout URI query parameter to cancel requests on the service side if the
 * server timeout interval elapses before the service has finished processing the request.
 *
 * For more information on timeouts for different services, see here:
 * <a href="https://learn.microsoft.com/rest/api/storageservices/setting-timeouts-for-blob-service-operations">Setting timeouts for blob service operations</a>
 * <a href="https://learn.microsoft.com/rest/api/storageservices/setting-timeouts-for-file-service-operations">Setting timeouts for file service operations</a>
 * <a href="https://learn.microsoft.com/rest/api/storageservices/setting-timeouts-for-queue-service-operations">Setting timeouts for queue service operations</a>
 */
public final class ServiceTimeoutPolicy implements HttpPipelinePolicy {
    private final boolean applyTimeout;
    private final String timeoutInSeconds;

    /**
     * Creates a service timeout policy.
     * <p>
     * The maximum timeout interval for Blob service operations is 30 seconds, with exceptions for certain operations.
     * The default value is also 30 seconds, although some read and write operations may use a larger default. Apart
     * from these exceptions, the service automatically reduces any timeouts larger than 30 seconds to the
     * 30-second maximum. For more information, see here:
     * <a href="https://learn.microsoft.com/rest/api/storageservices/setting-timeouts-for-blob-service-operations">Setting timeouts for blob service operations</a>
     * For more information on setting timeouts for file shares, see here:
     * <a href="https://learn.microsoft.com/rest/api/storageservices/setting-timeouts-for-file-service-operations">Setting timeouts for file service operations</a>
     * For more information on setting timeouts on queues, see here:
     * <a href="https://learn.microsoft.com/rest/api/storageservices/setting-timeouts-for-queue-service-operations">Setting timeouts for queue service operations</a>
     *
     * @param timeout The timeout duration.
     */
    public ServiceTimeoutPolicy(Duration timeout) {
        if (timeout == null) {
            applyTimeout = false;
            timeoutInSeconds = null;
        } else {
            long tempTimeoutInSeconds = timeout.getSeconds();
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
            String httpRequest = setTimeoutParameter(context, timeoutInSeconds);
            context.getHttpRequest().setUrl(httpRequest);
        }

        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (applyTimeout) {
            String httpRequest = setTimeoutParameter(context, timeoutInSeconds);
            context.getHttpRequest().setUrl(httpRequest);
        }

        return next.processSync();
    }

    static String setTimeoutParameter(HttpPipelineCallContext context, String timeoutInSeconds) {
        // Add 'timeout' URI query parameter to request URL.
        UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
        urlBuilder.setQueryParameter("timeout", timeoutInSeconds);
        return urlBuilder.toString();
    }

    /**
     * Gets the position to place the policy.
     *
     * @return The position to place the policy.
     */
    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_CALL;
    }
}
