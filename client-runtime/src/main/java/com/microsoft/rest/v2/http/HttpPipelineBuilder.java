/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.microsoft.rest.v2.credentials.ServiceClientCredentials;
import com.microsoft.rest.v2.policy.CookiePolicyFactory;
import com.microsoft.rest.v2.policy.CredentialsPolicyFactory;
import com.microsoft.rest.v2.policy.DecodingPolicyFactory;
import com.microsoft.rest.v2.policy.HostPolicyFactory;
import com.microsoft.rest.v2.policy.HttpLogDetailLevel;
import com.microsoft.rest.v2.policy.HttpLoggingPolicyFactory;
import com.microsoft.rest.v2.policy.ProxyAuthenticationPolicyFactory;
import com.microsoft.rest.v2.policy.RequestIdPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RetryPolicyFactory;
import com.microsoft.rest.v2.policy.TimeoutPolicyFactory;
import com.microsoft.rest.v2.policy.UserAgentPolicyFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A builder class that can be used to create a HttpPipeline.
 */
public final class HttpPipelineBuilder {
    /**
     * The optional properties that will be set on the created HTTP pipelines.
     */
    private HttpPipelineOptions options;

    /**
     * The list of RequestPolicy factories that will be applied to HTTP requests and responses.
     * The factories appear in this list in the reverse order that they will be applied to
     * outgoing requests.
     */
    private final List<RequestPolicyFactory> requestPolicyFactories;

    /**
     * Create a new HttpPipeline builder.
     */
    public HttpPipelineBuilder() {
        this(null);
    }

    /**
     * Create a new HttpPipeline builder.
     *
     * @param options The optional properties that will be set on the created HTTP pipelines.
     */
    public HttpPipelineBuilder(HttpPipelineOptions options) {
        this.options = options;
        this.requestPolicyFactories = new ArrayList<>();
    }

    /**
     * Get the RequestPolicy factories in this HttpPipeline builder.
     * @return the RequestPolicy factories in this HttpPipeline builder.
     */
    List<RequestPolicyFactory> requestPolicyFactories() {
        return requestPolicyFactories;
    }

    /**
     * Get the options for this HttpPipeline builder.
     * @return the options for this HttpPipeline builder.
     */
    HttpPipelineOptions options() {
        return options;
    }

    /**
     * Set the HttpClient that will be used by HttpPipelines that are created by this Builder.
     * @param httpClient The HttpClient to use.
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withHttpClient(HttpClient httpClient) {
        if (options == null) {
            options = new HttpPipelineOptions();
        }
        options.withHttpClient(httpClient);
        return this;
    }

    /**
     * Set the Logger that will be used for each RequestPolicy within the created HttpPipeline.
     * @param logger The Logger to provide to each RequestPolicy.
     * @return This HttpPipeline options object.
     */
    public HttpPipelineBuilder withLogger(HttpPipelineLogger logger) {
        if (options == null) {
            options = new HttpPipelineOptions();
        }
        options.withLogger(logger);
        return this;
    }

    /**
     * Add the provided RequestPolicy factory to this HttpPipeline builder.
     * @param requestPolicyFactory The RequestPolicy factory to add to this HttpPipeline builder.
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withRequestPolicy(RequestPolicyFactory requestPolicyFactory) {
        return withRequestPolicy(requestPolicyFactories.size(), requestPolicyFactory);
    }

    /**
     * Add the provided RequestPolicy factory to this HttpPipeline builder
     * at the provided index in the pipeline.
     * @param index The index to insert the provided RequestPolicy factory.
     * @param requestPolicyFactory The RequestPolicy factory to add to this
     *                             HttpPipeline builder.
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withRequestPolicy(int index, RequestPolicyFactory requestPolicyFactory) {
        // The requestPolicyFactories list is in reverse order that the
        // policies will be in. The caller of this method should be
        // providing the index based on the policy list, not the factory
        // list.
        final int insertIndex = requestPolicyFactories.size() - index;
        requestPolicyFactories.add(insertIndex, requestPolicyFactory);
        return this;
    }

    /**
     * Add the provided RequestPolicy factories to this HttpPipeline builder.
     * @param requestPolicyFactories The RequestPolicy factories to add to this
     *                               HttpPipeline builder.
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withRequestPolicies(RequestPolicyFactory... requestPolicyFactories) {
        for (RequestPolicyFactory factory : requestPolicyFactories) {
            withRequestPolicy(factory);
        }
        return this;
    }

    /**
     * Add a RequestPolicy which stores and adds cookies across multiple
     * requests and responses.
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withCookiePolicy() {
        return withRequestPolicy(new CookiePolicyFactory());
    }

    /**
     * Add a RequestPolicy which applies the given ServiceClientCredentials to
     * outgoing requests.
     * @param credentials The credentials to apply to requests.
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withCredentialsPolicy(ServiceClientCredentials credentials) {
        return withRequestPolicy(new CredentialsPolicyFactory(credentials));
    }

    /**
     * Adds a RequestPolicy which decodes the headers and body of incoming
     * responses.
     * Required for services that need to deserialize JSON or XML responses.
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withDecodingPolicy() {
        return withRequestPolicy(new DecodingPolicyFactory());
    }

    /**
     * Adds a RequestPolicy which sets the host on all outgoing requests.
     * @param host The hostname to use in all outgoing requests.
     * @return This HttpPipelineBuilder.
     */
    public HttpPipelineBuilder withHostPolicy(String host) {
        return withRequestPolicy(new HostPolicyFactory(host));
    }

    /**
     * Adds a RequestPolicy which logs all HTTP traffic using SLF4J.
     * @param level The HTTP logging detail level.
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withHttpLoggingPolicy(HttpLogDetailLevel level) {
        return withHttpLoggingPolicy(level, false);
    }

    /**
     * Adds a RequestPolicy which logs all HTTP traffic using SLF4J.
     * @param level The HTTP logging detail level.
     * @param prettyPrintJSON Whether or not to pretty print JSON message bodies.
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withHttpLoggingPolicy(HttpLogDetailLevel level, boolean prettyPrintJSON) {
        return withRequestPolicy(new HttpLoggingPolicyFactory(level, prettyPrintJSON));
    }

    /**
     * Adds a RequestPolicy which adds proxy authentication headers to
     * outgoing requests.
     * @param username The username for authentication.
     * @param password The password for authentication.
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withProxyAuthenticationPolicy(String username, String password) {
        return withRequestPolicy(new ProxyAuthenticationPolicyFactory(username, password));
    }

    /**
     * Adds a RequestPolicy which adds a per-request ID to the
     * "x-ms-client-request-id" header to outgoing requests.
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withRequestIdPolicy() {
        return withRequestPolicy(new RequestIdPolicyFactory());
    }

    /**
     * Adds a RequestPolicy which retries a failed request up to the given
     * number of times.
     * @param maxRetries The maximum number of times to retry failed requests.
     * @param delayTime the delay between retries
     * @param timeUnit the time unit of the delay
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withRetryPolicy(int maxRetries, long delayTime, TimeUnit timeUnit) {
        return withRequestPolicy(new RetryPolicyFactory(maxRetries, delayTime, timeUnit));
    }

    /**
     * Adds a RequestPolicy which fails a request if it does not complete by
     * the time the given interval elapses.
     * @param timeout The amount of time to wait before timing out a request.
     * @param unit The unit of time associated with the timeout parameter.
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withTimeoutPolicy(long timeout, TimeUnit unit) {
        return withRequestPolicy(new TimeoutPolicyFactory(timeout, unit));
    }

    /**
     * Add a RequestPolicy that will add the provided UserAgent header to each
     * outgoing HttpRequest.
     * @param userAgent The userAgent header value to add to each outgoing HttpRequest.
     * @return This HttpPipeline builder.
     */
    public HttpPipelineBuilder withUserAgentPolicy(String userAgent) {
        return withRequestPolicy(new UserAgentPolicyFactory(userAgent));
    }

    /**
     * Create a new HttpPipeline from the RequestPolicy factories that have been added to this
     * HttpPipeline builder.
     * @return The created HttpPipeline.
     */
    public HttpPipeline build() {
        final int requestPolicyCount = requestPolicyFactories.size();
        final RequestPolicyFactory[] requestPolicyFactoryArray = new RequestPolicyFactory[requestPolicyCount];
        return new HttpPipeline(requestPolicyFactories.toArray(requestPolicyFactoryArray), options);
    }
}