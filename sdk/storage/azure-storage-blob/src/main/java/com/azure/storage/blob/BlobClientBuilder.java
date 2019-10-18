// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.util.BuilderHelper;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.implementation.policy.SasTokenCredentialPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link BlobClient
 * BlobClients} and {@link BlobAsyncClient BlobAsyncClients}, call {@link #buildClient() buildClient} and {@link
 * #buildAsyncClient() buildAsyncClient} respectively to construct an instance of the desired client.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the endpoint through {@code .endpoint()}, including the container name and blob name, in the format of
 * {@code https://{accountName}.blob.core.windows.net/{containerName}/{blobName}}.
 * <li>the credential through {@code .credential()} or {@code .connectionString()} if the container is not publicly
 * accessible.
 * </ul>
 */
@ServiceClientBuilder(serviceClients = {BlobClient.class, BlobAsyncClient.class})
public final class BlobClientBuilder {
    private final ClientLogger logger = new ClientLogger(BlobClientBuilder.class);

    private String endpoint;
    private String accountName;
    private String containerName;
    private String blobName;
    private String snapshot;

    private CpkInfo customerProvidedKey;
    private StorageSharedKeyCredential storageSharedKeyCredential;
    private TokenCredential tokenCredential;
    private SasTokenCredential sasTokenCredential;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
    private HttpLogOptions logOptions = new HttpLogOptions();
    private RequestRetryOptions retryOptions = new RequestRetryOptions();
    private HttpPipeline httpPipeline;

    private Configuration configuration;
    private BlobServiceVersion version;

    /**
     * Creates a builder instance that is able to configure and construct {@link BlobClient BlobClients} and {@link
     * BlobAsyncClient BlobAsyncClients}.
     */
    public BlobClientBuilder() {
    }

    /**
     * Creates a {@link BlobClient} based on options set in the builder. BlobClients are used to perform generic blob
     * methods such as {@link BlobClient#download(OutputStream) download} and {@link BlobClient#getProperties() get
     * properties}, use this when the blob type is unknown.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobClientBuilder.buildClient}
     *
     * @return a {@link BlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint} or {@code blobName} is {@code null}.
     */
    public BlobClient buildClient() {
        return new BlobClient(buildAsyncClient());
    }

    /**
     * Creates a {@link BlobAsyncClient} based on options set in the builder. BlobAsyncClients are used to perform
     * generic blob methods such as {@link BlobAsyncClient#download() download} and {@link
     * BlobAsyncClient#getProperties()}, use this when the blob type is unknown.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobClientBuilder.buildAsyncClient}
     *
     * @return a {@link BlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint} or {@code blobName} is {@code null}.
     */
    public BlobAsyncClient buildAsyncClient() {
        Objects.requireNonNull(blobName, "'blobName' cannot be null.");
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");

        /*
        Implicit and explicit root container access are functionally equivalent, but explicit references are easier
        to read and debug.
         */
        String blobContainerName = ImplUtils.isNullOrEmpty(containerName)
            ? BlobContainerAsyncClient.ROOT_CONTAINER_NAME
            : containerName;

        BlobServiceVersion serviceVersion = version != null ? version : BlobServiceVersion.getLatest();

        HttpPipeline pipeline = (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(() -> {
            if (storageSharedKeyCredential != null) {
                return new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential);
            } else if (tokenCredential != null) {
                return new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", endpoint));
            } else if (sasTokenCredential != null) {
                return new SasTokenCredentialPolicy(sasTokenCredential);
            } else {
                return null;
            }
        }, retryOptions, logOptions, httpClient, additionalPolicies, configuration, serviceVersion);

        return new BlobAsyncClient(pipeline, String.format("%s/%s/%s", endpoint, blobContainerName, blobName),
            serviceVersion, accountName, blobContainerName, blobName, snapshot, customerProvidedKey);
    }

    /**
     * Sets the {@link CustomerProvidedKey customer provided key} that is used to encrypt blob contents on the server.
     *
     * @param customerProvidedKey Customer provided key containing the encryption key information.
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder customerProvidedKey(CustomerProvidedKey customerProvidedKey) {
        if (customerProvidedKey == null) {
            this.customerProvidedKey = null;
        } else {
            this.customerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySHA256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }

        return this;
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated BlobClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public BlobClientBuilder credential(StorageSharedKeyCredential credential) {
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated BlobClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public BlobClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     * @return the updated BlobClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public BlobClientBuilder sasToken(String sasToken) {
        this.sasTokenCredential = new SasTokenCredential(Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null."));
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Clears the credential used to authorize the request.
     *
     * <p>This is for blobs that are publicly accessible.</p>
     *
     * @return the updated BlobClientBuilder
     */
    public BlobClientBuilder setAnonymousAccess() {
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Constructs a {@link StorageSharedKeyCredential} used to authorize requests sent to the service. Additionally,
     * if the connection string contains `DefaultEndpointsProtocol` and `EndpointSuffix` it will set the {@link
     * #endpoint(String) endpoint}.
     *
     * @param connectionString Connection string of the storage account.
     * @return the updated BlobClientBuilder
     * @throws IllegalArgumentException If {@code connectionString} doesn't contain `AccountName` or `AccountKey`.
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     */
    public BlobClientBuilder connectionString(String connectionString) {
        BuilderHelper.configureConnectionString(connectionString, (accountName) -> this.accountName = accountName,
            this::credential, this::endpoint, logger);

        return this;
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name, blob name)
     *
     * <p>If the endpoint is to a blob in the root container, this method will fail as it will interpret the blob name
     * as the container name. With only one path element, it is impossible to distinguish between a container name and
     * a blob in the root container, so it is assumed to be the container name as this is much more common. When working
     * with blobs in the root container, it is best to set the endpoint to the account url and specify the blob name
     * separately using the {@link BlobClientBuilder#blobName(String) blobName} method.</p>
     *
     * @param endpoint URL of the service
     * @return the updated BlobClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public BlobClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            BlobUrlParts parts = BlobUrlParts.parse(url);

            this.accountName = parts.getAccountName();
            this.endpoint = parts.getScheme() + "://" + parts.getHost();
            this.containerName = parts.getBlobContainerName();
            this.blobName = parts.getBlobName();
            this.snapshot = parts.getSnapshot();

            String sasToken = parts.getSasQueryParameters().encode();
            if (!ImplUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage Blob endpoint url is malformed."));
        }
        return this;
    }

    /**
     * Sets the name of the container that contains the blob.
     *
     * @param containerName Name of the container. If the value {@code null} or empty the root container, {@code $root}
     * , will be used.
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder containerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Sets the name of the blob.
     *
     * @param blobName Name of the blob.
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code blobName} is {@code null}
     */
    public BlobClientBuilder blobName(String blobName) {
        this.blobName = Objects.requireNonNull(blobName, "'blobName' cannot be null.");
        return this;
    }

    /**
     * Sets the snapshot identifier of the blob.
     *
     * @param snapshot Snapshot identifier for the blob.
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder snapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder httpClient(HttpClient httpClient) {
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
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public BlobClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public BlobClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions The options used to configure retry behavior.
     * @return the updated BlobClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public BlobClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the {@link BlobServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link BlobServiceVersion} of the service to be used when making requests.
     * @return the updated BlobClientBuilder object
     */
    public BlobClientBuilder serviceVersion(BlobServiceVersion version) {
        this.version = version;
        return this;
    }
}
