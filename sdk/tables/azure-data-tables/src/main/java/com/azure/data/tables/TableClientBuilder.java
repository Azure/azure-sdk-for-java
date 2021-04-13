// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.tables.implementation.TablesJacksonSerializer;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import com.azure.storage.common.implementation.connectionstring.StorageEndpoint;
import com.azure.storage.common.policy.RequestRetryOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link TableClient} and
 * {@link TableAsyncClient} objects. Call {@link #buildClient()} or {@link #buildAsyncClient()}, respectively, to
 * construct an instance of the desired client.
 */
@ServiceClientBuilder(serviceClients = {TableClient.class, TableAsyncClient.class})
public class TableClientBuilder {
    private static final SerializerAdapter TABLES_SERIALIZER = new TablesJacksonSerializer();

    private final ClientLogger logger = new ClientLogger(TableClientBuilder.class);
    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();
    private String tableName;
    private Configuration configuration;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private String endpoint;
    private HttpLogOptions httpLogOptions;
    private ClientOptions clientOptions;
    private HttpPipeline httpPipeline;
    private TablesSharedKeyCredential tablesSharedKeyCredential;
    private AzureSasCredential azureSasCredential;
    private String sasToken;
    private TablesServiceVersion version;
    private RequestRetryOptions retryOptions = new RequestRetryOptions();

    /**
     * Creates a builder instance that is able to configure and construct {@link TableClient} and
     * {@link TableAsyncClient} objects.
     */
    public TableClientBuilder() {
    }

    /**
     * Creates a {@link TableClient} based on options set in the builder.
     *
     * @return A {@link TableClient} created from the configurations in this builder.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     * @throws IllegalStateException If multiple credentials have been specified.
     */
    public TableClient buildClient() {
        return new TableClient(buildAsyncClient());
    }

    /**
     * Creates a {@link TableAsyncClient} based on options set in the builder.
     *
     * @return A {@link TableAsyncClient} created from the configurations in this builder.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     * @throws IllegalStateException If multiple credentials have been specified.
     */
    public TableAsyncClient buildAsyncClient() {
        TablesServiceVersion serviceVersion = version != null ? version : TablesServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            tablesSharedKeyCredential, tokenCredential, azureSasCredential, sasToken, endpoint, retryOptions,
            httpLogOptions, clientOptions, httpClient, perCallPolicies, perRetryPolicies, configuration, logger);

        return new TableAsyncClient(tableName, pipeline, endpoint, serviceVersion, TABLES_SERIALIZER);
    }

    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the storage or CosmosDB table API account.
     *
     * @return The updated {@link TableClientBuilder}.
     *
     * @throws IllegalArgumentException If {@code connectionString} isn't a valid connection string.
     */
    public TableClientBuilder connectionString(String connectionString) {
        if (connectionString == null) {
            throw logger.logExceptionAsError(new NullPointerException("'connectionString' cannot be null."));
        }

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, logger);
        StorageEndpoint endpoint = storageConnectionString.getTableEndpoint();

        if (endpoint == null || endpoint.getPrimaryUri() == null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(
                    "'connectionString' missing required settings to derive tables service endpoint."));
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
     *
     * @return The updated {@link TableClientBuilder}.
     *
     * @throws IllegalArgumentException If {@code endpoint} isn't a valid URL.
     */
    public TableClientBuilder endpoint(String endpoint) {
        if (endpoint == null) {
            throw logger.logExceptionAsError(new NullPointerException("'endpoint' cannot be null."));
        }

        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL."));
        }

        this.endpoint = endpoint;

        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client. If {@code pipeline} is set, all other settings are
     * ignored, aside from {@code endpoint}.
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     *
     * @return The updated {@link TableClientBuilder}.
     */
    public TableClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.httpPipeline != null && pipeline == null) {
            logger.warning("'pipeline' is being set to 'null' when it was previously configured.");
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
     *
     * @return The updated {@link TableClientBuilder}.
     */
    public TableClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     *
     * @return The updated {@link TableClientBuilder}.
     *
     * @throws IllegalArgumentException If {@code sasToken} is empty.
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public TableClientBuilder sasToken(String sasToken) {
        if (sasToken == null) {
            throw logger.logExceptionAsError(new NullPointerException("'sasToken' cannot be null."));
        }

        if (sasToken.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sasToken' cannot be null or empty."));
        }

        this.sasToken = sasToken;
        this.tablesSharedKeyCredential = null;
        this.tokenCredential = null;

        return this;
    }

    /**
     * Sets the {@link AzureSasCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureSasCredential} used to authorize requests sent to the service.
     *
     * @return The updated {@link TableClientBuilder}.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public TableClientBuilder credential(AzureSasCredential credential) {
        if (credential == null) {
            throw logger.logExceptionAsError(new NullPointerException("'credential' cannot be null."));
        }

        this.azureSasCredential = credential;

        return this;
    }

    /**
     * Sets the {@link TablesSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link TablesSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @return The updated {@link TableClientBuilder}.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public TableClientBuilder credential(TablesSharedKeyCredential credential) {
        if (credential == null) {
            throw logger.logExceptionAsError(new NullPointerException("'credential' cannot be null."));
        }

        this.tablesSharedKeyCredential = credential;
        this.tokenCredential = null;
        this.sasToken = null;

        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @return The updated {@link TableClientBuilder}.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public TableClientBuilder credential(TokenCredential credential) {
        if (credential == null) {
            throw logger.logExceptionAsError(new NullPointerException("'credential' cannot be null."));
        }

        this.tokenCredential = credential;
        this.tablesSharedKeyCredential = null;
        this.sasToken = null;

        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The {@link HttpClient} to use for requests.
     *
     * @return The updated {@link TableClientBuilder}.
     */
    public TableClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.warning("'httpClient' is being set to 'null' when it was previously configured.");
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
     *
     * @return The updated {@link TableClientBuilder}.
     *
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public TableClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        if (logOptions == null) {
            throw logger.logExceptionAsError(new NullPointerException("'logOptions' cannot be null."));
        }

        this.httpLogOptions = logOptions;

        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent. The policy will be added after the retry policy. If the
     * method is called multiple times, all policies will be added and their order preserved.
     *
     * @param pipelinePolicy A pipeline policy
     *
     * @return The updated {@link TableClientBuilder}.
     *
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public TableClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        if (pipelinePolicy == null) {
            throw logger.logExceptionAsError(new NullPointerException("'pipelinePolicy' cannot be null."));
        }

        if (pipelinePolicy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(pipelinePolicy);
        } else {
            perRetryPolicies.add(pipelinePolicy);
        }

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
     *
     * @return The updated {@link TableClientBuilder}.
     */
    public TableClientBuilder serviceVersion(TablesServiceVersion version) {
        this.version = version;

        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions {@link RequestRetryOptions}.
     *
     * @return The updated {@link TableClientBuilder}.
     *
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public TableClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        if (retryOptions == null) {
            throw logger.logExceptionAsError(new NullPointerException("'retryOptions' cannot be null."));
        }

        this.retryOptions = retryOptions;

        return this;
    }

    /**
     * Sets the client options such as application ID and custom headers to set on a request.
     *
     * @param clientOptions The {@link ClientOptions}.
     *
     * @return The updated {@link TableClientBuilder}.
     */
    public TableClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;

        return this;
    }

    /**
     * Sets the name of the table.
     *
     * @param tableName Name of the table.
     *
     * @return The updated {@link TableClientBuilder}.
     *
     * @throws IllegalArgumentException If {@code tableName} is {@code null} or empty.
     */
    public TableClientBuilder tableName(String tableName) {
        if (tableName == null) {
            throw logger.logExceptionAsError(new NullPointerException("'tableName' cannot be null."));
        }

        if (tableName.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'tableName' cannot be null or empty."));
        }

        this.tableName = tableName;

        return this;
    }
}
