// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

// TODO: Remove this policy when Java generator adds support for it.
/**
 * Version
 */
public class VersionPolicy implements HttpPipelinePolicy {
    private final String version;

    /**
     *
     * @param version The version to use
     */
    public VersionPolicy(String version) {
        this.version = version;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.getHttpRequest().getHeaders().put("x-ms-version", version);
        return next.process();
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_CALL; // Must be added before credential policies.
    }
}
