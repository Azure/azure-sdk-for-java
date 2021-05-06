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
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.monitor.query.base.LogsBaseClient;
import com.azure.monitor.query.log.implementation.AzureLogAnalyticsImplBuilder;

/**
 *
 */
@ServiceClientBuilder(serviceClients = {LogsClient.class, LogsAsyncClient.class})
public final class LogsClientBuilder {

    private final AzureLogAnalyticsImplBuilder innerLogBuilder = new AzureLogAnalyticsImplBuilder();
    private ClientOptions clientOptions;
    private MetricsServiceVersion serviceVersion;

    /**
     * Sets the log query endpoint.
     * @param endpoint the host value.
     *
     * @return the LogsClientBuilder.
     */
    public LogsClientBuilder endpoint(String endpoint) {
        innerLogBuilder.host(endpoint);
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     * @param pipeline the pipeline value.
     *
     * @return the LogsClientBuilder.
     */
    public LogsClientBuilder pipeline(HttpPipeline pipeline) {
        innerLogBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     * @param httpClient the httpClient value.
     *
     * @return the LogsClientBuilder.
     */
    public LogsClientBuilder httpClient(HttpClient httpClient) {
        innerLogBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     * @param configuration the configuration value.
     *
     * @return the LogsClientBuilder.
     */
    public LogsClientBuilder configuration(Configuration configuration) {
        innerLogBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     * @param httpLogOptions the httpLogOptions value.
     *
     * @return the LogsClientBuilder.
     */
    public LogsClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        innerLogBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     * @param retryPolicy the retryPolicy value.
     *
     * @return the LogsClientBuilder.
     */
    public LogsClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        innerLogBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     * @param customPolicy The custom Http pipeline policy to add.
     *
     * @return the LogsClientBuilder.
     */
    public LogsClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        innerLogBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * Sets The TokenCredential used for authentication.
     * @param tokenCredential the tokenCredential value.
     *
     * @return the LogsClientBuilder.
     */
    public LogsClientBuilder credential(TokenCredential tokenCredential) {
        innerLogBuilder.credential(tokenCredential);
        return this;
    }

    /**
     * @param clientOptions
     *
     * @return
     */
    public LogsClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * @param serviceVersion
     *
     * @return
     */
    public LogsClientBuilder serviceVersion(MetricsServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * @return
     */
    public LogsClient buildClient() {
        return new LogsClient(buildAsyncClient());
    }

    /**
     * @return
     */
    public LogsAsyncClient buildAsyncClient() {
        return new LogsAsyncClient(innerLogBuilder.buildClient());
    }


    /**
     * @return
     */
    public LogsBaseClientBuilder base() {
        return new LogsBaseClientBuilder();
    }

    public final class LogsBaseClientBuilder {
        private ObjectSerializer serializer;

        public LogsBaseClientBuilder serializer(ObjectSerializer serializer) {
            this.serializer = serializer;
            return this;
        }

        public LogsBaseClient buildBaseClient() {
            return new LogsBaseClient();
        }
    }


}
