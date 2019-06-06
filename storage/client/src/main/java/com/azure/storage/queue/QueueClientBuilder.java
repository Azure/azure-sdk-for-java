// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.configuration.Configuration;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.queue.models.SASTokenCredential;

/**
 * Fluent builder for queue clients
 */
public final class QueueClientBuilder {
    private final QueueAsyncClientBuilder builder;

    QueueClientBuilder() {
        builder = new QueueAsyncClientBuilder();
    }

    /**
     * Constructs an instance of QueueClient based on the configurations stored in the builder.
     * @return a new client instance
     */
    public QueueClient build() {
        return new QueueClient(builder.build());
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, queue name)
     * @param endpoint URL of the service
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder endpoint(String endpoint) {
        builder.endpoint(endpoint);
        return this;
    }

    /**
     * Sets the queue name
     * @param queueName Name of the queue
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder queueName(String queueName) {
        builder.queueName(queueName);
        return this;
    }

    /**
     * Sets the credentials used to authorize requests sent to the service
     * @param credentials authorization credentials
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder credentials(SASTokenCredential credentials) {
        builder.credentials(credentials);
        return this;
    }

    /**
     * Sets the connection string for the service, parses it for authentication information (account name, account key)
     * @param connectionString connection string from access keys section
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder connectionString(String connectionString) {
        builder.connectionString(connectionString);
        return this;
    }

    /**
     * Sets the http client used to send service requests
     * @param httpClient http client to send requests
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder httpClient(HttpClient httpClient) {
        builder.httpClient(httpClient);
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     * @param pipelinePolicy a pipeline policy
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        builder.addPolicy(pipelinePolicy);
        return this;
    }

    /**
     * Sets the logging level for service requests
     * @param logLevel logging level
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        builder.httpLogDetailLevel(logLevel);
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values used to build the client with
     * when they are not set in the builder, defaults to Configuration.NONE
     * @param configuration configuration store
     * @return the updated QueueClientBuilder object
     */
    public QueueClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }
}
