// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

/**
 * This is a factory which creates policies in an {@link com.azure.core.http.HttpPipeline} for setting the request property on the response
 * object. This is necessary because of a bug in autorest which fails to set this property. In most cases, it is
 * sufficient to allow the default pipeline to add this factory automatically and assume that it works. The factory and
 * policy must only be used directly when creating a custom pipeline.
 */
final class SetResponseFieldPolicy implements HttpPipelinePolicy {

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process()
            .map(response ->
                response.withRequest(context.httpRequest()));
    }
}
