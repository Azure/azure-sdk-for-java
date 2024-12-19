// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

/**
 * Policy that processes telemetry information when sending a request and receiving a response.
 */
public final class HttpTelemetryPolicy implements HttpPipelinePolicy {
    static final String NAME = "telemetry";

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        return next.process();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
