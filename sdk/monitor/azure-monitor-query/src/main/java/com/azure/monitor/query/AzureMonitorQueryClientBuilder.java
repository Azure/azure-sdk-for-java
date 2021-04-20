// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.monitor.query.log.implementation.AzureLogAnalyticsImplBuilder;
import com.azure.monitor.query.metric.implementation.MonitorManagementClientImplBuilder;

public class AzureMonitorQueryClientBuilder {

    private final AzureLogAnalyticsImplBuilder innerLogBuilder = new AzureLogAnalyticsImplBuilder();
    private final MonitorManagementClientImplBuilder innerMetricsBuilder = new MonitorManagementClientImplBuilder();


    /**
     * Sets the log query endpoint.
     *
     * @param logEndpoint the host value.
     * @return the AzureMonitorQueryClientBuilder.
     */
    public AzureMonitorQueryClientBuilder logEndpoint(String logEndpoint) {
        innerLogBuilder.host(logEndpoint);
        return this;
    }

    /**
     * Sets the metrics query endpoint.
     *
     * @param metricsEndpoint the host value.
     * @return the AzureMonitorQueryClientBuilder.
     */
    public AzureMonitorQueryClientBuilder metricsEndpoint(String metricsEndpoint) {
        innerMetricsBuilder.host(metricsEndpoint);
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     * @param pipeline the pipeline value.
     *
     * @return the AzureMonitorQueryClientBuilder.
     */
    public AzureMonitorQueryClientBuilder pipeline(HttpPipeline pipeline) {
        innerLogBuilder.pipeline(pipeline);
        innerMetricsBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     * @param httpClient the httpClient value.
     *
     * @return the AzureMonitorQueryClientBuilder.
     */
    public AzureMonitorQueryClientBuilder httpClient(HttpClient httpClient) {
        innerLogBuilder.httpClient(httpClient);
        innerMetricsBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     * @param configuration the configuration value.
     *
     * @return the AzureMonitorQueryClientBuilder.
     */
    public AzureMonitorQueryClientBuilder configuration(Configuration configuration) {
        innerLogBuilder.configuration(configuration);
        innerMetricsBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     * @param httpLogOptions the httpLogOptions value.
     *
     * @return the AzureMonitorQueryClientBuilder.
     */
    public AzureMonitorQueryClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        innerLogBuilder.httpLogOptions(httpLogOptions);
        innerMetricsBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     * @param retryPolicy the retryPolicy value.
     *
     * @return the AzureMonitorQueryClientBuilder.
     */
    public AzureMonitorQueryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        innerLogBuilder.retryPolicy(retryPolicy);
        innerMetricsBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     *
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the AzureMonitorQueryClientBuilder.
     */
    public AzureMonitorQueryClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        innerLogBuilder.addPolicy(customPolicy);
        innerMetricsBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * Sets The TokenCredential used for authentication.
     * @param tokenCredential the tokenCredential value.
     *
     * @return the AzureMonitorQueryClientBuilder.
     */
    public AzureMonitorQueryClientBuilder credential(TokenCredential tokenCredential) {
        innerLogBuilder.credential(tokenCredential);
        innerMetricsBuilder.credential(tokenCredential);
        return this;
    }

    public AzureMonitorQueryClient buildClient() {
        return new AzureMonitorQueryClient(buildAsyncClient());
    }

    private AzureMonitorQueryAsyncClient buildAsyncClient() {
        return new AzureMonitorQueryAsyncClient(innerLogBuilder.buildClient(), innerMetricsBuilder.buildClient());
    }

}
