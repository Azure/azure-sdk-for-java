// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerThrottlingInfo;
import reactor.core.publisher.Mono;

import java.util.function.BiConsumer;

/**
 * A Http Pipeline Policy for automatic send throttling rate limit info to a call back function
 */
public class ResourceManagerThrottlingPolicy implements HttpPipelinePolicy {
    private final BiConsumer<? super HttpResponse, ? super ResourceManagerThrottlingInfo> callback;

    /**
     * Creates the resource manager throttling policy
     * @param callback consume the ResourceManagerThrottlingInfo for every request, it is not a thread-safe method
     */
    public ResourceManagerThrottlingPolicy(
        BiConsumer<? super HttpResponse, ? super ResourceManagerThrottlingInfo> callback) {
        this.callback = callback;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.clone().process()
            .flatMap(response -> {
                HttpResponse bufferedResponse = response.buffer();
                callback.accept(bufferedResponse, ResourceManagerThrottlingInfo.fromHeaders(response.getHeaders()));
                return Mono.just(bufferedResponse);
            });
    }
}
