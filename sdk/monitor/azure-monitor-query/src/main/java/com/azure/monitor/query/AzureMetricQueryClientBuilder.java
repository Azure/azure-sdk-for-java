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
import com.azure.monitor.query.metric.implementation.MonitorManagementClientImplBuilder;

public class AzureMetricQueryClientBuilder {

    private final MonitorManagementClientImplBuilder innerBuilder = new MonitorManagementClientImplBuilder();

    /**
     * Sets server parameter.
     *
     * @param host the host value.
     * @return the AzureMetricQueryClientBuilder.
     */
    public AzureMetricQueryClientBuilder host(String host) {
        innerBuilder.host(host);
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     * @param pipeline the pipeline value.
     *
     * @return the AzureMetricQueryClientBuilder.
     */
    public AzureMetricQueryClientBuilder pipeline(HttpPipeline pipeline) {
        innerBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     * @param httpClient the httpClient value.
     *
     * @return the AzureMetricQueryClientBuilder.
     */
    public AzureMetricQueryClientBuilder httpClient(HttpClient httpClient) {
        innerBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     * @param configuration the configuration value.
     *
     * @return the AzureMetricQueryClientBuilder.
     */
    public AzureMetricQueryClientBuilder configuration(Configuration configuration) {
        innerBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     * @param httpLogOptions the httpLogOptions value.
     *
     * @return the AzureMetricQueryClientBuilder.
     */
    public AzureMetricQueryClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        innerBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     * @param retryPolicy the retryPolicy value.
     *
     * @return the AzureMetricQueryClientBuilder.
     */
    public AzureMetricQueryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        innerBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     *
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the AzureMetricQueryClientBuilder.
     */
    public AzureMetricQueryClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        innerBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * Sets The TokenCredential used for authentication.
     * @param tokenCredential the tokenCredential value.
     *
     * @return the AzureMetricQueryClientBuilder.
     */
    public AzureMetricQueryClientBuilder credential(TokenCredential tokenCredential) {
        innerBuilder.credential(tokenCredential);
        return this;
    }

    public AzureMetricQueryClient buildClient() {
        return new AzureMetricQueryClient(buildAsyncClient());
    }

    private AzureMetricQueryAsyncClient buildAsyncClient() {
        return new AzureMetricQueryAsyncClient(innerBuilder.buildClient());
    }
}
