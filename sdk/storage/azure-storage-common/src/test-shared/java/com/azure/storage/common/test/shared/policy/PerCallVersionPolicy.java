// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.test.shared.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

/**
 * Policy that overrides the version header to a specific version.
 */
public final class PerCallVersionPolicy implements HttpPipelinePolicy {
    private static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");

    private final String version;

    /**
     * Creates a PerCallVersionPolicy that overrides the version header to a specific version.
     *
     * @param version The version to override the header to.
     */
    public PerCallVersionPolicy(String version) {
        this.version = version;
    }

    /**
     * Gets the version to override the header to.
     *
     * @return The version to override the header to.
     */
    public String getVersion() {
        return version;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.getHttpRequest().setHeader(X_MS_VERSION, version);
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        context.getHttpRequest().setHeader(X_MS_VERSION, version);
        return next.processSync();
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_CALL;
    }
}
