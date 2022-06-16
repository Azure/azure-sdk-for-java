// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

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
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.ingestion.implementation.IngestionUsingDataCollectionRulesClientBuilder;
import com.azure.monitor.ingestion.implementation.IngestionUsingDataCollectionRulesServiceVersion;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Fluent builder for creating instances of {@link LogsIngestionClient} and {@link LogsIngestionAsyncClient}.
 */
@ServiceClientBuilder(serviceClients = {LogsIngestionClient.class, LogsIngestionAsyncClient.class})
public final class LogsIngestionClientBuilder implements ConfigurationTrait<LogsIngestionClientBuilder>,
        HttpTrait<LogsIngestionClientBuilder>, EndpointTrait<LogsIngestionClientBuilder>, TokenCredentialTrait<LogsIngestionClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(LogsIngestionClientBuilder.class);
    private final IngestionUsingDataCollectionRulesClientBuilder innerLogBuilder =
            new IngestionUsingDataCollectionRulesClientBuilder();

    /**
     * Sets the log query endpoint.
     * @param endpoint the host value.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    @Override
    public LogsIngestionClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
            innerLogBuilder.endpoint(endpoint);
            return this;
        } catch (MalformedURLException exception) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'endpoint' must be a valid URL.", exception));
        }
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     * @param pipeline the pipeline value.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    @Override
    public LogsIngestionClientBuilder pipeline(HttpPipeline pipeline) {
        innerLogBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     * @param httpClient the httpClient value.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    @Override
    public LogsIngestionClientBuilder httpClient(HttpClient httpClient) {
        innerLogBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     * @param configuration the configuration value.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    @Override
    public LogsIngestionClientBuilder configuration(Configuration configuration) {
        innerLogBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     * @param httpLogOptions the httpLogOptions value.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    @Override
    public LogsIngestionClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        innerLogBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     * @param retryPolicy the retryPolicy value.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    public LogsIngestionClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        innerLogBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    @Override
    public LogsIngestionClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        innerLogBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * Sets the retry options for this client.
     * @param retryOptions the retry options for this client.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    @Override
    public LogsIngestionClientBuilder retryOptions(RetryOptions retryOptions) {
        innerLogBuilder.retryOptions(retryOptions);
        return this;
    }

    /**
     * Sets The TokenCredential used for authentication.
     * @param tokenCredential the tokenCredential value.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    @Override
    public LogsIngestionClientBuilder credential(TokenCredential tokenCredential) {
        innerLogBuilder.credential(tokenCredential);
        return this;
    }

    /**
     * Set the {@link ClientOptions} used for creating the client.
     * @param clientOptions The {@link ClientOptions}.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    @Override
    public LogsIngestionClientBuilder clientOptions(ClientOptions clientOptions) {
        innerLogBuilder.clientOptions(clientOptions);
        return this;
    }

    /**
     * The service version to use when creating the client.
     * @param serviceVersion The {@link LogsIngestionServiceVersion}.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    public LogsIngestionClientBuilder serviceVersion(LogsIngestionServiceVersion serviceVersion) {
        innerLogBuilder.serviceVersion(IngestionUsingDataCollectionRulesServiceVersion.valueOf(serviceVersion.getVersion()));
        return this;
    }

    /**
     * Creates a synchronous client with the configured options in this builder.
     * @return A synchronous {@link LogsIngestionClient}.
     */
    public LogsIngestionClient buildClient() {
        return new LogsIngestionClient(buildAsyncClient());
    }

    /**
     * Creates an asynchronous client with the configured options in this builder.
     * @return An asynchronous {@link LogsIngestionAsyncClient}.
     */
    public LogsIngestionAsyncClient buildAsyncClient() {
        return new LogsIngestionAsyncClient(innerLogBuilder.buildAsyncClient());
    }

}
