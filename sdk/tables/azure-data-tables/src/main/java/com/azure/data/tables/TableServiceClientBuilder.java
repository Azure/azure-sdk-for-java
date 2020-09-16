// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
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
 * builds the table service clients
 */
@ServiceClientBuilder(serviceClients = {TableServiceClient.class, TableServiceAsyncClient.class})
public class TableServiceClientBuilder {
    private final ClientLogger logger = new ClientLogger(TableServiceClientBuilder.class);
    private final SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
    private final List<HttpPipelinePolicy> policies;
    private String connectionString;
    private Configuration configuration;
    private String endpoint;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private TablesServiceVersion version;
    private TokenCredential tokenCredential;
    private HttpPipeline httpPipeline;
    private SasTokenCredential sasTokenCredential;
    private String accountName;
    private RequestRetryOptions retryOptions = new RequestRetryOptions();

    /**
     * constructor
     */
    public TableServiceClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();
    }

    /**
     * builds a sync TableServiceClient
     *
     * @return a sync TableServiceClient
     */
    public TableServiceClient buildClient() {
        return new TableServiceClient(buildAsyncClient());
    }

    /**
     * builds an async TableServiceAsyncClient
     *
     * @return TableServiceAsyncClient an aysnc TableServiceAsyncClient
     */
    public TableServiceAsyncClient buildAsyncClient() {

        TablesServiceVersion serviceVersion = version != null ? version : TablesServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            (TablesSharedKeyCredential) tokenCredential, tokenCredential, sasTokenCredential, endpoint, retryOptions,
            httpLogOptions, httpClient, policies, configuration, logger);

        return new TableServiceAsyncClient(pipeline, endpoint, serviceVersion, serializerAdapter);
    }

    /**
     * Sets the connection string to help build the client
     *
     * @param connectionString the connection string to the storage account
     * @return the TableServiceClientBuilder
     */
    public TableServiceClientBuilder connectionString(String connectionString) {
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
     * Sets the table service endpoint
     *
     * @param endpoint URL of the service
     * @return the updated TableServiceClientBuilder object
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
     * Sets the HTTP pipeline to use for the service client.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated TableServiceClientBuilder object.
     */
    public TableServiceClientBuilder pipeline(HttpPipeline pipeline) {
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
     * @return the updated BlobServiceClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public TableServiceClientBuilder sasToken(String sasToken) {
        this.sasTokenCredential = new SasTokenCredential(Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null."));
        this.tokenCredential = null;
        return this;
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
     *
     * @param credential the tables shared key credential
     * @return the updated TableServiceClient builder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public TableServiceClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
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
            logger.error("'httpClient' is being set to 'null' when it was previously configured.");
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
        this.httpLogOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
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
        this.policies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
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

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions {@link RequestRetryOptions}.
     * @return the updated TableServiceClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public TableServiceClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }
}
