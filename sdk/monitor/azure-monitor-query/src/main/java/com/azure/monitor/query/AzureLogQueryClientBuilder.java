// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.monitor.query.log.implementation.AzureLogAnalyticsImplBuilder;

final class AzureLogQueryClientBuilder {

    private final AzureLogAnalyticsImplBuilder innerBuilder = new AzureLogAnalyticsImplBuilder();
    private TokenCredential tokenCredential;

    /**
     * Sets server parameter.
     *
     * @param host the host value.
     * @return the AzureLogQueryClientBuilder.
     */
    public AzureLogQueryClientBuilder host(String host) {
        innerBuilder.host(host);
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     * @param pipeline the pipeline value.
     *
     * @return the AzureLogQueryClientBuilder.
     */
    public AzureLogQueryClientBuilder pipeline(HttpPipeline pipeline) {
        innerBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     * @param httpClient the httpClient value.
     *
     * @return the AzureLogQueryClientBuilder.
     */
    public AzureLogQueryClientBuilder httpClient(HttpClient httpClient) {
        innerBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     * @param configuration the configuration value.
     *
     * @return the AzureLogQueryClientBuilder.
     */
    public AzureLogQueryClientBuilder configuration(Configuration configuration) {
        innerBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     * @param httpLogOptions the httpLogOptions value.
     *
     * @return the AzureLogQueryClientBuilder.
     */
    public AzureLogQueryClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        innerBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     * @param retryPolicy the retryPolicy value.
     *
     * @return the AzureLogQueryClientBuilder.
     */
    public AzureLogQueryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        innerBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     *
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the AzureLogQueryClientBuilder.
     */
    public AzureLogQueryClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        innerBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * Sets The TokenCredential used for authentication.
     * @param tokenCredential the tokenCredential value.
     *
     * @return the AzureLogQueryClientBuilder.
     */
    public AzureLogQueryClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        innerBuilder.credential(tokenCredential);
        return this;
    }

    public AzureLogQueryClient buildClient() {
        return new AzureLogQueryClient(buildAsyncClient());
    }

    private AzureLogQueryAsyncClient buildAsyncClient() {
        BearerTokenAuthenticationPolicy tokenPolicy = new BearerTokenAuthenticationPolicy(this.tokenCredential, " https://api.loganalytics.io" +
            "/.default");
        innerBuilder.addPolicy(tokenPolicy);
        return new AzureLogQueryAsyncClient(innerBuilder.buildClient());
    }
}
