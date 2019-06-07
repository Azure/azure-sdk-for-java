// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.configuration.Configuration;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;

/**
 * Fluent builder for page blob clients.
 */
public final class PageBlobSyncClientBuilder {
    private PageBlobAsyncClientBuilder builder;

    PageBlobSyncClientBuilder() {
        builder = new PageBlobAsyncClientBuilder();
    }

    /**
     * Constructs an instance of PageBlobSyncClient based on the configurations stored in the builder.
     * @return a new client instance
     */
    public PageBlobSyncClient build() {
        return new PageBlobSyncClient(builder.build());
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, blob name)
     * @param endpoint URL of the service
     * @return the updated PageBlobSyncClientBuilder object
     */
    public PageBlobSyncClientBuilder endpoint(String endpoint) {
        builder.endpoint(endpoint);
        return this;
    }

    /**
     * Sets the credentials used to authorize requests sent to the service
     * @param credentials authorization credentials
     * @return the updated PageBlobSyncClientBuilder object
     */
    public PageBlobSyncClientBuilder credentials(SharedKeyCredentials credentials) {
        builder.credentials(credentials);
        return this;
    }

    /**
     * Sets the credentials used to authorize requests sent to the service
     * @param credentials authorization credentials
     * @return the updated PageBlobSyncClientBuilder object
     */
    public PageBlobSyncClientBuilder credentials(TokenCredentials credentials) {
        builder.credentials(credentials);
        return this;
    }

    /**
     * Clears the credentials used to authorize requests sent to the service
     * @return the updated PageBlobSyncClientBuilder object
     */
    public PageBlobSyncClientBuilder anonymousCredentials() {
        builder.anonymousCredentials();
        return this;
    }

    /**
     * Sets the connection string for the service, parses it for authentication information (account name, account key)
     * @param connectionString connection string from access keys section
     * @return the updated PageBlobSyncClientBuilder object
     */
    public PageBlobSyncClientBuilder connectionString(String connectionString) {
        builder.connectionString(connectionString);
        return this;
    }

    /**
     * Sets the http client used to send service requests
     * @param httpClient http client to send requests
     * @return the updated PageBlobSyncClientBuilder object
     */
    public PageBlobSyncClientBuilder httpClient(HttpClient httpClient) {
        builder.httpClient(httpClient);
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent
     * @param pipelinePolicy a pipeline policy
     * @return the updated PageBlobSyncClientBuilder object
     */
    public PageBlobSyncClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        builder.addPolicy(pipelinePolicy);
        return this;
    }

    /**
     * Sets the logging level for service requests
     * @param logLevel logging level
     * @return the updated PageBlobSyncClientBuilder object
     */
    public PageBlobSyncClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        builder.httpLogDetailLevel(logLevel);
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values used to build the client with
     * when they are not set in the builder, defaults to Configuration.NONE
     * @param configuration configuration store
     * @return the updated PageBlobSyncClientBuilder object
     */
    public PageBlobSyncClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }
}
