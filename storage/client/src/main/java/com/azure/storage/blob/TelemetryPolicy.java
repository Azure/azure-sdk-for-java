// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Locale;

/**
 * This is a factory which creates policies in an {@link com.azure.core.http.HttpPipeline} for adding telemetry to a
 * given HTTP request. In  most cases, it is sufficient to configure a {@link TelemetryOptions} object and set those as
 * a field on a {@link PipelineOptions} object to configure a default pipeline. The factory and policy must only be used
 * directly when creating a custom pipeline.
 */
final class TelemetryPolicy implements HttpPipelinePolicy {

    private final String userAgent;

    /**
     * Creates a factory that can create telemetry policy objects which add telemetry information to the outgoing
     * HTTP requests.
     *
     * @param telemetryOptions
     *         {@link TelemetryOptions}
     */
    public TelemetryPolicy(TelemetryOptions telemetryOptions) {
        telemetryOptions = telemetryOptions == null ? new TelemetryOptions() : telemetryOptions;
        String userAgentPrefix = telemetryOptions.userAgentPrefix() == null
                ? Constants.EMPTY_STRING : telemetryOptions.userAgentPrefix();
        this.userAgent = userAgentPrefix + ' '
                + Constants.HeaderConstants.USER_AGENT_PREFIX + '/' + Constants.HeaderConstants.USER_AGENT_VERSION
                + String.format(Locale.ROOT, " (JavaJRE %s; %s %s)",
                        System.getProperty("java.version"),
                        System.getProperty("os.name").replaceAll(" ", ""),
                        System.getProperty("os.version"));
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.httpRequest().headers().put(Constants.HeaderConstants.USER_AGENT, userAgent);
        return next.process();
    }
}
