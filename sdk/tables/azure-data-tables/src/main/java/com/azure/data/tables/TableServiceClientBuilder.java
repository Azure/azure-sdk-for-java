// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import com.azure.storage.common.implementation.connectionstring.StorageEndpoint;
import com.azure.storage.common.policy.RequestRetryOptions;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of
 * {@link TableServiceClient} and {@link TableServiceAsyncClient} objects. Call {@link #buildClient()} or
 * {@link #buildAsyncClient()}, respectively, to construct an instance of the desired client.
 */
@ServiceClientBuilder(serviceClients = {TableServiceClient.class, TableServiceAsyncClient.class})
public class TableServiceClientBuilder {
    private final ClientLogger logger = new ClientLogger(TableServiceClientBuilder.class);
    private final SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
    private final List<HttpPipelinePolicy> policies;
    private Configuration configuration;
    private String endpoint;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private TablesServiceVersion version;
    private TokenCredential tokenCredential;
    private HttpPipeline httpPipeline;
    private TablesSharedKeyCredential tablesSharedKeyCredential;
    private AzureSasCredential azureSasCredential;
    private String sasToken;
    private RequestRetryOptions retryOptions = new RequestRetryOptions();

    /**
     * Creates a builder instance that is able to configure and construct {@link TableServiceClient} and
     * {@link TableServiceAsyncClient} objects.
     */
    public TableServiceClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();
    }

    /**
     * Creates a {@link TableServiceClient} based on options set in the builder.
     *
     * @return A {@link TableServiceClient} created from the configurations in this builder.
     * @throws IllegalStateException If multiple credentials have been specified.
     */
    public TableServiceClient buildClient() {
        return new TableServiceClient(buildAsyncClient());
    }

    /**
     * Creates a {@link TableServiceAsyncClient} based on options set in the builder.
     *
     * @return A {@link TableServiceAsyncClient} created from the configurations in this builder.
     * @throws IllegalStateException If multiple credentials have been specified.
     */
    public TableServiceAsyncClient buildAsyncClient() {

        TablesServiceVersion serviceVersion = version != null ? version : TablesServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            tablesSharedKeyCredential, tokenCredential, azureSasCredential, sasToken,
            endpoint, retryOptions, httpLogOptions,
            httpClient, policies, configuration, logger);

        return new TableServiceAsyncClient(pipeline, endpoint, serviceVersion, serializerAdapter);
    }

    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the storage or CosmosDB table API account.
     * @return The updated {@code TableServiceClientBuilder}.
     * @throws IllegalArgumentException if {@code connectionString} isn't a valid connection string.
     */
    public TableServiceClientBuilder connectionString(String connectionString) {
        StorageConnectionString storageConnectionString
            = StorageConnectionString.create(connectionString, logger);
        StorageEndpoint endpoint = storageConnectionString.getTableEndpoint();
        if (endpoint == null || endpoint.getPrimaryUri() == null) {
            throw logger
                .logExceptionAsError(new IllegalArgumentException(
                    "connectionString missing required settings to derive tables service endpoint."));
        }
        this.endpoint(endpoint.getPrimaryUri());
        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        if (authSettings.getType() == StorageAuthenticationSettings.Type.ACCOUNT_NAME_KEY) {
            this.credential(new TablesSharedKeyCredential(authSettings.getAccount().getName(),
                authSettings.getAccount().getAccessKey()));
        } else if (authSettings.getType() == StorageAuthenticationSettings.Type.SAS_TOKEN) {
            this.sasToken(authSettings.getSasToken());
        }
        return this;
    }

    /**
     * Sets the service endpoint.
     *
     * @param endpoint The URL of the storage or CosmosDB table API account endpoint.
     * @return The updated {@code TableServiceClientBuilder}.
     * @throws IllegalArgumentException if {@code endpoint} isn't a valid URL.
     */
    public TableServiceClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL"));
        }
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client. If {@code pipeline} is set, all other settings are
     * ignored, aside from {@code endpoint}.
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated {@code TableServiceClientBuilder}.
     */
    public TableServiceClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.httpPipeline != null && pipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = pipeline;
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return The updated {@code TableServiceClientBuilder}.
     */
    public TableServiceClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     * @return The updated {@code TableServiceClientBuilder}.
     * @throws NullPointerException if {@code sasToken} is {@code null}.
     */
    public TableServiceClientBuilder sasToken(String sasToken) {
        this.sasToken = Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null.");
        this.tablesSharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link AzureSasCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureSasCredential} used to authorize requests sent to the service.
     * @return The updated {@code TableServiceClientBuilder}.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public TableServiceClientBuilder credential(AzureSasCredential credential) {
        this.azureSasCredential = Objects.requireNonNull(credential,
            "'sasToken' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link TablesSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link TablesSharedKeyCredential} used to authorize requests sent to the service.
     * @return The updated {@code TableServiceClientBuilder}.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public TableServiceClientBuilder credential(TablesSharedKeyCredential credential) {
        this.tablesSharedKeyCredential = Objects.requireNonNull(credential, "credential cannot be null.");
        this.tokenCredential = null;
        this.sasToken = null;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated {@code TableServiceClientBuilder}.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public TableServiceClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tablesSharedKeyCredential = null;
        this.sasToken = null;
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The {@link HttpClient} to use for requests.
     * @return The updated {@code TableServiceClientBuilder}.
     */
    public TableServiceClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.error("'httpClient' is being set to 'null' when it was previously configured.");
        }
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the logging configuration to use when sending and receiving requests to and from the service.
     *
     * If a {@code logLevel} is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * @param logOptions The logging configuration to use when sending and receiving requests to and from the service.
     * @return The updated {@code TableServiceClientBuilder}.
     * @throws NullPointerException if {@code logOptions} is {@code null}.
     */
    public TableServiceClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent. The policy will be added after the retry policy. If
     * the method is called multiple times, all policies will be added and their order preserved.
     *
     * @param pipelinePolicy A pipeline policy
     * @return The updated {@code TableServiceClientBuilder}.
     * @throws NullPointerException if {@code pipelinePolicy} is {@code null}.
     */
    public TableServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.policies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the {@link TablesServiceVersion} that is used when making API requests.
     *
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     *
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param version The {@link TablesServiceVersion} of the service to be used when making requests.
     * @return The updated {@code TableServiceClientBuilder}.
     */
    public TableServiceClientBuilder serviceVersion(TablesServiceVersion version) {
        this.version = version;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions {@link RequestRetryOptions}.
     * @return The updated {@code TableServiceClientBuilder}.
     * @throws NullPointerException if {@code retryOptions} is {@code null}.
     */
    public TableServiceClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }
}
