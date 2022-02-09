// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

/**
 * {@link HttpPipelinePolicy} to transform PATCH requests into POST requests with the "X-HTTP-Method":"MERGE" header.
 */
public final class CosmosPatchTransformPolicy implements HttpPipelinePolicy {
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return Mono.defer(() -> {
            HttpRequest request = context.getHttpRequest();
            HttpMethod method = request.getHttpMethod();

            if (method == HttpMethod.PATCH) {
                transformPatchToCosmosPost(request);
            }

            return next.process();
        });
    }

    /**
     * Transform a PATCH request into POST request with the "X-HTTP-Method":"MERGE" header set.
     *
     * @param request The pipeline's {@link HttpRequest}.
     */
    private void transformPatchToCosmosPost(HttpRequest request) {
        request.setHttpMethod(HttpMethod.POST);
        request.setHeader("X-HTTP-Method", "MERGE");
    }
}
