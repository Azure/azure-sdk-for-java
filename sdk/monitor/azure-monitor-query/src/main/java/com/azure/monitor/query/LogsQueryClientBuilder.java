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
import com.azure.monitor.query.implementation.logs.AzureLogAnalyticsImplBuilder;
import com.azure.monitor.query.models.LogsQueryClientAudience;

/**
 * Fluent builder for creating instances of {@link LogsQueryClient} and {@link LogsQueryAsyncClient}.
 *
 * <p><strong>Instantiating an asynchronous Logs query Client</strong></p>
 *
 * {@codesnippet com.azure.monitor.query.LogsQueryAsyncClient.instantiation}
 *
 * <p><strong>Instantiating a synchronous Logs query Client</strong></p>
 *
 * {@codesnippet com.azure.monitor.query.LogsQueryClient.instantiation}
 */
@ServiceClientBuilder(serviceClients = {LogsQueryClient.class, LogsQueryAsyncClient.class})
public final class LogsQueryClientBuilder {
    private final ClientLogger logger = new ClientLogger(LogsQueryClientBuilder.class);
    private final AzureLogAnalyticsImplBuilder innerLogBuilder = new AzureLogAnalyticsImplBuilder();
    private ClientOptions clientOptions;
    private LogsQueryServiceVersion serviceVersion;

    /**
     * Sets the log query endpoint.
     * @param endpoint the host value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    public LogsQueryClientBuilder endpoint(String endpoint) {
        innerLogBuilder.host(endpoint);
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     * @param pipeline the pipeline value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    public LogsQueryClientBuilder pipeline(HttpPipeline pipeline) {
        innerLogBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     * @param httpClient the httpClient value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    public LogsQueryClientBuilder httpClient(HttpClient httpClient) {
        innerLogBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     * @param configuration the configuration value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    public LogsQueryClientBuilder configuration(Configuration configuration) {
        innerLogBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     * @param httpLogOptions the httpLogOptions value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    public LogsQueryClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        innerLogBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     * @param retryPolicy the retryPolicy value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    public LogsQueryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        innerLogBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the {@link LogsQueryClientBuilder}.
     */
    public LogsQueryClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        innerLogBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * Sets The TokenCredential used for authentication.
     * @param tokenCredential the tokenCredential value.
     * @return the {@link LogsQueryClientBuilder}.
     */
    public LogsQueryClientBuilder credential(TokenCredential tokenCredential) {
        innerLogBuilder.credential(tokenCredential);
        return this;
    }

    /**
     * Set the {@link ClientOptions} used for creating the client.
     * @param clientOptions The {@link ClientOptions}.
     * @return the {@link LogsQueryClientBuilder}.
     */
    public LogsQueryClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the audience to use for authentication with Azure Active Directory. The Azure Public Cloud audience will be
     * used if the property is null.
     * @param audience audience to use for authentication with Azure Active Directory. The Azure Public Cloud audience
     * will be used if the property is null.
     * @return the {@link LogsQueryClientBuilder}.
     */
    public LogsQueryClientBuilder audience(LogsQueryClientAudience audience) {
        innerLogBuilder.audience(audience == null ? null : audience.toString());
        return this;
    }

    /**
     * The service version to use when creating the client.
     * @param serviceVersion The {@link LogsQueryServiceVersion}.
     * @return the {@link LogsQueryClientBuilder}.
     */
    public LogsQueryClientBuilder serviceVersion(LogsQueryServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Creates a synchronous client with the configured options in this builder.
     * @return A synchronous {@link LogsQueryClient}.
     */
    public LogsQueryClient buildClient() {
        return new LogsQueryClient(buildAsyncClient());
    }

    /**
     * Creates an asynchronous client with the configured options in this builder.
     * @return An asynchronous {@link LogsQueryAsyncClient}.
     */
    public LogsQueryAsyncClient buildAsyncClient() {
        logger.info("Using service version " + this.serviceVersion);
        return new LogsQueryAsyncClient(innerLogBuilder.buildClient());
    }
}
