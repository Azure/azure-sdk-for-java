// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.tables.implementation.StorageAuthenticationSettings;
import com.azure.data.tables.implementation.StorageConnectionString;
import com.azure.data.tables.implementation.StorageEndpoint;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.azure.data.tables.BuilderHelper.validateCredentials;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of
 * {@link TableServiceClient} and {@link TableServiceAsyncClient} objects. Call {@link #buildClient()} or
 * {@link #buildAsyncClient()}, respectively, to construct an instance of the desired client.
 *
 * <p>The minimal configuration options required by {@link TableServiceClientBuilder} to build a
 * {@link TableServiceClient} or {@link TableServiceAsyncClient} are an {@link String endpoint} and a form of
 * authentication, which can be set via: {@link TableServiceClientBuilder#connectionString(String)},
 * {@link TableServiceClientBuilder#credential(AzureSasCredential)},
 * {@link TableServiceClientBuilder#credential(AzureNamedKeyCredential)} or
 * {@link TableServiceClientBuilder#sasToken(String)}</p>
 *
 * <p><strong>Samples to construct a sync client</strong></p>
 * {@codesnippet com.azure.data.tables.tableServiceClient.instantiation}
 * <p><strong>Samples to construct an async client</strong></p>
 * {@codesnippet com.azure.data.tables.tableServiceAsyncClient.instantiation}
 *
 * @see TableServiceAsyncClient
 * @see TableServiceClient
 */
@ServiceClientBuilder(serviceClients = {TableServiceClient.class, TableServiceAsyncClient.class})
public final class TableServiceClientBuilder {
    private final ClientLogger logger = new ClientLogger(TableServiceClientBuilder.class);
    private final SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();
    private Configuration configuration;
    private String connectionString;
    private String endpoint;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private ClientOptions clientOptions;
    private TableServiceVersion version;
    private HttpPipeline httpPipeline;
    private AzureNamedKeyCredential azureNamedKeyCredential;
    private AzureSasCredential azureSasCredential;
    private TokenCredential tokenCredential;
    private String sasToken;
    private RetryPolicy retryPolicy;

    /**
     * Creates a builder instance that is able to configure and construct {@link TableServiceClient} and
     * {@link TableServiceAsyncClient} objects.
     */
    public TableServiceClientBuilder() {
    }

    /**
     * Creates a {@link TableServiceClient} based on options set in the builder.
     *
     * @return A {@link TableServiceClient} created from the configurations in this builder.
     *
     * @throws NullPointerException If {@code endpoint} is {@code null}.
     * @throws IllegalArgumentException If {@code endpoint} is malformed or empty.
     * @throws IllegalStateException If no form of authentication or {@code endpoint} have been specified or if
     * multiple forms of authentication are provided, with the exception of {@code sasToken} +
     * {@code connectionString}. Also thrown if {@code endpoint} and/or {@code sasToken} are set alongside a
     * {@code connectionString} and the endpoint and/or SAS token in the latter are different than the former,
     * respectively.
     */
    public TableServiceClient buildClient() {
        return new TableServiceClient(buildAsyncClient());
    }

    /**
     * Creates a {@link TableServiceAsyncClient} based on options set in the builder.
     *
     * @return A {@link TableServiceAsyncClient} created from the configurations in this builder.
     *
     * @throws NullPointerException If {@code endpoint} is {@code null}.
     * @throws IllegalArgumentException If {@code endpoint} is malformed or empty.
     * @throws IllegalStateException If no form of authentication or {@code endpoint} have been specified or if
     * multiple forms of authentication are provided, with the exception of {@code sasToken} +
     * {@code connectionString}. Also thrown if {@code endpoint} and/or {@code sasToken} are set alongside a
     * {@code connectionString} and the endpoint and/or SAS token in the latter are different than the former,
     * respectively.
     */
    public TableServiceAsyncClient buildAsyncClient() {
        TableServiceVersion serviceVersion = version != null ? version : TableServiceVersion.getLatest();

        validateCredentials(azureNamedKeyCredential, azureSasCredential, tokenCredential, sasToken, connectionString,
            logger);

        AzureNamedKeyCredential namedKeyCredential = null;

        // If 'connectionString' was provided, extract the endpoint and sasToken.
        if (connectionString != null) {
            StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, logger);
            StorageEndpoint storageConnectionStringTableEndpoint = storageConnectionString.getTableEndpoint();

            if (storageConnectionStringTableEndpoint == null
                || storageConnectionStringTableEndpoint.getPrimaryUri() == null) {

                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "'connectionString' is missing the required settings to derive a Tables endpoint."));
            }

            String connectionStringEndpoint = storageConnectionStringTableEndpoint.getPrimaryUri();

            // If no 'endpoint' was provided, use the one in the 'connectionString'. Else, verify they are the same.
            if (endpoint == null) {
                endpoint = connectionStringEndpoint;
            } else {
                if (endpoint.endsWith("/")) {
                    endpoint = endpoint.substring(0, endpoint.length() - 1);
                }

                if (connectionStringEndpoint.endsWith("/")) {
                    connectionStringEndpoint =
                        connectionStringEndpoint.substring(0, connectionStringEndpoint.length() - 1);
                }

                if (!endpoint.equals(connectionStringEndpoint)) {
                    throw logger.logExceptionAsError(new IllegalStateException(
                        "'endpoint' points to a different tables endpoint than 'connectionString'."));
                }
            }

            StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();

            if (authSettings.getType() == StorageAuthenticationSettings.Type.ACCOUNT_NAME_KEY) {
                namedKeyCredential = (azureNamedKeyCredential != null) ? azureNamedKeyCredential
                    : new AzureNamedKeyCredential(authSettings.getAccount().getName(),
                    authSettings.getAccount().getAccessKey());
            } else if (authSettings.getType() == StorageAuthenticationSettings.Type.SAS_TOKEN) {
                sasToken = (sasToken != null) ? sasToken : authSettings.getSasToken();
            }
        }

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            namedKeyCredential != null ? namedKeyCredential : azureNamedKeyCredential, azureSasCredential,
            tokenCredential, sasToken, endpoint, retryPolicy, httpLogOptions, clientOptions, httpClient,
            perCallPolicies, perRetryPolicies, configuration, logger);

        return new TableServiceAsyncClient(pipeline, endpoint, serviceVersion, serializerAdapter);
    }

    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the storage or CosmosDB table API account.
     *
     * @return The updated {@link TableServiceClientBuilder}.
     *
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     * @throws IllegalArgumentException If {@code connectionString} isn't a valid connection string.
     */
    public TableServiceClientBuilder connectionString(String connectionString) {
        if (connectionString == null) {
            throw logger.logExceptionAsError(new NullPointerException("'connectionString' cannot be null."));
        }

        StorageConnectionString.create(connectionString, logger);

        this.connectionString = connectionString;

        return this;
    }

    /**
     * Sets the service endpoint.
     *
     * @param endpoint The URL of the storage or CosmosDB table API account endpoint.
     *
     * @return The updated {@link TableServiceClientBuilder}.
     *
     * @throws IllegalArgumentException If {@code endpoint} isn't a valid URL.
     */
    public TableServiceClientBuilder endpoint(String endpoint) {
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
     * @return The updated {@link TableServiceClientBuilder}.
     */
    public TableServiceClientBuilder pipeline(HttpPipeline pipeline) {
        this.httpPipeline = pipeline;

        return this;
    }

    /**
     * Sets the {@link Configuration configuration} object used to retrieve environment configuration values during
     * building of the client.
     *
     * <p>The {@link Configuration default configuration store} is a clone of the
     * {@link Configuration#getGlobalConfiguration() global configuration store}, use {@link Configuration#NONE} to
     * bypass using configuration settings during construction.</p>
     *
     * @param configuration {@link Configuration} store used to retrieve environment configuration.
     *
     * @return The updated {@link TableServiceClientBuilder}.
     */
    public TableServiceClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service. Setting this is mutually exclusive with
     * {@link TableServiceClientBuilder#credential(AzureNamedKeyCredential)},
     * {@link TableServiceClientBuilder#credential(AzureSasCredential)} or
     * {@link TableServiceClientBuilder#credential(TokenCredential)}.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     *
     * @return The updated {@link TableServiceClientBuilder}.
     *
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     * @throws IllegalArgumentException If {@code sasToken} is empty.
     */
    public TableServiceClientBuilder sasToken(String sasToken) {
        if (sasToken == null) {
            throw logger.logExceptionAsError(new NullPointerException("'sasToken' cannot be null."));
        }

        if (sasToken.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sasToken' cannot be empty."));
        }

        this.sasToken = sasToken;

        return this;
    }

    /**
     * Sets the {@link AzureSasCredential} used to authorize requests sent to the service. Setting this is mutually
     * exclusive with {@link TableServiceClientBuilder#credential(AzureNamedKeyCredential)},
     * {@link TableServiceClientBuilder#credential(TokenCredential)} or
     * {@link TableServiceClientBuilder#sasToken(String)}.
     *
     * @param credential {@link AzureSasCredential} used to authorize requests sent to the service.
     *
     * @return The updated {@link TableServiceClientBuilder}.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public TableServiceClientBuilder credential(AzureSasCredential credential) {
        if (credential == null) {
            throw logger.logExceptionAsError(new NullPointerException("'credential' cannot be null."));
        }

        this.azureSasCredential = credential;

        return this;
    }

    /**
     * Sets the {@link AzureNamedKeyCredential} used to authorize requests sent to the service. Setting this is mutually
     * exclusive with using {@link TableServiceClientBuilder#credential(AzureSasCredential)},
     * {@link TableServiceClientBuilder#credential(TokenCredential)} or
     * {@link TableServiceClientBuilder#sasToken(String)}.
     *
     * @param credential {@link AzureNamedKeyCredential} used to authorize requests sent to the service.
     *
     * @return The updated {@link TableServiceClientBuilder}.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public TableServiceClientBuilder credential(AzureNamedKeyCredential credential) {
        if (credential == null) {
            throw logger.logExceptionAsError(new NullPointerException("'credential' cannot be null."));
        }

        this.azureNamedKeyCredential = credential;

        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Setting this is mutually
     * exclusive with using {@link TableServiceClientBuilder#credential(AzureNamedKeyCredential)},
     * {@link TableServiceClientBuilder#credential(AzureSasCredential)} or
     * {@link TableServiceClientBuilder#sasToken(String)}.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @return The updated {@link TableServiceClientBuilder}.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public TableServiceClientBuilder credential(TokenCredential credential) {
        if (credential == null) {
            throw logger.logExceptionAsError(new NullPointerException("'credential' cannot be null."));
        }

        this.tokenCredential = credential;

        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The {@link HttpClient} to use for requests.
     *
     * @return The updated {@link TableServiceClientBuilder}.
     */
    public TableServiceClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.warning("'httpClient' is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;

        return this;
    }

    /**
     * Sets the {@link HttpLogOptions logging configuration} to use when sending and receiving requests to and from
     * the service. If a {@code logLevel} is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * @param logOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests to
     * and from the service.
     *
     * @return The updated {@link TableServiceClientBuilder}.
     */
    public TableServiceClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;

        return this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent. The policy will be added
     * after the {@link RetryPolicy retry policy}. If the method is called multiple times, all
     * {@link HttpPipelinePolicy policies} will be added and their order preserved.
     *
     * @param pipelinePolicy A {@link HttpPipelinePolicy pipeline policy}.
     *
     * @return The updated {@link TableServiceClientBuilder}.
     *
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public TableServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
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
     * Sets the {@link TableServiceVersion service version} that is used when making API requests.
     *
     * <p>If a {@link TableServiceVersion service version} is not provided, the
     * {@link TableServiceVersion service version} that will be used will be the latest known
     * {@link TableServiceVersion service version} based on the version of the client library being used. If no
     * {@link TableServiceVersion service version} is specified, updating to a newer version of the client library will
     * have the result of potentially moving to a newer {@link TableServiceVersion service version}.</p>
     *
     * <p>Targeting a specific {@link TableServiceVersion service version} may also mean that the service will return an
     * error for newer APIs.</p>
     *
     * @param serviceVersion The {@link TableServiceVersion} of the service to be used when making requests.
     *
     * @return The updated {@link TableServiceClientBuilder}.
     */
    public TableServiceClientBuilder serviceVersion(TableServiceVersion serviceVersion) {
        this.version = serviceVersion;

        return this;
    }

    /**
     * Sets the request {@link RetryPolicy} for all the requests made through the client. The default
     * {@link RetryPolicy} will be used in the pipeline, if not provided.
     *
     * @param retryPolicy {@link RetryPolicy}.
     *
     * @return The updated {@link TableServiceClientBuilder}.
     */
    public TableServiceClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;

        return this;
    }

    /**
     * Sets the {@link ClientOptions} such as application ID and custom headers to set on a request.
     *
     * @param clientOptions The {@link ClientOptions}.
     *
     * @return The updated {@link TableServiceClientBuilder}.
     */
    public TableServiceClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;

        return this;
    }
}
