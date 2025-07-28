// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.monitor.query.metrics.models.MetricsQueryAudience;
import com.azure.monitor.query.MetricsQueryClientBuilder;
import com.azure.monitor.query.implementation.metricsbatch.AzureMonitorMetricBatchBuilder;
import com.azure.monitor.query.metrics.implementation.MonitorQueryMetricsClientBuilder;
import com.azure.monitor.query.metrics.implementation.MonitorQueryMetricsClientImpl;
import com.azure.monitor.query.metrics.implementation.MonitorQueryMetricsUtils;

public final class MetricsQueryClientBuilder implements EndpointTrait<MetricsQueryClientBuilder>, HttpTrait<MetricsQueryClientBuilder>,
    ConfigurationTrait<MetricsQueryClientBuilder>, TokenCredentialTrait<MetricsQueryClientBuilder> {

    private final MonitorQueryMetricsClientBuilder innerBuilder = new MonitorQueryMetricsClientBuilder();

    /**
     * Creates an instance of MetricsQueryClientBuilder.
     */
    public MetricsQueryClientBuilder() {
    }

    /**
     * Sets the metrics endpoint.
     * @param endpoint the endpoint.
     * @return the {@link MetricsQueryClientBuilder}.
     */
    @Override
    public MetricsQueryClientBuilder endpoint(String endpoint) {
        innerBuilder.endpoint(endpoint);
        return this;
    }

    /**
     * Sets the metrics audience.
     * @param audience the audience.
     * @return the {@link MetricsQueryClientBuilder}.
     */
    public MetricsQueryClientBuilder audience(MetricsQueryAudience audience) {
        innerBuilder.audience(audience);
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     * @param pipeline the pipeline value.
     * @return the {@link MetricsQueryClientBuilder}.
     */
    @Override
    public MetricsQueryClientBuilder pipeline(HttpPipeline pipeline) {
        innerBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     * @param httpClient the httpClient value.
     * @return the {@link MetricsQueryClientBuilder}.
     */
    @Override
    public MetricsQueryClientBuilder httpClient(HttpClient httpClient) {
        innerBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     * @param configuration the configuration value.
     * @return the {@link MetricsQueryClientBuilder}.
     */
    @Override
    public MetricsQueryClientBuilder configuration(Configuration configuration) {
        innerBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     * @param httpLogOptions the httpLogOptions value.
     * @return the {@link MetricsQueryClientBuilder}.
     */
    @Override
    public MetricsQueryClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        innerBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     * @param retryPolicy the retryPolicy value.
     * @return the {@link MetricsQueryClientBuilder}.
     */
    public MetricsQueryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        innerBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Sets the {@link RetryOptions} used for creating the client.
     * @param retryOptions The {@link RetryOptions}.
     * @return the updated {@link MetricsQueryClientBuilder}.
     */
    @Override
    public MetricsQueryClientBuilder retryOptions(RetryOptions retryOptions) {
        innerBuilder.retryOptions(retryOptions);
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the {@link MetricsQueryClientBuilder}.
     */
    @Override
    public MetricsQueryClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        innerBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * Sets The TokenCredential used for authentication.
     * @param tokenCredential the tokenCredential value.
     * @return the {@link MetricsQueryClientBuilder}.
     */
    @Override
    public MetricsQueryClientBuilder credential(TokenCredential tokenCredential) {
        innerBuilder.credential(tokenCredential);
        return this;
    }

    /**
     * Set the {@link ClientOptions} used for creating the client.
     * @param clientOptions The {@link ClientOptions}.
     * @return the {@link MetricsQueryClientBuilder}.
     */
    @Override
    public MetricsQueryClientBuilder clientOptions(ClientOptions clientOptions) {
        innerBuilder.clientOptions(clientOptions);
        return this;
    }

    /**
     * The service version to use when creating the client.
     * @param serviceVersion The {@link MetricsQueryServiceVersion}.
     * @return the {@link MetricsQueryClientBuilder}.
     */
    public MetricsQueryClientBuilder serviceVersion(MetricsQueryServiceVersion serviceVersion) {
        innerBuilder.serviceVersion(serviceVersion);
        return this;
    }

    /**
     * Creates a synchronous client with the configured options in this builder.
     * @return A synchronous {@link MetricsQueryClient}.
     */
    public MetricsQueryClient buildClient() {
        return new MetricsQueryClient(innerBuilder.buildClient());
    }

    /**
     * Creates an asynchronous client with the configured options in this builder.
     * @return An asynchronous {@link MetricsQueryAsyncClient}.
     */
    public MetricsQueryAsyncClient buildAsyncClient() {
        return new MetricsQueryAsyncClient(innerBuilder.buildClient());
    }
}
