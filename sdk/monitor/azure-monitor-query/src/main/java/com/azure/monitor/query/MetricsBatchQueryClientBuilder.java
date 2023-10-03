// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.monitor.query.implementation.metricsbatch.AzureMonitorMetricBatchBuilder;

/**
 * Fluent builder for creating instances of {@link MetricsBatchQueryClient} and {@link MetricsBatchQueryAsyncClient}.
 */
@ServiceClientBuilder(serviceClients = {MetricsBatchQueryClient.class, MetricsBatchQueryAsyncClient.class})
public final class MetricsBatchQueryClientBuilder implements EndpointTrait<MetricsBatchQueryClientBuilder>,
    HttpTrait<MetricsBatchQueryClientBuilder>, ConfigurationTrait<MetricsBatchQueryClientBuilder>, TokenCredentialTrait<MetricsBatchQueryClientBuilder> {

    private final AzureMonitorMetricBatchBuilder innerMetricsBatchBuilder = new AzureMonitorMetricBatchBuilder();

    /**
     * Sets the metrics batch query endpoint.
     * @param endpoint the endpoint.
     * @return the {@link MetricsBatchQueryClientBuilder}.
     */
    @Override
    public MetricsBatchQueryClientBuilder endpoint(String endpoint) {
        innerMetricsBatchBuilder.endpoint(endpoint);
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     * @param pipeline the pipeline value.
     * @return the {@link MetricsBatchQueryClientBuilder}.
     */
    @Override
    public MetricsBatchQueryClientBuilder pipeline(HttpPipeline pipeline) {
        innerMetricsBatchBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     * @param httpClient the httpClient value.
     * @return the {@link MetricsBatchQueryClientBuilder}.
     */
    @Override
    public MetricsBatchQueryClientBuilder httpClient(HttpClient httpClient) {
        innerMetricsBatchBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     * @param configuration the configuration value.
     * @return the {@link MetricsBatchQueryClientBuilder}.
     */
    @Override
    public MetricsBatchQueryClientBuilder configuration(Configuration configuration) {
        innerMetricsBatchBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     * @param httpLogOptions the httpLogOptions value.
     * @return the {@link MetricsBatchQueryClientBuilder}.
     */
    @Override
    public MetricsBatchQueryClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        innerMetricsBatchBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     * @param retryPolicy the retryPolicy value.
     * @return the {@link MetricsBatchQueryClientBuilder}.
     */
    public MetricsBatchQueryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        innerMetricsBatchBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Sets the {@link RetryOptions} used for creating the client.
     * @param retryOptions The {@link RetryOptions}.
     * @return the updated {@link MetricsBatchQueryClientBuilder}.
     */
    @Override
    public MetricsBatchQueryClientBuilder retryOptions(RetryOptions retryOptions) {
        innerMetricsBatchBuilder.retryOptions(retryOptions);
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the {@link MetricsBatchQueryClientBuilder}.
     */
    @Override
    public MetricsBatchQueryClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        innerMetricsBatchBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * Sets The TokenCredential used for authentication.
     * @param tokenCredential the tokenCredential value.
     * @return the {@link MetricsBatchQueryClientBuilder}.
     */
    @Override
    public MetricsBatchQueryClientBuilder credential(TokenCredential tokenCredential) {
        innerMetricsBatchBuilder.credential(tokenCredential);
        return this;
    }

    /**
     * Set the {@link ClientOptions} used for creating the client.
     * @param clientOptions The {@link ClientOptions}.
     * @return the {@link MetricsBatchQueryClientBuilder}.
     */
    @Override
    public MetricsBatchQueryClientBuilder clientOptions(ClientOptions clientOptions) {
        innerMetricsBatchBuilder.clientOptions(clientOptions);
        return this;
    }

    /**
     * The service version to use when creating the client.
     * @param serviceVersion The {@link MetricsBatchQueryServiceVersion}.
     * @return the {@link MetricsBatchQueryClientBuilder}.
     */
    public MetricsBatchQueryClientBuilder serviceVersion(MetricsBatchQueryServiceVersion serviceVersion) {
        innerMetricsBatchBuilder.apiVersion(serviceVersion.getVersion());
        return this;
    }

    /**
     * Creates a synchronous client with the configured options in this builder.
     * @return A synchronous {@link MetricsBatchQueryClient}.
     */
    public MetricsBatchQueryClient buildClient() {
        return new MetricsBatchQueryClient(innerMetricsBatchBuilder.buildClient());
    }

    /**
     * Creates an asynchronous client with the configured options in this builder.
     * @return An asynchronous {@link MetricsBatchQueryAsyncClient}.
     */
    public MetricsBatchQueryAsyncClient buildAsyncClient() {
        return new MetricsBatchQueryAsyncClient(innerMetricsBatchBuilder.buildClient());
    }

}
