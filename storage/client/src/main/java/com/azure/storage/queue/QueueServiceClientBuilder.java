// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.configuration.Configuration;
import com.azure.storage.common.credentials.SASTokenCredential;

/**
 * Fluent builder for queue service clients
 */
public final class QueueServiceClientBuilder {
    private final QueueServiceAsyncClientBuilder builder;

    QueueServiceClientBuilder() {
        builder = new QueueServiceAsyncClientBuilder();
    }

    /**
     * @return a new instance of QueueServiceAsyncClient constructed with options stored in the builder
     */
    public QueueServiceClient build() {
        return new QueueServiceClient(builder.build());
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token)
     * @param endpoint URL of the service
     * @return the updated QueueServiceClientBuilder object
     */
    public QueueServiceClientBuilder endpoint(String endpoint) {
        builder.endpoint(endpoint);
        return this;
    }

    /**
     * Sets the credential used to authorize requests sent to the service
     * @param credential authorization credential
     * @return the updated QueueServiceClientBuilder object
     */
    public QueueServiceClientBuilder credential(SASTokenCredential credential) {
        builder.credential(credential);
        return this;
    }

    /**
     * Sets the connection string for the service, parses it for authentication information (account name, account key)
     * @param connectionString connection string from access keys section
     * @return the updated QueueServiceClientBuilder object
     */
    public QueueServiceClientBuilder connectionString(String connectionString) {
        builder.connectionString(connectionString);
        return this;
    }

    /**
     * Sets the http client used to send service requests
     * @param httpClient http client to send requests
     * @return the updated QueueServiceClientBuilder object
     */
    public QueueServiceClientBuilder httpClient(HttpClient httpClient) {
        builder.httpClient(httpClient);
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     * @param pipelinePolicy a pipeline policy
     * @return the updated QueueServiceClientBuilder object
     */
    public QueueServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        builder.addPolicy(pipelinePolicy);
        return this;
    }

    /**
     * Sets the logging level for service requests
     * @param logLevel logging level
     * @return the updated QueueServiceClientBuilder object
     */
    public QueueServiceClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        builder.httpLogDetailLevel(logLevel);
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values used to build the client with
     * when they are not set in the builder, defaults to Configuration.NONE
     * @param configuration configuration store
     * @return the updated QueueServiceClientBuilder object
     */
    public QueueServiceClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }
}
