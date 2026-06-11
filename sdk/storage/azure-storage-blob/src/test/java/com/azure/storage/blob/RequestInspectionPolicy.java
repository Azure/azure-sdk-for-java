// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * Test-only pipeline policy that lets a test peek at every {@link HttpRequest} as it
 * goes on the wire. Registers at {@link HttpPipelinePosition#PER_RETRY} so it sees
 * the {@code Authorization} header that the auth policies set.
 *
 * <p>Used by the session-auth live tests as a wire-level sanity check (e.g. to assert
 * which authentication scheme was applied to a given request).</p>
 */
public final class RequestInspectionPolicy implements HttpPipelinePolicy {
    private final Consumer<HttpRequest> inspector;

    public RequestInspectionPolicy(Consumer<HttpRequest> inspector) {
        this.inspector = inspector;
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_RETRY;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (inspector != null) {
            inspector.accept(context.getHttpRequest());
        }
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (inspector != null) {
            inspector.accept(context.getHttpRequest());
        }
        return next.processSync();
    }
}
