// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link HttpPipelinePolicy} that manages adding Azure SDK telemetry to an HTTP request.
 */
public class AzureTelemetryPolicy implements HttpPipelinePolicy {
    /**
     * The key used in a {@link Context} to store Azure SDK telemetry information.
     */
    public static final String CONTEXT_TELEMETRY_KEY = "azsdk-telemetry";

    @Override
    @SuppressWarnings("unchecked")
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        Optional<Object> potentialTelemetryData = context.getData(CONTEXT_TELEMETRY_KEY);

        if (potentialTelemetryData.isPresent()) {
            Map<String, Object> telemetryData = (Map<String, Object>) potentialTelemetryData.get();

            if (!CoreUtils.isNullOrEmpty(telemetryData)) {
                context.getHttpRequest().setHeader("x-ms-azsdk-telemetry", telemetryData.entrySet().stream()
                    .map(kvp -> kvp.getKey() + ":" + kvp.getValue().toString())
                    .collect(Collectors.joining(",")));
            }
        }

        return next.process();
    }
}
