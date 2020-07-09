// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import java.util.ArrayList;
import java.util.List;
import com.azure.core.util.Configuration;
import java.util.Objects;

/**
 * builds the table service clients
 */
@ServiceClientBuilder(serviceClients = {TableServiceClient.class, TableServiceAsyncClient.class})
public class TableServiceClientBuilder {
    private final ClientLogger logger = new ClientLogger(TableServiceClientBuilder.class);
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
    private String connectionString;
    private Configuration configuration;
    private TablesSharedKeyCredential tablesSharedKeyCredential;
    private String endpoint;
    private HttpClient httpClient;
    private HttpLogOptions logOptions;
    private TablesServiceVersion version;

    /**
     * Sets the connection string to help build the client
     *
     * @param connectionString the connection string to the storage account
     * @return the TableServiceClientBuilder
     */
    public TableServiceClientBuilder connectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    /**
     * builds a sync TableServiceClient
     *
     * @return a sync TableServiceClient
     */
    public TableServiceClient buildClient() {
        return new TableServiceClient();
    }

    /**
     * builds an async TableServiceAsyncClient
     *
     * @return TableServiceAsyncClient an aysnc TableServiceAsyncClient
     */
    public TableServiceAsyncClient buildAsyncClient() {
        return new TableServiceAsyncClient();
    }

    /**
     * constructor
     */
    public TableServiceClientBuilder() {

    }

    /**
     * gets the connection string
     * @return the connection string
     */
    public String getConnectionString() {
        return this.connectionString;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated TableServiceClientBuilder object
     */
    public TableServiceClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * update credential
     * @param credential the tables shared key credential
     * @return the updated TableServiceClient builder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public TableServiceClientBuilder credential(TablesSharedKeyCredential credential) {
        this.tablesSharedKeyCredential = Objects.requireNonNull(tablesSharedKeyCredential, "credential cannot" +
            "be null");
        return this;
    }

    /**
     * Sets the table service endpoint
     *
     * @param endpoint URL of the service
     * @return the updated TableServiceClientBuilder object
     */
    public TableServiceClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated TableServiceClientBuilder object
     */
    public TableServiceClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.info("'httpClient' is being set to 'null' when it was previously configured.");
        }
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated TableServiceClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public TableServiceClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent. The policy will be added after the retry policy. If
     * the method is called multiple times, all policies will be added and their order preserved.
     *
     * @param pipelinePolicy a pipeline policy
     * @return the updated TableServiceClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public TableServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the TablesServiceVersion that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param version {@link TablesServiceVersion} of the service to be used when making requests.
     * @return the updated TableServiceClientBuilder object
     */
    public TableServiceClientBuilder serviceVersion(TablesServiceVersion version) {
        this.version = version;
        return this;
    }

}
