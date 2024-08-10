// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.StatusCode;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.Constant.EXCEPTION_TYPE;
import static com.azure.monitor.opentelemetry.exporter.implementation.utils.Constant.STATUS_CODE;

public class NetworkStatsbeatHttpPipelinePolicy implements HttpPipelinePolicy {

    private static final String INSTRUMENTATION_KEY_DATA = "instrumentationKey";

    private final NetworkStatsbeat networkStatsbeat;

    public NetworkStatsbeatHttpPipelinePolicy(NetworkStatsbeat networkStatsbeat) {
        this.networkStatsbeat = networkStatsbeat;
    }

    @SuppressWarnings("ArgumentSelectionDefectChecker")
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // using AtomicLong for both mutable holder and volatile (but atomicity is not needed here)
        AtomicLong startTime = new AtomicLong();
        String host = context.getHttpRequest().getUrl().getHost();
        String instrumentationKey = context.getData(INSTRUMENTATION_KEY_DATA).orElse("unknown").toString();
        return next.process()
            .doOnSubscribe(subscription -> startTime.set(System.currentTimeMillis()))
            .doOnSuccess(response -> {
                int statusCode = response.getStatusCode();
                if (statusCode == 200) {
                    networkStatsbeat.incrementRequestSuccessCount(System.currentTimeMillis() - startTime.get(),
                        instrumentationKey, host);
                } else if (StatusCode.isRedirect(statusCode)) {
                    // these are not tracked as success or failure since they are just redirects
                } else if (statusCode == 402 || statusCode == 439) {
                    networkStatsbeat.incrementThrottlingCount(instrumentationKey, host, STATUS_CODE, statusCode);
                } else if (StatusCode.isRetryable(statusCode)) {
                    networkStatsbeat.incrementRetryCount(instrumentationKey, host, STATUS_CODE, statusCode);
                } else {
                    // 400 and 404 will be tracked as failure count
                    networkStatsbeat.incrementRequestFailureCount(instrumentationKey, host, STATUS_CODE, statusCode);
                }
            })
            .doOnError(throwable -> {
                networkStatsbeat.incrementExceptionCount(instrumentationKey, host, EXCEPTION_TYPE,
                    throwable.getClass().getName());
            });
    }
}
