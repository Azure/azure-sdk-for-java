// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * This is a factory which creates policies in an {@link com.azure.core.http.HttpPipeline} for setting a unique request ID in the
 * x-ms-client-request-id header as is required for all requests to the service. In most cases, it is sufficient to
 * allow the default pipeline to add this factory automatically and assume that it works. The factory and policy must
 * only be used directly when creating a custom pipeline.
 */
public final class RequestIDPolicy implements HttpPipelinePolicy {

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.httpRequest().headers().put(Constants.HeaderConstants.CLIENT_REQUEST_ID_HEADER, UUID.randomUUID().toString());
        return next.process();
    }
}
