// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.client;

import io.clientcore.core.client.traits.HttpTrait;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpRedirectOptions;
import io.clientcore.core.http.models.HttpRetryOptions;
import io.clientcore.core.http.pipeline.HttpLoggingPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePosition;
import io.clientcore.core.http.pipeline.HttpRedirectPolicy;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

// TODO (alzimmer): Should this implement ProxyTrait? And how is ProxyTrait meant to be used when there is no generic
//  way to configure HTTP proxies?
/**
 * Base class for HTTP-based service client builders.
 *
 * @param <T> The type of the client builder.
 */
public abstract class HttpClientBuilderBase<T extends HttpClientBuilderBase<T>> implements HttpTrait<T> {
    private final HttpPipelineBuilder httpPipelineBuilder;

    private HttpLogOptions logOptions;
    private HttpRedirectOptions redirectOptions;
    private HttpRetryOptions retryOptions;

    /**
     * Creates a new instance of {@link HttpClientBuilderBase}.
     */
    protected HttpClientBuilderBase() {
        httpPipelineBuilder = new HttpPipelineBuilder();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T httpRetryOptions(HttpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = logOptions;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T httpRedirectOptions(HttpRedirectOptions redirectOptions) {
        this.redirectOptions = redirectOptions;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T modifyHttpPipelineBuilder(Consumer<HttpPipelineBuilder> builderModifier) {
        Objects.requireNonNull(builderModifier, "'builderModifier' cannot be null.");
        builderModifier.accept(httpPipelineBuilder);

        return (T) this;
    }

    protected final HttpPipeline buildHttpPipeline() {
        // TODO (alzimmer): A way to get the HttpClient and HttpPipelinePolicies from the HttpPipelineBuilder would be
        //  better than needing to create an HttpPipeline instance to get them.
        HttpPipeline initialPipeline = httpPipelineBuilder.build();
        HttpClient httpClient = initialPipeline.getHttpClient();

        List<HttpPipelinePolicy> beforeRetry = new ArrayList<>();
        List<HttpPipelinePolicy> afterRetry = new ArrayList<>();

        HttpPipelinePolicy loggingPolicy = null;
        HttpPipelinePolicy redirectPolicy = null;
        HttpPipelinePolicy retryPolicy = null;

        for (HttpPipelinePolicy policy : initialPipeline.getPolicies()) {
            if (policy instanceof HttpLoggingPolicy) {
                loggingPolicy = policy;
            } else if (policy instanceof HttpRedirectPolicy) {
                redirectPolicy = policy;
            } else if (policy instanceof HttpRetryPolicy) {
                retryPolicy = policy;
            } else if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
                afterRetry.add(policy);
            } else {
                beforeRetry.add(policy);
            }
        }

        if (loggingPolicy == null) {
            loggingPolicy = new HttpLoggingPolicy(logOptions);
        }

        // HttpRedirectPolicy is optional.
        if (redirectPolicy == null && redirectOptions != null) {
            redirectPolicy = new HttpRedirectPolicy(redirectOptions);
        }

        if (retryPolicy == null) {
            retryPolicy = (retryOptions == null) ? new HttpRetryPolicy() : new HttpRetryPolicy(retryOptions);
        }

        HttpPipelineBuilder httpPipelineBuilder = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(beforeRetry);

        if (redirectPolicy != null) {
            httpPipelineBuilder.policies(redirectPolicy);
        }

        return httpPipelineBuilder.policies(retryPolicy)
            .policies(afterRetry)
            .policies(loggingPolicy)
            .build();
    }
}
