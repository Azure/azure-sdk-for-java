// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.implementation.util.BuilderHelper;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.common.Utility;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import com.azure.storage.common.implementation.connectionstring.StorageEndpoint;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of specialized Storage Blob
 * clients, {@link AppendBlobClient}, {@link AppendBlobAsyncClient}, {@link BlockBlobClient},
 * {@link BlockBlobAsyncClient}, {@link PageBlobClient}, and {@link PageBlobAsyncClient}. These clients are used to
 * perform operations that are specific to the blob type.
 *
 * @see AppendBlobClient
 * @see AppendBlobAsyncClient
 * @see BlockBlobClient
 * @see BlockBlobAsyncClient
 * @see PageBlobClient
 * @see PageBlobAsyncClient
 */
@ServiceClientBuilder(serviceClients = {
    AppendBlobClient.class, AppendBlobAsyncClient.class,
    BlockBlobClient.class, BlockBlobAsyncClient.class,
    PageBlobClient.class, PageBlobAsyncClient.class
})
public final class SpecializedBlobClientBuilder {
    private final ClientLogger logger = new ClientLogger(SpecializedBlobClientBuilder.class);

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
    private HttpLogOptions logOptions = getDefaultHttpLogOptions();;
    private RequestRetryOptions retryOptions = new RequestRetryOptions();
    private HttpPipeline httpPipeline;

    private Configuration configuration;
    private BlobServiceVersion version;

    /**
     * Creates a {@link AppendBlobClient} based on options set in the Builder. AppendBlobClients are used to perform
     * append blob specific operations such as {@link AppendBlobClient#appendBlock(InputStream, long) append block},
     * only use this when the blob is known to be an append blob.
     *
     * @return a {@link AppendBlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public AppendBlobClient buildAppendBlobClient() {
        return new AppendBlobClient(buildAppendBlobAsyncClient());
    }

    /**
     * Creates a {@link AppendBlobAsyncClient} based on options set in the Builder. AppendBlobAsyncClients are used to
     * perform append blob specific operations such as {@link AppendBlobAsyncClient#appendBlock(Flux, long) append
     * blob}, only use this when the blob is known to be an append blob.
     *
     * @return a {@link AppendBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public AppendBlobAsyncClient buildAppendBlobAsyncClient() {
        validateConstruction();
        String containerName = getContainerName();

        return new AppendBlobAsyncClient(getHttpPipeline(), getUrl(containerName), getServiceVersion(),
            accountName, containerName, blobName, snapshot, customerProvidedKey);
    }

    /**
     * Creates a {@link BlockBlobClient} based on options set in the Builder. BlockBlobClients are used to perform
     * generic upload operations such as {@link BlockBlobClient#upload(InputStream, long) upload from file} and block
     * blob specific operations such as {@link BlockBlobClient#stageBlock(String, InputStream, long) stage block} and
     * {@link BlockBlobClient#commitBlockList(List)}, only use this when the blob is known to be a block blob.
     *
     * @return a {@link BlockBlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlockBlobClient buildBlockBlobClient() {
        return new BlockBlobClient(buildBlockBlobAsyncClient());
    }

    /**
     * Creates a {@link BlockBlobAsyncClient} based on options set in the Builder. BlockBlobAsyncClients are used to
     * perform generic upload operations such as {@link BlockBlobAsyncClient#upload(Flux, long) upload from file}
     * and block blob specific operations such as {@link BlockBlobAsyncClient#stageBlockWithResponse(String, Flux, long,
     * byte[], String) stage block} and {@link BlockBlobAsyncClient#commitBlockList(List) commit block list}, only use
     * this when the blob is known to be a block blob.
     *
     * @return a {@link BlockBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public BlockBlobAsyncClient buildBlockBlobAsyncClient() {
        validateConstruction();
        String containerName = getContainerName();

        return new BlockBlobAsyncClient(getHttpPipeline(), getUrl(containerName), getServiceVersion(),
            accountName, containerName, blobName, snapshot, customerProvidedKey);
    }

    /**
     * Creates a {@link PageBlobClient} based on options set in the Builder. PageBlobClients are used to perform page
     * blob specific operations such as {@link PageBlobClient#uploadPages(PageRange, InputStream) upload pages} and
     * {@link PageBlobClient#clearPages(PageRange) clear pages}, only use this when the blob is known to be a page
     * blob.
     *
     * @return a {@link PageBlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public PageBlobClient buildPageBlobClient() {
        return new PageBlobClient(buildPageBlobAsyncClient());
    }

    /**
     * Creates a {@link PageBlobAsyncClient} based on options set in the Builder. PageBlobAsyncClients are used to
     * perform page blob specific operations such as {@link PageBlobAsyncClient#uploadPages(PageRange, Flux) upload
     * pages} and {@link PageBlobAsyncClient#clearPages(PageRange) clear pages}, only use this when the blob is known to
     * be a page blob.
     *
     * @return a {@link PageBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public PageBlobAsyncClient buildPageBlobAsyncClient() {
        validateConstruction();
        String containerName = getContainerName();

        return new PageBlobAsyncClient(getHttpPipeline(), getUrl(containerName), getServiceVersion(),
            accountName, containerName, blobName, snapshot, customerProvidedKey);
    }

    /*
     * Validate that the builder is able to construct a client.
     */
    private void validateConstruction() {
        Objects.requireNonNull(blobName, "'blobName' cannot be null.");
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");

        BuilderHelper.httpsValidation(customerProvidedKey, "customer provided key", endpoint, logger);
    }

    /*
     * Gets the container name to use in this client, if no container name has been passed the root container will be
     * used.
     *
     * Implicit and explicit root container access are functionally equivalent, but explicit references are easier to
     * read and debug.
     */
    private String getContainerName() {
        return CoreUtils.isNullOrEmpty(containerName) ? BlobContainerAsyncClient.ROOT_CONTAINER_NAME : containerName;
    }

    private HttpPipeline getHttpPipeline() {
        return (httpPipeline != null) ? httpPipeline : BuilderHelper.buildPipeline(
            storageSharedKeyCredential, tokenCredential, sasTokenCredential, endpoint, retryOptions, logOptions,
            httpClient, additionalPolicies, configuration, logger);
    }

    private BlobServiceVersion getServiceVersion() {
        return (version != null) ? version : BlobServiceVersion.getLatest();
    }

    private String getUrl(String containerName) {
        return String.format("%s/%s/%s", endpoint, containerName, blobName);
    }

    /**
     * Configures the builder based on the {@link BlobClientBase}.
     *
     * @param blobClient The {@code BlobClientBase} used to configure this builder.
     * @return the updated SpecializedBlobClientBuilder object.
     */
    public SpecializedBlobClientBuilder blobClient(BlobClientBase blobClient) {
        pipeline(blobClient.getHttpPipeline());
        endpoint(blobClient.getBlobUrl());
        serviceVersion(blobClient.getServiceVersion());
        this.snapshot = blobClient.getSnapshotId();
        this.customerProvidedKey = blobClient.getCustomerProvidedKey();
        return this;
    }

    /**
     * Configures the builder based on the {@link BlobAsyncClientBase}.
     *
     * @param blobAsyncClient The {@code BlobAsyncClientBase} used to configure this builder.
     * @return the updated SpecializedBlobClientBuilder object.
     */
    public SpecializedBlobClientBuilder blobAsyncClient(BlobAsyncClientBase blobAsyncClient) {
        pipeline(blobAsyncClient.getHttpPipeline());
        endpoint(blobAsyncClient.getBlobUrl());
        serviceVersion(blobAsyncClient.getServiceVersion());
        this.snapshot = blobAsyncClient.getSnapshotId();
        this.customerProvidedKey = blobAsyncClient.getCustomerProvidedKey();
        return this;
    }

    /**
     * Configures the builder based on the {@link BlobContainerClient} and appends the blob name to the container's URL.
     *
     * @param blobContainerClient The {@code ContainerClient} used to configure this builder.
     * @param blobName Name of the blob.
     * @return the updated SpecializedBlobClientBuilder object.
     */
    public SpecializedBlobClientBuilder containerClient(BlobContainerClient blobContainerClient, String blobName) {
        pipeline(blobContainerClient.getHttpPipeline());
        endpoint(blobContainerClient.getBlobContainerUrl());
        serviceVersion(blobContainerClient.getServiceVersion());
        blobName(blobName);
        this.customerProvidedKey = blobContainerClient.getCustomerProvidedKey();
        return this;
    }

    /**
     * Configures the builder based on the {@link BlobContainerAsyncClient} and appends the blob name to the container's
     * URL.
     *
     * @param blobContainerAsyncClient The {@code ContainerAsyncClient} used to configure this builder.
     * @param blobName Name of the blob.
     * @return the updated SpecializedBlobClientBuilder object.
     */
    public SpecializedBlobClientBuilder containerAsyncClient(BlobContainerAsyncClient blobContainerAsyncClient,
        String blobName) {
        pipeline(blobContainerAsyncClient.getHttpPipeline());
        endpoint(blobContainerAsyncClient.getBlobContainerUrl());
        serviceVersion(blobContainerAsyncClient.getServiceVersion());
        blobName(blobName);
        this.customerProvidedKey = blobContainerAsyncClient.getCustomerProvidedKey();
        return this;
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name, blob name)
     *
     * @param endpoint URL of the service
     * @return the updated SpecializedBlobClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public SpecializedBlobClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            BlobUrlParts parts = BlobUrlParts.parse(url);

            this.accountName = parts.getAccountName();
            this.endpoint = BuilderHelper.getEndpoint(parts);
            this.containerName = parts.getBlobContainerName();
            this.blobName = Utility.urlEncode(parts.getBlobName());
            this.snapshot = parts.getSnapshot();

            String sasToken = parts.getCommonSasQueryParameters().encode();
            if (!CoreUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage Blob endpoint url is malformed."));
        }
        return this;
    }

    /**
     * Sets the {@link CustomerProvidedKey customer provided key} that is used to encrypt blob contents on the server.
     *
     * @param customerProvidedKey Customer provided key containing the encryption key information.
     * @return the updated SpecializedBlobClientBuilder object
     */
    public SpecializedBlobClientBuilder customerProvidedKey(CustomerProvidedKey customerProvidedKey) {
        if (customerProvidedKey == null) {
            this.customerProvidedKey = null;
        } else {
            this.customerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }

        return this;
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated SpecializedBlobClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public SpecializedBlobClientBuilder credential(StorageSharedKeyCredential credential) {
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential The credential to use for authenticating request.
     * @return the updated SpecializedBlobClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public SpecializedBlobClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests.
     * @return the updated SpecializedBlobClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public SpecializedBlobClientBuilder sasToken(String sasToken) {
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
     * @return the updated SpecializedBlobClientBuilder
     */
    public SpecializedBlobClientBuilder setAnonymousAccess() {
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        this.sasTokenCredential = null;
        return this;
    }

    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the storage account.
     * @return the updated SpecializedBlobClientBuilder
     * @throws IllegalArgumentException If {@code connectionString} in invalid.
     */
    public SpecializedBlobClientBuilder connectionString(String connectionString) {
        StorageConnectionString storageConnectionString
                = StorageConnectionString.create(connectionString, logger);
        StorageEndpoint endpoint = storageConnectionString.getBlobEndpoint();
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
            this.credential(new StorageSharedKeyCredential(authSettings.getAccount().getName(),
                    authSettings.getAccount().getAccessKey()));
        } else if (authSettings.getType() == StorageAuthenticationSettings.Type.SAS_TOKEN) {
            this.sasToken(authSettings.getSasToken());
        }
        return this;
    }

    /**
     * Sets the name of the container that contains the blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobClientBase.Builder.containerName#String}
     *
     * @param containerName Name of the container. If the value {@code null} or empty the root container, {@code $root},
     * will be used.
     * @return the updated SpecializedBlobClientBuilder object
     */
    public SpecializedBlobClientBuilder containerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Sets the name of the blob.
     *
     * @param blobName Name of the blob.
     * @return the updated SpecializedBlobClientBuilder object
     * @throws NullPointerException If {@code blobName} is {@code null}
     */
    public SpecializedBlobClientBuilder blobName(String blobName) {
        this.blobName = Utility.urlEncode(Utility.urlDecode(Objects.requireNonNull(blobName,
            "'blobName' cannot be null.")));
        return this;
    }

    /**
     * Sets the snapshot identifier of the blob.
     *
     * @param snapshot Snapshot identifier for the blob.
     * @return the updated SpecializedBlobClientBuilder object
     */
    public SpecializedBlobClientBuilder snapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated SpecializedBlobClientBuilder object
     */
    public SpecializedBlobClientBuilder httpClient(HttpClient httpClient) {
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
     * @return the updated SpecializedBlobClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public SpecializedBlobClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated SpecializedBlobClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public SpecializedBlobClientBuilder httpLogOptions(HttpLogOptions logOptions) {
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
     * @return the updated SpecializedBlobClientBuilder object
     */
    public SpecializedBlobClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * @param retryOptions The options used to configure retry behavior.
     * @return the updated SpecializedBlobClientBuilder object
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public SpecializedBlobClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated SpecializedBlobClientBuilder object
     */
    public SpecializedBlobClientBuilder pipeline(HttpPipeline httpPipeline) {
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
     * @return the updated SpecializedBlobClientBuilder object
     */
    public SpecializedBlobClientBuilder serviceVersion(BlobServiceVersion version) {
        this.version = version;
        return this;
    }
}
