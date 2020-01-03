// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.file.datalake.implementation.util.BuilderHelper;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * DataLakeFileSystemClient FileSystemClients} and {@link DataLakeFileSystemAsyncClient FileSystemAsyncClients}, call
 * {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an
 * instance of the desired client.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the endpoint through {@code .endpoint()}, including the file system name, in the format of {@code
 * https://{accountName}.dfs.core.windows.net/{fileSystemName}}.
 * <li>the credential through {@code .credential()} or {@code .connectionString()} if the file system is not publicly
 * accessible.
 * </ul>
 */
@ServiceClientBuilder(serviceClients = {DataLakeFileSystemClient.class, DataLakeFileSystemAsyncClient.class})
public class DataLakeFileSystemClientBuilder {
    private final ClientLogger logger = new ClientLogger(DataLakeFileSystemClientBuilder.class);

    private final BlobContainerClientBuilder blobContainerClientBuilder;

    private String endpoint;
    private String accountName;
    private String fileSystemName;

    private StorageSharedKeyCredential storageSharedKeyCredential;
    private TokenCredential tokenCredential;
    private SasTokenCredential sasTokenCredential;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
    private HttpLogOptions logOptions;
    private RequestRetryOptions retryOptions = new RequestRetryOptions();
    private HttpPipeline httpPipeline;

    private Configuration configuration;
    private DataLakeServiceVersion version;

    /**
     * Creates a builder instance that is able to configure and construct {@link DataLakeFileSystemClient
     * FileSystemClients}
     * and {@link DataLakeFileSystemAsyncClient FileSystemAsyncClients}.
     */
    public DataLakeFileSystemClientBuilder() {
        logOptions = getDefaultHttpLogOptions();
        blobContainerClientBuilder = new BlobContainerClientBuilder();
    }

    /**
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder.buildClient}
     *
     * @return a {@link DataLakeFileSystemClient} created from the configurations in this builder.
     */
    public DataLakeFileSystemClient buildClient() {
        return new DataLakeFileSystemClient(buildAsyncClient(), blobContainerClientBuilder.buildClient());
    }

    /**
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder.buildAsyncClient}
     *
     * @return a {@link DataLakeFileSystemAsyncClient} created from the configurations in this builder.
     */
    public DataLakeFileSystemAsyncClient buildAsyncClient() {
        /*
        Implicit and explicit root file system access are functionally equivalent, but explicit references are easier
        to read and debug.
         */
        String dataLakeFileSystemName = CoreUtils.isNullOrEmpty(fileSystemName)
            ? DataLakeFileSystemAsyncClient.ROOT_FILESYSTEM_NAME
            : fileSystemName;

        DataLakeServiceVersion serviceVersion = version != null ? version : DataLakeServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            storageSharedKeyCredential, tokenCredential, sasTokenCredential, endpoint, retryOptions, logOptions,
            httpClient, additionalPolicies, configuration, logger);

        return new DataLakeFileSystemAsyncClient(pipeline, String.format("%s/%s", endpoint, dataLakeFileSystemName),
            serviceVersion, accountName, dataLakeFileSystemName, blobContainerClientBuilder.buildAsyncClient());
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, file system name)
     *
     * @param endpoint URL of the service
     * @return the updated DataLakeFileSystemClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public DataLakeFileSystemClientBuilder endpoint(String endpoint) {
        // Ensure endpoint provided is dfs endpoint
        endpoint = DataLakeImplUtils.endpointToDesiredEndpoint(endpoint, "dfs", "blob");
        blobContainerClientBuilder.endpoint(DataLakeImplUtils.endpointToDesiredEndpoint(endpoint, "blob", "dfs"));
        try {
            URL url = new URL(endpoint);
            BlobUrlParts parts = BlobUrlParts.parse(url);

            this.accountName = parts.getAccountName();
            this.fileSystemName = parts.getBlobContainerName();
            this.endpoint = BuilderHelper.getEndpoint(parts);

            String sasToken = parts.getCommonSasQueryParameters().encode();
            if (!CoreUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage Datalake endpoint url is malformed."));
        }

        return this;
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated DataLakeFileSystemClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public DataLakeFileSystemClientBuilder credential(StorageSharedKeyCredential credential) {
        blobContainerClientBuilder.credential(credential);
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated DataLakeFileSystemClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public DataLakeFileSystemClientBuilder credential(TokenCredential credential) {
        blobContainerClientBuilder.credential(credential);
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     * @return the updated DataLakeFileSystemClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public DataLakeFileSystemClientBuilder sasToken(String sasToken) {
        blobContainerClientBuilder.sasToken(sasToken);
        this.sasTokenCredential = new SasTokenCredential(Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null."));
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Clears the credential used to authorize the request.
     *
     * <p>This is for file systems that are publicly accessible.</p>
     *
     * @return the updated DataLakeFileSystemClientBuilder
     */
    public DataLakeFileSystemClientBuilder setAnonymousAccess() {
        blobContainerClientBuilder.setAnonymousAccess();
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the name of the file system.
     *
     * @param fileSystemName Name of the file system. If the value {@code null} or empty the root file system,
     * {@code $root}, will be used.
     * @return the updated DataLakeFileSystemClientBuilder object
     */
    public DataLakeFileSystemClientBuilder fileSystemName(String fileSystemName) {
        blobContainerClientBuilder.containerName(fileSystemName);
        this.fileSystemName = fileSystemName;
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated DataLakeFileSystemClientBuilder object
     */
    public DataLakeFileSystemClientBuilder httpClient(HttpClient httpClient) {
        blobContainerClientBuilder.httpClient(httpClient);
        if (this.httpClient != null && httpClient == null) {
            logger.info("'httpClient' is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Gets the default Storage whitelist log headers and query parameters.
     *
     * @return the default http log options.
     */
    public static HttpLogOptions getDefaultHttpLogOptions() {
        return BuilderHelper.getDefaultHttpLogOptions();
    }

    /**
     * Adds a pipeline policy to apply on each request sent. The policy will be added after the retry policy. If
     * the method is called multiple times, all policies will be added and their order preserved.
     *
     * @param pipelinePolicy a pipeline policy
     * @return the updated DataLakeFileSystemClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public DataLakeFileSystemClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        blobContainerClientBuilder.addPolicy(pipelinePolicy);
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated DataLakeFileSystemClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public DataLakeFileSystemClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        blobContainerClientBuilder.httpLogOptions(logOptions);
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated DataLakeFileSystemClientBuilder object
     */
    public DataLakeFileSystemClientBuilder configuration(Configuration configuration) {
        blobContainerClientBuilder.configuration(configuration);
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions The options used to configure retry behavior.
     * @return the updated DataLakeFileSystemClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public DataLakeFileSystemClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        blobContainerClientBuilder.retryOptions(retryOptions);
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated DataLakeFileSystemClientBuilder object
     */
    public DataLakeFileSystemClientBuilder pipeline(HttpPipeline httpPipeline) {
        blobContainerClientBuilder.pipeline(httpPipeline);
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    // TODO (gapra) : Determine how to set the blob service version here
    /**
     * Sets the {@link DataLakeServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link DataLakeServiceVersion} of the service to be used when making requests.
     * @return the updated DataLakeFileSystemClientBuilder object
     */
    public DataLakeFileSystemClientBuilder serviceVersion(DataLakeServiceVersion version) {
        this.version = version;
        return this;
    }
}
