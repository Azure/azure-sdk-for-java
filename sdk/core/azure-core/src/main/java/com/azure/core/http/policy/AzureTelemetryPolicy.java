// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

/**
 * {@link HttpPipelinePolicy} that manages adding Azure SDK telemetry to an HTTP request.
 */
public class AzureTelemetryPolicy implements HttpPipelinePolicy {
    /**
     * The key used in a {@link Context} to store Azure SDK telemetry information.
     */
    public static final String CONTEXT_TELEMETRY_KEY = "azsdk-telemetry";

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.getData(CONTEXT_TELEMETRY_KEY)
            .ifPresent(telemetry -> context.getHttpRequest()
                .setHeader("x-ms-azsdk-telemetry", String.valueOf(telemetry)));

        return next.process();
    }
}
