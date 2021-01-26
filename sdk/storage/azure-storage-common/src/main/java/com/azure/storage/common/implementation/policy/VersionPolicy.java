package com.azure.storage.common.implementation.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

// TODO: Remove this policy when Java generator adds support for it.
public class VersionPolicy implements HttpPipelinePolicy {
    private final String version;

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
