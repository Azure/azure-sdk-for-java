// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * Lightweight {@link HttpClient} decorator that inserts the supplied {@link HttpPipelinePolicy policies} in front of
 * the provided delegate client. This makes it possible to reuse the Azure pipeline policy system with HTTP clients that
 * originate outside of the Azure SDK (e.g. OpenAI generated clients).
 */
@Deprecated
public final class PolicyDecoratingHttpClient implements HttpClient {

    //    private final HttpClient delegate;
    private final HttpPipeline pipeline;

    /**
     * Creates a new decorating client.
     *
     * @param delegate Underlying HTTP client that performs the actual network I/O.
     * @param policies Policies that should run before the request reaches the delegate.
     */
    @Deprecated
    public PolicyDecoratingHttpClient(HttpClient delegate, List<HttpPipelinePolicy> policies) {
        Objects.requireNonNull(delegate, "delegate cannot be null");

        HttpPipelineBuilder builder = new HttpPipelineBuilder().httpClient(delegate);
        if (policies == null || !policies.isEmpty()) {
            builder.policies(policies.toArray(new HttpPipelinePolicy[0]));
        }
        this.pipeline = builder.build();
    }

    public PolicyDecoratingHttpClient(HttpPipeline httpPipeline) {
        this.pipeline = httpPipeline;
    }

    /**
     * Sends the request using the decorated pipeline without a custom {@link Context}.
     */
    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return pipeline.send(request);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        return pipeline.send(request, context);
    }

    /**
     * Synchronously sends the request by blocking on the reactive pipeline. Intended for compatibility with
     * libraries that lack asynchronous plumbing.
     */
    public HttpResponse sendSync(HttpRequest request, Context context) {
        return send(request, context).block();
    }
}
