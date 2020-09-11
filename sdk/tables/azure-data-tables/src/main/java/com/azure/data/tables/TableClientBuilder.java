// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.tables.implementation.TablesJacksonSerializer;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import com.azure.storage.common.implementation.connectionstring.StorageEndpoint;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * builds table client
 */
@ServiceClientBuilder(serviceClients = {TableClient.class, TableAsyncClient.class})
public class TableClientBuilder {
    private final ClientLogger logger = new ClientLogger(TableClientBuilder.class);
    private final SerializerAdapter serializerAdapter = new TablesJacksonSerializer();

    private String tableName;
    private final List<HttpPipelinePolicy> policies;
    private Configuration configuration;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private String endpoint;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline httpPipeline;
    private SasTokenCredential sasTokenCredential;
    private TablesServiceVersion version;
    private String accountName;
    private RequestRetryOptions retryOptions = new RequestRetryOptions();


    TableClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();
    }

    /**
     * Sets the connection string to help build the client
     *
     * @param connectionString the connection string to the storage account
     * @return the TableClientBuilder
     */
    public TableClientBuilder connectionString(String connectionString) {
        StorageConnectionString storageConnectionString
            = StorageConnectionString.create(connectionString, logger);
        StorageEndpoint endpoint = storageConnectionString.getTableEndpoint();
        if (endpoint == null || endpoint.getPrimaryUri() == null) {
            throw logger
                .logExceptionAsError(new IllegalArgumentException(
                    "connectionString missing required settings to derive blob service endpoint."));
        }
        this.endpoint(endpoint.getPrimaryUri());
        if (storageConnectionString.getAccountName() != null) {
            this.accountName = storageConnectionString.getAccountName();
        }
        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        if (authSettings.getType() == StorageAuthenticationSettings.Type.ACCOUNT_NAME_KEY) {
            this.credential((TokenCredential) new TablesSharedKeyCredential(authSettings.getAccount().getName(),
                authSettings.getAccount().getAccessKey()));
        } else if (authSettings.getType() == StorageAuthenticationSettings.Type.SAS_TOKEN) {
            this.sasToken(authSettings.getSasToken());
        }
        return this;
    }

    /**
     * Sets the table name to help build the client
     *
     * @param tableName name of the table for which the client is created for
     * @return the TableClientBuilder
     */
    public TableClientBuilder tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * builds a sync tableClient
     *
     * @return a sync tableClient
     */
    public TableClient buildClient() {
        return new TableClient(buildAsyncClient());
    }

    /**
     * builds an async tableClient
     *
     * @return an aysnc tableClient
     */
    public TableAsyncClient buildAsyncClient() {
        TablesServiceVersion serviceVersion = version != null ? version : TablesServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            (TablesSharedKeyCredential) tokenCredential, tokenCredential, sasTokenCredential, endpoint, retryOptions,
            httpLogOptions, httpClient, policies, configuration, logger);

        return new TableAsyncClient(tableName, pipeline, endpoint, serviceVersion, serializerAdapter);
    }

    /**
     * Sets the endpoint for the Azure Storage Table instance that the client will interact with.
     *
     * @param endpoint The URL of the Azure Storage Table instance to send service requests to and receive responses
     * from.
     * @return the updated TableClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} isn't a proper URL
     */
    public TableClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL"));
        }
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated TableClientBuilder object.
     */
    public TableClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.httpPipeline != null && pipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }
        this.httpPipeline = pipeline;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     * @return the updated BlobClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public TableClientBuilder sasToken(String sasToken) {
        this.sasTokenCredential = new SasTokenCredential(Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null."));
        this.tokenCredential = null;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated TableClientBuilder object.
     */
    public TableClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param credential TokenCredential used to authenticate HTTP requests.
     * @return The updated TableClientBuilder object.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public TableClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "credential cannot be null.");
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return The updated TableClientBuilder object.
     */
    public TableClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            logger.error("'httpClient' is being set to 'null' when it was previously configured.");
        }
        this.httpClient = client;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return The updated TableClientBuilder object.
     */
    public TableClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param pipelinePolicy The retry policy for service requests.
     * @return The updated TableClientBuilder object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public TableClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.policies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the {@link TablesServiceVersion} that is used when making API requests.
     *
     * @param version {@link TablesServiceVersion} of the service to be used when making requests.
     * @return The updated TableClientBuilder object.
     */
    public TableClientBuilder serviceVersion(TablesServiceVersion version) {
        this.version = version;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions {@link RequestRetryOptions}.
     * @return the updated TableServiceClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public TableClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }
}
