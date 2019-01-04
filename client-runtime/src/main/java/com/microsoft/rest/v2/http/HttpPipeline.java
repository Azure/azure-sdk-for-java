/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.HttpClientRequestPolicyAdapter;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * A collection of RequestPolicies that will be applied to a HTTP request before it is sent and will
 * be applied to a HTTP response when it is received.
 */
public final class HttpPipeline {
    /**
     * The list of RequestPolicy factories that will be applied to HTTP requests and responses.
     * The factories appear in this list in the order that they will be applied to outgoing
     * requests.
     */
    private final RequestPolicyFactory[] requestPolicyFactories;

    /**
     * The HttpClient that will be used to send requests unless the sendRequestAsync() method is
     * called with a different HttpClient.
     */
    private final HttpClientRequestPolicyAdapter httpClientRequestPolicyAdapter;

    /**
     * The optional properties that will be passed to each RequestPolicy as it is being created.
     */
    private final RequestPolicyOptions requestPolicyOptions;

    /**
     * Create a new HttpPipeline with the provided RequestPolicy factories.
     * @param requestPolicyFactories The RequestPolicy factories to apply to HTTP requests and
     *                               responses that pass through this HttpPipeline.
     * @param options The optional properties that will be set on this HTTP pipelines.
     */
    HttpPipeline(RequestPolicyFactory[] requestPolicyFactories, HttpPipelineOptions options) {
        this.requestPolicyFactories = requestPolicyFactories;

        final HttpClient httpClient = (options != null && options.httpClient() != null ? options.httpClient() : HttpClient.createDefault());
        this.httpClientRequestPolicyAdapter = new HttpClientRequestPolicyAdapter(httpClient);

        final HttpPipelineLogger logger = (options != null ? options.logger() : null);
        this.requestPolicyOptions = new RequestPolicyOptions(logger);
    }

    /**
     * Send the provided HTTP request using this HttpPipeline's HttpClient after it has passed through
     * each of the RequestPolicies that have been configured on this HttpPipeline.
     * @param httpRequest The HttpRequest to send.
     * @return The HttpResponse that was received.
     */
    public Mono<HttpResponse> sendRequestAsync(HttpRequest httpRequest) {
        RequestPolicy requestPolicy = httpClientRequestPolicyAdapter;
        for (final RequestPolicyFactory requestPolicyFactory : requestPolicyFactories) {
            requestPolicy = requestPolicyFactory.create(requestPolicy, requestPolicyOptions);
        }
        return requestPolicy.sendAsync(httpRequest);
    }

    /**
     * Build a new HttpPipeline that will use the provided RequestPolicy factories.
     * @param requestPolicyFactories The RequestPolicy factories to use.
     * @return The built HttpPipeline.
     */
    public static HttpPipeline build(Iterable<RequestPolicyFactory> requestPolicyFactories) {
        return build(null, requestPolicyFactories);
    }

    /**
     * Build a new HttpPipeline that will use the provided RequestPolicy factories.
     * @param requestPolicyFactories The RequestPolicy factories to use.
     * @return The built HttpPipeline.
     */
    public static HttpPipeline build(RequestPolicyFactory... requestPolicyFactories) {
        return build((HttpPipelineOptions) null, requestPolicyFactories);
    }

    /**
     * Build a new HttpPipeline that will use the provided HttpClient and RequestPolicy factories.
     * @param httpClient The HttpClient to use.
     * @param requestPolicyFactories The RequestPolicy factories to use.
     * @return The built HttpPipeline.
     */
    public static HttpPipeline build(HttpClient httpClient, RequestPolicyFactory... requestPolicyFactories) {
        return build(new HttpPipelineOptions().withHttpClient(httpClient), requestPolicyFactories);
    }

    /**
     * Build a new HttpPipeline that will use the provided HttpClient and RequestPolicy factories.
     * @param pipelineOptions The optional properties that can be set on the created HttpPipeline.
     * @param requestPolicyFactories The RequestPolicy factories to use.
     * @return The built HttpPipeline.
     */
    public static HttpPipeline build(HttpPipelineOptions pipelineOptions, RequestPolicyFactory... requestPolicyFactories) {
        return build(pipelineOptions, Arrays.asList(requestPolicyFactories));
    }

    /**
     * Build a new HttpPipeline that will use the provided HttpClient and RequestPolicy factories.
     * @param pipelineOptions The optional properties that can be set on the created HttpPipeline.
     * @param requestPolicyFactories The RequestPolicy factories to use.
     * @return The built HttpPipeline.
     */
    public static HttpPipeline build(HttpPipelineOptions pipelineOptions, Iterable<RequestPolicyFactory> requestPolicyFactories) {
        final HttpPipelineBuilder builder = new HttpPipelineBuilder(pipelineOptions);
        if (requestPolicyFactories != null) {
            for (final RequestPolicyFactory requestPolicyFactory : requestPolicyFactories) {
                builder.withRequestPolicy(requestPolicyFactory);
            }
        }
        return builder.build();
    }
}
