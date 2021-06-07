// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.query.metrics.implementation.MonitorManagementClientImplBuilder;
import com.azure.monitor.query.metricsdefinitions.implementation.MetricsDefinitionsClientImplBuilder;
import com.azure.monitor.query.metricsnamespaces.implementation.MetricsNamespacesClientImplBuilder;

/**
 * Fluent builder for creating instances of {@link MetricsQueryClient} and {@link MetricsQueryAsyncClient}.
 */
@ServiceClientBuilder(serviceClients = {MetricsQueryClient.class, MetricsQueryAsyncClient.class})
public final class MetricsQueryClientBuilder {

    private final MonitorManagementClientImplBuilder innerMetricsBuilder = new MonitorManagementClientImplBuilder();
    private final MetricsDefinitionsClientImplBuilder innerMetricsDefinitionsBuilder =
            new MetricsDefinitionsClientImplBuilder();
    private final MetricsNamespacesClientImplBuilder innerMetricsNamespaceBuilder =
            new MetricsNamespacesClientImplBuilder();
    private final ClientLogger logger = new ClientLogger(MetricsQueryClientBuilder.class);
    private ClientOptions clientOptions;
    private MetricsQueryServiceVersion serviceVersion;


    /**
     * Sets the metrics query endpoint.
     * @param endpoint the host value.
     * @return the MetricsClientBuilder.
     */
    public MetricsQueryClientBuilder endpoint(String endpoint) {
        innerMetricsBuilder.host(endpoint);
        innerMetricsDefinitionsBuilder.host(endpoint);
        innerMetricsNamespaceBuilder.host(endpoint);
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     * @param pipeline the pipeline value.
     * @return the MetricsClientBuilder.
     */
    public MetricsQueryClientBuilder pipeline(HttpPipeline pipeline) {
        innerMetricsBuilder.pipeline(pipeline);
        innerMetricsDefinitionsBuilder.pipeline(pipeline);
        innerMetricsNamespaceBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     * @param httpClient the httpClient value.
     * @return the MetricsClientBuilder.
     */
    public MetricsQueryClientBuilder httpClient(HttpClient httpClient) {
        innerMetricsBuilder.httpClient(httpClient);
        innerMetricsDefinitionsBuilder.httpClient(httpClient);
        innerMetricsNamespaceBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     * @param configuration the configuration value.
     * @return the MetricsClientBuilder.
     */
    public MetricsQueryClientBuilder configuration(Configuration configuration) {
        innerMetricsBuilder.configuration(configuration);
        innerMetricsDefinitionsBuilder.configuration(configuration);
        innerMetricsNamespaceBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     * @param httpLogOptions the httpLogOptions value.
     * @return the MetricsClientBuilder.
     */
    public MetricsQueryClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        innerMetricsBuilder.httpLogOptions(httpLogOptions);
        innerMetricsDefinitionsBuilder.httpLogOptions(httpLogOptions);
        innerMetricsNamespaceBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     * @param retryPolicy the retryPolicy value.
     * @return the MetricsClientBuilder.
     */
    public MetricsQueryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        innerMetricsBuilder.retryPolicy(retryPolicy);
        innerMetricsDefinitionsBuilder.retryPolicy(retryPolicy);
        innerMetricsNamespaceBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the MetricsClientBuilder.
     */
    public MetricsQueryClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        innerMetricsBuilder.addPolicy(customPolicy);
        innerMetricsDefinitionsBuilder.addPolicy(customPolicy);
        innerMetricsNamespaceBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * Sets The TokenCredential used for authentication.
     * @param tokenCredential the tokenCredential value.
     * @return the MetricsClientBuilder.
     */
    public MetricsQueryClientBuilder credential(TokenCredential tokenCredential) {
        innerMetricsBuilder.credential(tokenCredential);
        innerMetricsDefinitionsBuilder.credential(tokenCredential);
        innerMetricsNamespaceBuilder.credential(tokenCredential);
        return this;
    }

    /**
     * Set the {@link ClientOptions} used for creating the client.
     * @param clientOptions The {@link ClientOptions}.
     * @return the {@link MetricsQueryClientBuilder}
     */
    public MetricsQueryClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * The service version to use when creating the client.
     * @param serviceVersion The {@link MetricsQueryServiceVersion}.
     * @return the {@link MetricsQueryClientBuilder}
     */
    public MetricsQueryClientBuilder serviceVersion(MetricsQueryServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Creates a synchronous client with the configured options in this builder.
     * @return A synchronous {@link MetricsQueryClient}.
     */
    public MetricsQueryClient buildClient() {
        return new MetricsQueryClient(buildAsyncClient());
    }

    /**
     * Creates an asynchronous client with the configured options in this builder.
     * @return An asynchronous {@link MetricsQueryAsyncClient}.
     */
    public MetricsQueryAsyncClient buildAsyncClient() {
        logger.info("Using service version " + this.serviceVersion);
        return new MetricsQueryAsyncClient(innerMetricsBuilder.buildClient(),
                innerMetricsNamespaceBuilder.buildClient(), innerMetricsDefinitionsBuilder.buildClient());
    }

}
