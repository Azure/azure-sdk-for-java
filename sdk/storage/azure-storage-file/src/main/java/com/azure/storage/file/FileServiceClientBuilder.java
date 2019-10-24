// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import com.azure.storage.common.implementation.connectionstring.StorageEndpoint;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.implementation.policy.SasTokenCredentialPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;
import com.azure.storage.file.implementation.AzureFileStorageBuilder;
import com.azure.storage.file.implementation.AzureFileStorageImpl;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * FileServiceClient FileServiceClients} and {@link FileServiceAsyncClient FileServiceAsyncClients}, calling {@link
 * FileServiceClientBuilder#buildClient() buildClient} constructs an instance of FileServiceClient and calling {@link
 * FileServiceClientBuilder#buildAsyncClient() buildFileAsyncClient} constructs an instance of FileServiceAsyncClient.
 *
 * <p>The client needs the endpoint of the Azure Storage File service and authorization credential.
 * {@link FileServiceClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give the builder a
 * SAS token that authorizes the client.</p>
 *
 * <p><strong>Instantiating a synchronous FileService Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.fileServiceClient.instantiation.sastoken}
 *
 * <p><strong>Instantiating an Asynchronous FileService Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.instantiation.sastoken}
 *
 * <p>If the {@code endpoint} doesn't contain the query parameters to construct a SAS token they may be set using
 * {@link #sasToken(String) sasToken} .</p>
 *
 * {@codesnippet com.azure.storage.file.fileServiceClient.instantiation.credential}
 *
 * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.instantiation.credential}
 *
 * <p>Another way to authenticate the client is using a {@link StorageSharedKeyCredential}. To create a
 * StorageSharedKeyCredential a connection string from the Storage File service must be used. Set the
 * StorageSharedKeyCredential with {@link FileServiceClientBuilder#connectionString(String) connectionString}.
 * If the builder has both a SAS token and StorageSharedKeyCredential the StorageSharedKeyCredential will be preferred
 * when authorizing requests sent to the service.</p>
 *
 * <p><strong>Instantiating a synchronous FileService Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.file.fileServiceClient.instantiation.connectionstring}
 *
 * <p><strong>Instantiating an Asynchronous FileService Client with connection string. </strong></p>
 * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.instantiation.connectionstring}
 *
 * @see FileServiceClient
 * @see FileServiceAsyncClient
 * @see StorageSharedKeyCredential
 */
@ServiceClientBuilder(serviceClients = {FileServiceClient.class, FileServiceAsyncClient.class})
public final class FileServiceClientBuilder {
    private final ClientLogger logger = new ClientLogger(FileServiceClientBuilder.class);

    private String endpoint;
    private String accountName;

    private StorageSharedKeyCredential storageSharedKeyCredential;
    private SasTokenCredential sasTokenCredential;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
    private HttpLogOptions logOptions = new FileLogOptions();
    private RequestRetryOptions retryOptions = new RequestRetryOptions();
    private HttpPipeline httpPipeline;

    private Configuration configuration;
    private FileServiceVersion version;

    /**
     * Creates a builder instance that is able to configure and construct {@link FileServiceClient FileServiceClients}
     * and {@link FileServiceAsyncClient FileServiceAsyncClients}.
     */
    public FileServiceClientBuilder() {
    }

    /**
     * Creates a {@link FileServiceAsyncClient} based on options set in the builder. Every time this method is called a
     * new instance of {@link FileServiceAsyncClient} is created.
     *
     * <p>
     * If {@link FileServiceClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * FileServiceClientBuilder#endpoint(String) endpoint} are used to create the {@link FileServiceAsyncClient client}.
     * All other builder settings are ignored.
     * </p>
     *
     * @return A FileServiceAsyncClient with the options set from the builder.
     * @throws IllegalArgumentException If neither a {@link StorageSharedKeyCredential} or
     * {@link #sasToken(String) SAS token} has been set.
     */
    public FileServiceAsyncClient buildAsyncClient() {
        FileServiceVersion serviceVersion = version != null ? version : FileServiceVersion.getLatest();
        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(() -> {
            if (storageSharedKeyCredential != null) {
                return new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential);
            } else if (sasTokenCredential != null) {
                return new SasTokenCredentialPolicy(sasTokenCredential);
            } else {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Credentials are required for authorization"));
            }
        }, retryOptions, logOptions, httpClient, additionalPolicies, configuration, serviceVersion);

        AzureFileStorageImpl azureFileStorage = new AzureFileStorageBuilder()
            .url(endpoint)
            .pipeline(pipeline)
            .version(serviceVersion.getVersion())
            .build();

        return new FileServiceAsyncClient(azureFileStorage, accountName, serviceVersion);
    }

    /**
     * Creates a {@link FileServiceClient} based on options set in the builder. Every time {@code buildClient()} is
     * called a new instance of {@link FileServiceClient} is created.
     *
     * <p>
     * If {@link FileServiceClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * FileServiceClientBuilder#endpoint(String) endpoint} are used to create the {@link FileServiceClient client}. All
     * other builder settings are ignored.
     * </p>
     *
     * @return A FileServiceClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} is {@code null}.
     * @throws IllegalArgumentException If neither a {@link StorageSharedKeyCredential}
     * or {@link #sasToken(String) SAS token} has been set.
     */
    public FileServiceClient buildClient() {
        return new FileServiceClient(buildAsyncClient());
    }

    /**
     * Sets the endpoint for the Azure Storage File instance that the client will interact with.
     *
     * <p>Query parameters of the endpoint will be parsed in an attempt to generate a SAS token to authenticate
     * requests sent to the service.</p>
     *
     * @param endpoint The URL of the Azure Storage File instance to send service requests to and receive responses
     * from.
     * @return the updated FileServiceClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} isn't a proper URL
     */
    public FileServiceClientBuilder endpoint(String endpoint) {
        try {
            URL fullUrl = new URL(endpoint);
            this.endpoint = fullUrl.getProtocol() + "://" + fullUrl.getHost();
            this.accountName = BuilderHelper.getAccountName(fullUrl);

            // Attempt to get the SAS token from the URL passed
            String sasToken = new FileServiceSasQueryParameters(
                StorageImplUtils.parseQueryStringSplitValues(fullUrl.getQuery()), false).encode();
            if (!ImplUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage File Service endpoint url is malformed."));
        }

        return this;
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated FileServiceClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public FileServiceClientBuilder credential(StorageSharedKeyCredential credential) {
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     * @return the updated FileServiceClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public FileServiceClientBuilder sasToken(String sasToken) {
        this.sasTokenCredential = new SasTokenCredential(Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null."));
        this.storageSharedKeyCredential = null;
        return this;
    }

    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the storage account.
     * @return the updated FileServiceClientBuilder
     * @throws IllegalArgumentException If {@code connectionString} is invalid.
     */
    public FileServiceClientBuilder connectionString(String connectionString) {
        StorageConnectionString storageConnectionString
                = StorageConnectionString.create(connectionString, logger);
        StorageEndpoint endpoint = storageConnectionString.getFileEndpoint();
        if (endpoint == null || endpoint.getPrimaryUri() == null) {
            throw logger
                    .logExceptionAsError(new IllegalArgumentException(
                            "connectionString missing required settings to derive file service endpoint."));
        }
        this.endpoint(endpoint.getPrimaryUri());
        if (storageConnectionString.getAccountName() != null) {
            this.accountName = storageConnectionString.getAccountName();
        }
        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        if (authSettings.getType() == StorageAuthenticationSettings.Type.ACCOUNT_NAME_KEY) {
            this.credential(new StorageSharedKeyCredential(authSettings.getAccount().getName(),
                    authSettings.getAccount().getAccessKey()));
        } else if (authSettings.getType() == StorageAuthenticationSettings.Type.SAS_TOKEN) {
            this.sasToken(authSettings.getSasToken());
        }
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated FileServiceClientBuilder object
     */
    public FileServiceClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.info("'httpClient' is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent.
     *
     * @param pipelinePolicy a pipeline policy
     * @return the updated FileServiceClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public FileServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the {@link FileLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated FileServiceClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public FileServiceClientBuilder fileLogOptions(FileLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated FileServiceClientBuilder object
     */
    public FileServiceClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions The options used to configure retry behavior.
     * @return the updated FileServiceClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public FileServiceClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated FileServiceClientBuilder object
     */
    public FileServiceClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the {@link FileServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link FileServiceVersion} of the service to be used when making requests.
     * @return the updated FileServiceClientBuilder object
     */
    public FileServiceClientBuilder serviceVersion(FileServiceVersion version) {
        this.version = version;
        return this;
    }
}
