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
import com.azure.storage.blob.BlobServiceClientBuilder;
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
 * This class provides a fluent builder API to help aid the configuration and instantiation of
 * {@link DataLakeServiceClient DataLakeServiceClients} and {@link DataLakeServiceAsyncClient
 * DataLakeServiceAsyncClients}, call {@link #buildClient() buildClient} and {@link #buildAsyncClient()
 * buildAsyncClient} respectively to construct an instance of the desired client.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the endpoint through {@code .endpoint()}, in the format of {@code https://{accountName}.dfs.core.windows.net}.
 * <li>the credential through {@code .credential()} or {@code .connectionString()}.
 * </ul>
 */
@ServiceClientBuilder(serviceClients = {DataLakeServiceClient.class, DataLakeServiceAsyncClient.class})
public class DataLakeServiceClientBuilder {
    private final ClientLogger logger = new ClientLogger(DataLakeServiceClientBuilder.class);

    private final BlobServiceClientBuilder blobServiceClientBuilder;

    private String endpoint;
    private String accountName;

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
     * Creates a builder instance that is able to configure and construct {@link DataLakeServiceClient
     * DataLakeServiceClients} and {@link DataLakeServiceAsyncClient DataLakeServiceAsyncClients}.
     */
    public DataLakeServiceClientBuilder() {
        logOptions = getDefaultHttpLogOptions();
        blobServiceClientBuilder = new BlobServiceClientBuilder();
    }

    /**
     * @return a {@link DataLakeServiceClient} created from the configurations in this builder.
     */
    public DataLakeServiceClient buildClient() {
        return new DataLakeServiceClient(buildAsyncClient(), blobServiceClientBuilder.buildClient());
    }

    /**
     * @return a {@link DataLakeServiceAsyncClient} created from the configurations in this builder.
     */
    public DataLakeServiceAsyncClient buildAsyncClient() {
        if (Objects.isNull(storageSharedKeyCredential) && Objects.isNull(tokenCredential)
            && Objects.isNull(sasTokenCredential)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Data Lake Service Client cannot be accessed "
                + "anonymously. Please provide a form of authentication"));
        }
        DataLakeServiceVersion serviceVersion = version != null ? version : DataLakeServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            storageSharedKeyCredential, tokenCredential, sasTokenCredential, endpoint, retryOptions, logOptions,
            httpClient, additionalPolicies, configuration, logger);

        return new DataLakeServiceAsyncClient(pipeline, endpoint, serviceVersion, accountName,
            blobServiceClientBuilder.buildAsyncClient());
    }

    /**
     * Sets the data lake service endpoint, additionally parses it for information (SAS token)
     *
     * @param endpoint URL of the service
     * @return the updated DataLakeServiceClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public DataLakeServiceClientBuilder endpoint(String endpoint) {
        // Ensure endpoint provided is dfs endpoint
        endpoint = DataLakeImplUtils.endpointToDesiredEndpoint(endpoint, "dfs", "blob");
        blobServiceClientBuilder.endpoint(DataLakeImplUtils.endpointToDesiredEndpoint(endpoint, "blob", "dfs"));
        try {
            BlobUrlParts parts = BlobUrlParts.parse(new URL(endpoint));

            this.accountName = parts.getAccountName();
            this.endpoint = BuilderHelper.getEndpoint(parts);

            String sasToken = parts.getCommonSasQueryParameters().encode();
            if (!CoreUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage endpoint url is malformed."));
        }

        return this;
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated DataLakeServiceClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public DataLakeServiceClientBuilder credential(StorageSharedKeyCredential credential) {
        blobServiceClientBuilder.credential(credential);
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated DataLakeServiceClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public DataLakeServiceClientBuilder credential(TokenCredential credential) {
        blobServiceClientBuilder.credential(credential);
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     * @return the updated DataLakeServiceClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public DataLakeServiceClientBuilder sasToken(String sasToken) {
        blobServiceClientBuilder.sasToken(sasToken);
        this.sasTokenCredential = new SasTokenCredential(Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null."));
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated DataLakeServiceClientBuilder object
     */
    public DataLakeServiceClientBuilder httpClient(HttpClient httpClient) {
        blobServiceClientBuilder.httpClient(httpClient);
        if (this.httpClient != null && httpClient == null) {
            logger.info("'httpClient' is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent. The policy will be added after the retry policy. If
     * the method is called multiple times, all policies will be added and their order preserved.
     *
     * @param pipelinePolicy a pipeline policy
     * @return the updated DataLakeServiceClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public DataLakeServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        blobServiceClientBuilder.addPolicy(pipelinePolicy);
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated DataLakeServiceClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public DataLakeServiceClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        blobServiceClientBuilder.httpLogOptions(logOptions);
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
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
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated DataLakeServiceClientBuilder object
     */
    public DataLakeServiceClientBuilder configuration(Configuration configuration) {
        blobServiceClientBuilder.configuration(configuration);
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions The options used to configure retry behavior.
     * @return the updated DataLakeServiceClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public DataLakeServiceClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        blobServiceClientBuilder.retryOptions(retryOptions);
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated DataLakeServiceClientBuilder object
     */
    public DataLakeServiceClientBuilder pipeline(HttpPipeline httpPipeline) {
        blobServiceClientBuilder.pipeline(httpPipeline);
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    // TODO (gapra) : Determine how to set the service version of blobs too
    /**
     * Sets the {@link DataLakeServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link DataLakeServiceVersion} of the service to be used when making requests.
     * @return the updated DataLakeServiceClientBuilder object
     */
    public DataLakeServiceClientBuilder serviceVersion(DataLakeServiceVersion version) {
        this.version = version;
        return this;
    }

}
