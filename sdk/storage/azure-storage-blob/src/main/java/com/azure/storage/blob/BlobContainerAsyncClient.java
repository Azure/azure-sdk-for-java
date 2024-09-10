// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.AzureBlobStorageImplBuilder;
import com.azure.storage.blob.implementation.accesshelpers.BlobItemConstructorProxy;
import com.azure.storage.blob.implementation.models.BlobHierarchyListSegment;
import com.azure.storage.blob.implementation.models.ContainersGetAccountInfoHeaders;
import com.azure.storage.blob.implementation.models.ContainersGetPropertiesHeaders;
import com.azure.storage.blob.implementation.models.ContainersListBlobFlatSegmentHeaders;
import com.azure.storage.blob.implementation.models.ContainersListBlobHierarchySegmentHeaders;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.implementation.models.ListBlobsFlatSegmentResponse;
import com.azure.storage.blob.implementation.models.ListBlobsHierarchySegmentResponse;
import com.azure.storage.blob.implementation.util.BlobSasImplUtil;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.BlobContainerEncryptionScope;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.ListBlobsIncludeItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.TaggedBlobItem;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.options.BlobContainerCreateOptions;
import com.azure.storage.blob.options.FindBlobsOptions;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Client to a container. It may only be instantiated through a {@link BlobContainerClientBuilder} or via the method
 * {@link BlobServiceAsyncClient#getBlobContainerAsyncClient(String)}. This class does not hold any state about a
 * particular blob but is instead a convenient way of sending off appropriate requests to the resource on the service.
 * It may also be used to construct URLs to blobs.
 *
 * <p>
 * This client contains operations on a container. Operations on a blob are available on {@link BlobAsyncClient} through
 * {@link #getBlobAsyncClient(String)}, and operations on the service are available on {@link BlobServiceAsyncClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/azure/storage/blobs/storage-blobs-introduction>Azure
 * Docs</a> for more information on containers.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core project
 * (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong> start the actual network
 * operation, until {@code .subscribe()} is called on the reactive response. You can simply convert one of these
 * responses to a {@link java.util.concurrent.CompletableFuture} object through {@link Mono#toFuture()}.
 */
@ServiceClient(builder = BlobContainerClientBuilder.class, isAsync = true)
public final class BlobContainerAsyncClient {
    /**
     * Special container name for the root container in the Storage account.
     */
    public static final String ROOT_CONTAINER_NAME = "$root";

    /**
     * Special container name for the static website container in the Storage account.
     */
    public static final String STATIC_WEBSITE_CONTAINER_NAME = "$web";

    /**
     * Special container name for the logs container in the Storage account.
     */
    public static final String LOG_CONTAINER_NAME = "$logs";

    private static final ClientLogger LOGGER = new ClientLogger(BlobContainerAsyncClient.class);
    private final AzureBlobStorageImpl azureBlobStorage;

    private final String accountName;
    private final String containerName;
    private final BlobServiceVersion serviceVersion;
    private final CpkInfo customerProvidedKey; // only used to pass down to blob clients
    private final EncryptionScope encryptionScope; // only used to pass down to blob clients
    private final BlobContainerEncryptionScope blobContainerEncryptionScope;

    /**
     * Package-private constructor for use by {@link BlobContainerClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param containerName The container name.
     * @param customerProvidedKey Customer provided key used during encryption of the blob's data on the server, pass
     * {@code null} to allow the service to use its own encryption.
     * @param encryptionScope Encryption scope used during encryption of the blob's data on the server, pass
     * {@code null} to allow the service to use its own encryption.
     */
    BlobContainerAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion,
        String accountName, String containerName, CpkInfo customerProvidedKey, EncryptionScope encryptionScope,
        BlobContainerEncryptionScope blobContainerEncryptionScope) {
        this.azureBlobStorage = new AzureBlobStorageImplBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.serviceVersion = serviceVersion;

        this.accountName = accountName;
        this.containerName = containerName;
        this.customerProvidedKey = customerProvidedKey;
        this.encryptionScope = encryptionScope;
        this.blobContainerEncryptionScope = blobContainerEncryptionScope;
        /* Check to make sure the uri is valid. We don't want the error to occur later in the generated layer
           when the sas token has already been applied. */
        try {
            URI.create(getBlobContainerUrl());
        } catch (IllegalArgumentException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    /**
     * Creates a new BlobAsyncClient object by concatenating blobName to the end of ContainerAsyncClient's URL. The new
     * BlobAsyncClient uses the same request policy pipeline as the ContainerAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.getBlobAsyncClient#String -->
     * <pre>
     * BlobAsyncClient blobAsyncClient = client.getBlobAsyncClient&#40;blobName&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.getBlobAsyncClient#String -->
     *
     * @param blobName A {@code String} representing the name of the blob. If the blob name contains special characters,
     * pass in the url encoded version of the blob name.
     * @return A new {@link BlobAsyncClient} object which references the blob with the specified name in this container.
     */
    public BlobAsyncClient getBlobAsyncClient(String blobName) {
        return getBlobAsyncClient(blobName, null);
    }

    /**
     * Creates a new BlobAsyncClient object by concatenating blobName to the end of ContainerAsyncClient's URL. The new
     * BlobAsyncClient uses the same request policy pipeline as the ContainerAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.getBlobAsyncClient#String-String -->
     * <pre>
     * BlobAsyncClient blobAsyncClient = client.getBlobAsyncClient&#40;blobName, snapshot&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.getBlobAsyncClient#String-String -->
     *
     * @param blobName A {@code String} representing the name of the blob. If the blob name contains special characters,
     * pass in the url encoded version of the blob name.
     * @param snapshot the snapshot identifier for the blob.
     * @return A new {@link BlobAsyncClient} object which references the blob with the specified name in this container.
     */
    public BlobAsyncClient getBlobAsyncClient(String blobName, String snapshot) {
        return new BlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getBlobContainerName(), blobName, snapshot, getCustomerProvidedKey(), encryptionScope);
    }

    /**
     * Creates a new BlobAsyncClient object by concatenating blobName to the end of ContainerAsyncClient's URL. The new
     * BlobAsyncClient uses the same request policy pipeline as the ContainerAsyncClient.
     *
     * @param blobName A {@code String} representing the name of the blob. If the blob name contains special characters,
     * pass in the url encoded version of the blob name.
     * @param versionId the version identifier for the blob, pass {@code null} to interact with the latest blob version.
     * @return A new {@link BlobAsyncClient} object which references the blob with the specified name in this container.
     */
    public BlobAsyncClient getBlobVersionAsyncClient(String blobName, String versionId) {
        return new BlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getBlobContainerName(), blobName, null, getCustomerProvidedKey(), encryptionScope, versionId);
    }

    /**
     * Get the url of the storage account.
     *
     * @return the URL of the storage account
     */
    public String getAccountUrl() {
        return azureBlobStorage.getUrl();
    }

    /**
     * Gets the URL of the container represented by this client.
     *
     * @return the URL.
     */
    public String getBlobContainerUrl() {
        return azureBlobStorage.getUrl() + "/" + containerName;
    }

    /**
     * Get the container name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.getBlobContainerName -->
     * <pre>
     * String containerName = client.getBlobContainerName&#40;&#41;;
     * System.out.println&#40;&quot;The name of the blob is &quot; + containerName&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.getBlobContainerName -->
     *
     * @return The name of container.
     */
    public String getBlobContainerName() {
        return containerName;
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Get an async client pointing to the account.
     *
     * @return {@link BlobServiceAsyncClient}
     */
    public BlobServiceAsyncClient getServiceAsyncClient() {
        return getServiceClientBuilder().buildAsyncClient();
    }

    BlobServiceClientBuilder getServiceClientBuilder() {
        CustomerProvidedKey encryptionKey = this.customerProvidedKey == null ? null
            : new CustomerProvidedKey(this.customerProvidedKey.getEncryptionKey());
        return new BlobServiceClientBuilder()
            .endpoint(this.getBlobContainerUrl())
            .pipeline(this.getHttpPipeline())
            .serviceVersion(this.serviceVersion)
            .blobContainerEncryptionScope(this.blobContainerEncryptionScope)
            .encryptionScope(this.getEncryptionScope())
            .customerProvidedKey(encryptionKey);
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public BlobServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return azureBlobStorage.getHttpPipeline();
    }

    /**
     * Gets the {@link CpkInfo} associated with this client that will be passed to {@link BlobAsyncClient
     * BlobAsyncClients} when {@link #getBlobAsyncClient(String) getBlobAsyncClient} is called.
     *
     * @return the customer provided key used for encryption.
     */
    public CpkInfo getCustomerProvidedKey() {
        return customerProvidedKey;
    }

    /**
     * Gets the {@link EncryptionScope} used to encrypt this blob's content on the server.
     *
     * @return the encryption scope used for encryption.
     */
    public String getEncryptionScope() {
        if (encryptionScope == null) {
            return null;
        }
        return encryptionScope.getEncryptionScope();
    }

    /**
     * Gets the {@link EncryptionScope} used to encrypt this blob's content on the server.
     *
     * @return the encryption scope used for encryption.
     */
    BlobContainerEncryptionScope getBlobContainerEncryptionScope() {
        if (blobContainerEncryptionScope == null) {
            return null;
        }
        return blobContainerEncryptionScope;
    }


    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.exists -->
     * <pre>
     * client.exists&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Exists? %b%n&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.exists -->
     *
     * @return true if the container exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> exists() {
        return existsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.existsWithResponse -->
     * <pre>
     * client.existsWithResponse&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Exists? %b%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.existsWithResponse -->
     *
     * @return true if the container exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> existsWithResponse() {
        try {
            return withContext(this::existsWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * @return true if the container exists, false if it doesn't
     */
    Mono<Response<Boolean>> existsWithResponse(Context context) {
        return this.getPropertiesWithResponse(null, context)
            .map(cp -> (Response<Boolean>) new SimpleResponse<>(cp, true))
            .onErrorResume(t -> t instanceof BlobStorageException && ((BlobStorageException) t).getStatusCode() == 404,
                t -> {
                    HttpResponse response = ((BlobStorageException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), false));
                });
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.create -->
     * <pre>
     * client.create&#40;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Create completed%n&quot;&#41;,
     *     error -&gt; System.out.printf&#40;&quot;Error while creating container %s%n&quot;, error&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.create -->
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> create() {
        return createWithResponse(null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.createWithResponse#Map-PublicAccessType -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * client.createWithResponse&#40;metadata, PublicAccessType.CONTAINER&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.createWithResponse#Map-PublicAccessType -->
     *
     * @param metadata Metadata to associate with the container. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createWithResponse(Map<String, String> metadata, PublicAccessType accessType) {
        try {
            return withContext(context -> createWithResponse(metadata, accessType, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> createWithResponse(Map<String, String> metadata, PublicAccessType accessType,
        Context context) {
        context = context == null ? Context.NONE : context;
        return this.azureBlobStorage.getContainers().createNoCustomHeadersWithResponseAsync(containerName, null,
            metadata, accessType, null, blobContainerEncryptionScope, context);
    }

    /**
     * Creates a new container within a storage account if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.createIfNotExists -->
     * <pre>
     * client.createIfNotExists&#40;&#41;.subscribe&#40;created -&gt; &#123;
     *     if &#40;created&#41; &#123;
     *         System.out.println&#40;&quot;successfully created.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.createIfNotExists -->
     *
     * @return A reactive response signaling completion. {@code true} indicates a new container was created,
     * {@code true} indicates a container already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> createIfNotExists() {
        return createIfNotExistsWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new container within a storage account if it does not exist.  For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.createIfNotExistsWithResponse#Map-PublicAccessType -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * BlobContainerCreateOptions options = new BlobContainerCreateOptions&#40;&#41;.setMetadata&#40;metadata&#41;
     *     .setPublicAccessType&#40;PublicAccessType.CONTAINER&#41;;
     *
     * client.createIfNotExistsWithResponse&#40;options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *         System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully created.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.createIfNotExistsWithResponse#Map-PublicAccessType -->
     *
     * @param options {@link BlobContainerCreateOptions}
     * @return A reactive response signaling completion. If {@link Response}'s status code is 201, a new container was
     * successfully created. If status code is 409, a container already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> createIfNotExistsWithResponse(BlobContainerCreateOptions options) {
        try {
            return createIfNotExistsWithResponse(options, null);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> createIfNotExistsWithResponse(BlobContainerCreateOptions options, Context context) {
        try {
            options = options == null ? new BlobContainerCreateOptions() : options;
            return createWithResponse(options.getMetadata(), options.getPublicAccessType(), context)
                .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
                .onErrorResume(t -> t instanceof BlobStorageException && ((BlobStorageException) t)
                    .getStatusCode() == 409,
                    t -> {
                        HttpResponse response = ((BlobStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), false));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.delete -->
     * <pre>
     * client.delete&#40;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Delete completed%n&quot;&#41;,
     *     error -&gt; System.out.printf&#40;&quot;Delete failed: %s%n&quot;, error&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.delete -->
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete() {
        return deleteWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.deleteWithResponse#BlobRequestConditions -->
     * <pre>
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * client.deleteWithResponse&#40;requestConditions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.deleteWithResponse#BlobRequestConditions -->
     *
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response signalling completion.
     * @throws UnsupportedOperationException If either {@link BlobRequestConditions#getIfMatch()} or
     * {@link BlobRequestConditions#getIfNoneMatch()} is set.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse(BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> deleteWithResponse(requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteWithResponse(BlobRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;

        if (!ModelHelper.validateNoETag(requestConditions)) {
            // Throwing is preferred to Mono.error because this will error out immediately instead of waiting until
            // subscription.
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("ETag access conditions are not supported for this API."));
        }
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.getContainers().deleteNoCustomHeadersWithResponseAsync(containerName, null,
            requestConditions.getLeaseId(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), null, context);
    }

    /**
     * Marks the specified container for deletion if it exists. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.deleteIfExists -->
     * <pre>
     * client.deleteIfExists&#40;&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.deleteIfExists -->
     *
     * @return A reactive response signaling completion. {@code true} indicates the container was deleted,
     * {@code false} indicates the container does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteIfExists() {
        return deleteIfExistsWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Marks the specified container for deletion if it exists. The container and any blobs contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.deleteIfExistsWithResponse#BlobRequestConditions -->
     * <pre>
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * client.deleteIfExistsWithResponse&#40;requestConditions&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.deleteIfExistsWithResponse#BlobRequestConditions -->
     *
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response signaling completion. If {@link Response}'s status code is 202, the container was
     * successfully deleted. If status code is 404, the container does not exist.
     * @throws UnsupportedOperationException If either {@link BlobRequestConditions#getIfMatch()} or
     * {@link BlobRequestConditions#getIfNoneMatch()} is set.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteIfExistsWithResponse(BlobRequestConditions requestConditions) {
        try {
            return deleteIfExistsWithResponse(requestConditions, null);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> deleteIfExistsWithResponse(BlobRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        try {
            return deleteWithResponse(requestConditions, context)
                .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
                .onErrorResume(t -> t instanceof BlobStorageException && ((BlobStorageException) t).getStatusCode() == 404,
                    t -> {
                        HttpResponse response = ((BlobStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), false));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.getProperties -->
     * <pre>
     * client.getProperties&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Public Access Type: %s, Legal Hold? %b, Immutable? %b%n&quot;,
     *         response.getBlobPublicAccess&#40;&#41;,
     *         response.hasLegalHold&#40;&#41;,
     *         response.hasImmutabilityPolicy&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.getProperties -->
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} containing the
     * container properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlobContainerProperties> getProperties() {
        return getPropertiesWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.getPropertiesWithResponse#String -->
     * <pre>
     * client.getPropertiesWithResponse&#40;leaseId&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Public Access Type: %s, Legal Hold? %b, Immutable? %b%n&quot;,
     *         response.getValue&#40;&#41;.getBlobPublicAccess&#40;&#41;,
     *         response.getValue&#40;&#41;.hasLegalHold&#40;&#41;,
     *         response.getValue&#40;&#41;.hasImmutabilityPolicy&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.getPropertiesWithResponse#String -->
     *
     * @param leaseId The lease ID the active lease on the container must match.
     * @return A reactive response containing the container properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlobContainerProperties>> getPropertiesWithResponse(String leaseId) {
        try {
            return withContext(context -> getPropertiesWithResponse(leaseId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<BlobContainerProperties>> getPropertiesWithResponse(String leaseId, Context context) {
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.getContainers()
            .getPropertiesWithResponseAsync(containerName, null, leaseId, null, context)
            .map(rb -> {
                ContainersGetPropertiesHeaders hd = rb.getDeserializedHeaders();
                BlobContainerProperties properties = new BlobContainerProperties(hd.getXMsMeta(), hd.getETag(),
                    hd.getLastModified(), hd.getXMsLeaseDuration(), hd.getXMsLeaseState(), hd.getXMsLeaseStatus(),
                    hd.getXMsBlobPublicAccess(), Boolean.TRUE.equals(hd.isXMsHasImmutabilityPolicy()),
                    Boolean.TRUE.equals(hd.isXMsHasLegalHold()), hd.getXMsDefaultEncryptionScope(),
                    hd.isXMsDenyEncryptionScopeOverride(), hd.isXMsImmutableStorageWithVersioningEnabled());
                return new SimpleResponse<>(rb, properties);
            });
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.setMetadata#Map -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * client.setMetadata&#40;metadata&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Set metadata completed%n&quot;&#41;,
     *     error -&gt; System.out.printf&#40;&quot;Set metadata failed: %s%n&quot;, error&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.setMetadata#Map -->
     *
     * @param metadata Metadata to associate with the container. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains signalling
     * completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.setMetadataWithResponse#Map-BlobRequestConditions -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * client.setMetadataWithResponse&#40;metadata, requestConditions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Set metadata completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.setMetadataWithResponse#Map-BlobRequestConditions -->
     *
     * @param metadata Metadata to associate with the container. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response signalling completion.
     * @throws UnsupportedOperationException If one of {@link BlobRequestConditions#getIfMatch()},
     * {@link BlobRequestConditions#getIfNoneMatch()}, or {@link BlobRequestConditions#getIfUnmodifiedSince()} is set.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> setMetadataWithResponse(metadata, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        BlobRequestConditions requestConditions, Context context) {
        context = context == null ? Context.NONE : context;
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        if (!ModelHelper.validateNoETag(requestConditions) || requestConditions.getIfUnmodifiedSince() != null) {
            // Throwing is preferred to Mono.error because this will error out immediately instead of waiting until
            // subscription.
            throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
                "If-Modified-Since is the only HTTP access condition supported for this API"));
        }

        return this.azureBlobStorage.getContainers().setMetadataNoCustomHeadersWithResponseAsync(containerName, null,
            requestConditions.getLeaseId(), metadata, requestConditions.getIfModifiedSince(), null, context);
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicy -->
     * <pre>
     * client.getAccessPolicy&#40;&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Blob Access Type: %s%n&quot;, response.getBlobAccessType&#40;&#41;&#41;;
     *
     *     for &#40;BlobSignedIdentifier identifier : response.getIdentifiers&#40;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Identifier Name: %s, Permissions %s%n&quot;,
     *             identifier.getId&#40;&#41;,
     *             identifier.getAccessPolicy&#40;&#41;.getPermissions&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicy -->
     *
     * @return A reactive response containing the container access policy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlobContainerAccessPolicies> getAccessPolicy() {
        return getAccessPolicyWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicyWithResponse#String -->
     * <pre>
     * client.getAccessPolicyWithResponse&#40;leaseId&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Blob Access Type: %s%n&quot;, response.getValue&#40;&#41;.getBlobAccessType&#40;&#41;&#41;;
     *
     *     for &#40;BlobSignedIdentifier identifier : response.getValue&#40;&#41;.getIdentifiers&#40;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Identifier Name: %s, Permissions %s%n&quot;,
     *             identifier.getId&#40;&#41;,
     *             identifier.getAccessPolicy&#40;&#41;.getPermissions&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicyWithResponse#String -->
     *
     * @param leaseId The lease ID the active lease on the container must match.
     * @return A reactive response containing the container access policy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlobContainerAccessPolicies>> getAccessPolicyWithResponse(String leaseId) {
        try {
            return withContext(context -> getAccessPolicyWithResponse(leaseId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<BlobContainerAccessPolicies>> getAccessPolicyWithResponse(String leaseId, Context context) {
        context = context == null ? Context.NONE : context;
        return this.azureBlobStorage.getContainers().getAccessPolicyWithResponseAsync(
            containerName, null, leaseId, null, context)
            .map(response -> new SimpleResponse<>(response,
                new BlobContainerAccessPolicies(response.getDeserializedHeaders().getXMsBlobPublicAccess(),
                response.getValue().items())));
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicy#PublicAccessType-List -->
     * <pre>
     * BlobSignedIdentifier identifier = new BlobSignedIdentifier&#40;&#41;
     *     .setId&#40;&quot;name&quot;&#41;
     *     .setAccessPolicy&#40;new BlobAccessPolicy&#40;&#41;
     *         .setStartsOn&#40;OffsetDateTime.now&#40;&#41;&#41;
     *         .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;7&#41;&#41;
     *         .setPermissions&#40;&quot;permissionString&quot;&#41;&#41;;
     *
     * client.setAccessPolicy&#40;PublicAccessType.CONTAINER, Collections.singletonList&#40;identifier&#41;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Set access policy completed%n&quot;&#41;,
     *     error -&gt; System.out.printf&#40;&quot;Set access policy failed: %s%n&quot;, error&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicy#PublicAccessType-List -->
     *
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link BlobSignedIdentifier} objects that specify the permissions for the container.
     * Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setAccessPolicy(PublicAccessType accessType, List<BlobSignedIdentifier> identifiers) {
        return setAccessPolicyWithResponse(accessType, identifiers, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobRequestConditions -->
     * <pre>
     * BlobSignedIdentifier identifier = new BlobSignedIdentifier&#40;&#41;
     *     .setId&#40;&quot;name&quot;&#41;
     *     .setAccessPolicy&#40;new BlobAccessPolicy&#40;&#41;
     *         .setStartsOn&#40;OffsetDateTime.now&#40;&#41;&#41;
     *         .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;7&#41;&#41;
     *         .setPermissions&#40;&quot;permissionString&quot;&#41;&#41;;
     *
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * client.setAccessPolicyWithResponse&#40;PublicAccessType.CONTAINER, Collections.singletonList&#40;identifier&#41;, requestConditions&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Set access policy completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobRequestConditions -->
     *
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link BlobSignedIdentifier} objects that specify the permissions for the container.
     * Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response signalling completion.
     * @throws UnsupportedOperationException If either {@link BlobRequestConditions#getIfMatch()} or
     * {@link BlobRequestConditions#getIfNoneMatch()} is set.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setAccessPolicyWithResponse(PublicAccessType accessType,
        List<BlobSignedIdentifier> identifiers, BlobRequestConditions requestConditions) {
        try {
            return withContext(context ->
                setAccessPolicyWithResponse(accessType, identifiers, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> setAccessPolicyWithResponse(PublicAccessType accessType,
        List<BlobSignedIdentifier> identifiers, BlobRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;

        if (!ModelHelper.validateNoETag(requestConditions)) {
            // Throwing is preferred to Mono.error because this will error out immediately instead of waiting until
            // subscription.
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("ETag access conditions are not supported for this API."));
        }

        List<BlobSignedIdentifier> finalIdentifiers = ModelHelper.truncateTimeForBlobSignedIdentifier(identifiers);
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.getContainers().setAccessPolicyNoCustomHeadersWithResponseAsync(containerName,
            null, requestConditions.getLeaseId(), accessType, requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), null, finalIdentifiers, context);
    }

    /**
     * Returns a reactive Publisher emitting all the blobs in this container lazily as needed. The directories are
     * flattened and only actual blobs and no directories are returned.
     *
     * <p>
     * Blob names are returned in lexicographic order. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p>
     * E.g. listing a container containing a 'foo' folder, which contains blobs 'foo1' and 'foo2', and a blob on the
     * root level 'bar', will return
     *
     * <ul>
     * <li>foo/foo1
     * <li>foo/foo2
     * <li>bar
     * </ul>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.listBlobs -->
     * <pre>
     * client.listBlobs&#40;&#41;.subscribe&#40;blob -&gt;
     *     System.out.printf&#40;&quot;Name: %s, Directory? %b%n&quot;, blob.getName&#40;&#41;, blob.isPrefix&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.listBlobs -->
     *
     * @return A reactive response emitting the flattened blobs.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<BlobItem> listBlobs() {
        return this.listBlobs(new ListBlobsOptions());
    }

    /**
     * Returns a reactive Publisher emitting all the blobs in this container lazily as needed. The directories are
     * flattened and only actual blobs and no directories are returned.
     *
     * <p>
     * Blob names are returned in lexicographic order. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p>
     * E.g. listing a container containing a 'foo' folder, which contains blobs 'foo1' and 'foo2', and a blob on the
     * root level 'bar', will return
     *
     * <ul>
     * <li>foo/foo1
     * <li>foo/foo2
     * <li>bar
     * </ul>
     *
     *
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.listBlobs#ListBlobsOptions -->
     * <pre>
     * ListBlobsOptions options = new ListBlobsOptions&#40;&#41;
     *     .setPrefix&#40;&quot;prefixToMatch&quot;&#41;
     *     .setDetails&#40;new BlobListDetails&#40;&#41;
     *         .setRetrieveDeletedBlobs&#40;true&#41;
     *         .setRetrieveSnapshots&#40;true&#41;&#41;;
     *
     * client.listBlobs&#40;options&#41;.subscribe&#40;blob -&gt;
     *     System.out.printf&#40;&quot;Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n&quot;,
     *         blob.getName&#40;&#41;,
     *         blob.isPrefix&#40;&#41;,
     *         blob.isDeleted&#40;&#41;,
     *         blob.getSnapshot&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.listBlobs#ListBlobsOptions -->
     *
     * @param options {@link ListBlobsOptions}
     * @return A reactive response emitting the listed blobs, flattened.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<BlobItem> listBlobs(ListBlobsOptions options) {
        return listBlobs(options, null);
    }

    /**
     * Returns a reactive Publisher emitting all the blobs in this container lazily as needed. The directories are
     * flattened and only actual blobs and no directories are returned.
     *
     * <p>
     * Blob names are returned in lexicographic order. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p>
     * E.g. listing a container containing a 'foo' folder, which contains blobs 'foo1' and 'foo2', and a blob on the
     * root level 'bar', will return
     *
     * <ul>
     * <li>foo/foo1
     * <li>foo/foo2
     * <li>bar
     * </ul>
     *
     *
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.listBlobs#ListBlobsOptions-String -->
     * <pre>
     * ListBlobsOptions options = new ListBlobsOptions&#40;&#41;
     *     .setPrefix&#40;&quot;prefixToMatch&quot;&#41;
     *     .setDetails&#40;new BlobListDetails&#40;&#41;
     *         .setRetrieveDeletedBlobs&#40;true&#41;
     *         .setRetrieveSnapshots&#40;true&#41;&#41;;
     *
     * String continuationToken = &quot;continuationToken&quot;;
     *
     * client.listBlobs&#40;options, continuationToken&#41;.subscribe&#40;blob -&gt;
     *     System.out.printf&#40;&quot;Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n&quot;,
     *         blob.getName&#40;&#41;,
     *         blob.isPrefix&#40;&#41;,
     *         blob.isDeleted&#40;&#41;,
     *         blob.getSnapshot&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.listBlobs#ListBlobsOptions-String -->
     *
     * @param options {@link ListBlobsOptions}
     * @param continuationToken Identifies the portion of the list to be returned with the next list operation.
     * @return A reactive response emitting the listed blobs, flattened.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<BlobItem> listBlobs(ListBlobsOptions options, String continuationToken) {
        try {
            return listBlobsFlatWithOptionalTimeout(options, continuationToken, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /*
     * Implementation for this paged listing operation, supporting an optional timeout provided by the synchronous
     * ContainerClient. Applies the given timeout to each
     * Mono<ResponseBase<ContainersListBlobFlatSegmentHeaders, ListBlobsFlatSegmentResponse>> backing the PagedFlux.
     *
     * @param options {@link ListBlobsOptions}.
     * @param timeout An optional timeout to be applied to the network asynchronous operations.
     * @return A reactive response emitting the listed blobs, flattened.
     */
    PagedFlux<BlobItem> listBlobsFlatWithOptionalTimeout(ListBlobsOptions options, String continuationToken,
        Duration timeout) {
        BiFunction<String, Integer, Mono<PagedResponse<BlobItem>>> func =
            (marker, pageSize) -> {
                ListBlobsOptions finalOptions;
                if (pageSize != null) {
                    if (options == null) {
                        finalOptions = new ListBlobsOptions().setMaxResultsPerPage(pageSize);
                    } else {
                        finalOptions = new ListBlobsOptions()
                            .setMaxResultsPerPage(pageSize)
                            .setPrefix(options.getPrefix())
                            .setDetails(options.getDetails());
                    }
                } else {
                    finalOptions = options;
                }

                return listBlobsFlatSegment(marker, finalOptions, timeout)
                    .map(response -> {
                        List<BlobItem> value = response.getValue().getSegment() == null
                            ? Collections.emptyList()
                            : response.getValue().getSegment().getBlobItems().stream()
                            .map(ModelHelper::populateBlobItem)
                            .collect(Collectors.toList());

                        return new PagedResponseBase<>(
                            response.getRequest(),
                            response.getStatusCode(),
                            response.getHeaders(),
                            value,
                            response.getValue().getNextMarker(),
                            response.getDeserializedHeaders());
                    });
            };
        return new PagedFlux<>(pageSize -> func.apply(continuationToken, pageSize), func);
    }

    /*
     * Returns a single segment of blobs starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @param marker
     *         Identifies the portion of the list to be returned with the next list operation.
     *         This value is returned by the response of a previous list operation as the
     *         ListBlobsFlatSegmentResponse.body().getNextMarker(). Set to null to list the first segment.
     * @param options
     *         {@link ListBlobsOptions}
     *
     * @return Emits the successful response.
     */
    private Mono<ResponseBase<ContainersListBlobFlatSegmentHeaders, ListBlobsFlatSegmentResponse>>
        listBlobsFlatSegment(String marker, ListBlobsOptions options, Duration timeout) {
        options = options == null ? new ListBlobsOptions() : options;

        ArrayList<ListBlobsIncludeItem> include =
            options.getDetails().toList().isEmpty() ? null : options.getDetails().toList();

        return StorageImplUtils.applyOptionalTimeout(
            this.azureBlobStorage.getContainers().listBlobFlatSegmentWithResponseAsync(containerName,
                options.getPrefix(), marker, options.getMaxResultsPerPage(), include, null, null, Context.NONE),
            timeout);
    }

    /**
     * Returns a reactive Publisher emitting all the blobs and directories (prefixes) under the given directory
     * (prefix). Directories will have {@link BlobItem#isPrefix()} set to true.
     *
     * <p>
     * Blob names are returned in lexicographic order. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p>
     * E.g. listing a container containing a 'foo' folder, which contains blobs 'foo1' and 'foo2', and a blob on the
     * root level 'bar', will return the following results when prefix=null:
     *
     * <ul>
     * <li>foo/ (isPrefix = true)
     * <li>bar (isPrefix = false)
     * </ul>
     * <p>
     * will return the following results when prefix="foo/":
     *
     * <ul>
     * <li>foo/foo1 (isPrefix = false)
     * <li>foo/foo2 (isPrefix = false)
     * </ul>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.listBlobsByHierarchy#String -->
     * <pre>
     * client.listBlobsByHierarchy&#40;&quot;directoryName&quot;&#41;.subscribe&#40;blob -&gt;
     *     System.out.printf&#40;&quot;Name: %s, Directory? %b%n&quot;, blob.getName&#40;&#41;, blob.isDeleted&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.listBlobsByHierarchy#String -->
     *
     * @param directory The directory to list blobs underneath
     * @return A reactive response emitting the prefixes and blobs.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<BlobItem> listBlobsByHierarchy(String directory) {
        return this.listBlobsByHierarchy("/", new ListBlobsOptions().setPrefix(directory));
    }

    /**
     * Returns a reactive Publisher emitting all the blobs and prefixes (directories) under the given prefix
     * (directory). Directories will have {@link BlobItem#isPrefix()} set to true.
     *
     * <p>
     * Blob names are returned in lexicographic order. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p>
     * E.g. listing a container containing a 'foo' folder, which contains blobs 'foo1' and 'foo2', and a blob on the
     * root level 'bar', will return the following results when prefix=null:
     *
     * <ul>
     * <li>foo/ (isPrefix = true)
     * <li>bar (isPrefix = false)
     * </ul>
     * <p>
     * will return the following results when prefix="foo/":
     *
     * <ul>
     * <li>foo/foo1 (isPrefix = false)
     * <li>foo/foo2 (isPrefix = false)
     * </ul>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.listBlobsByHierarchy#String-ListBlobsOptions -->
     * <pre>
     * ListBlobsOptions options = new ListBlobsOptions&#40;&#41;
     *     .setPrefix&#40;&quot;directoryName&quot;&#41;
     *     .setDetails&#40;new BlobListDetails&#40;&#41;
     *         .setRetrieveDeletedBlobs&#40;true&#41;
     *         .setRetrieveSnapshots&#40;true&#41;&#41;;
     *
     * client.listBlobsByHierarchy&#40;&quot;&#47;&quot;, options&#41;.subscribe&#40;blob -&gt;
     *     System.out.printf&#40;&quot;Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n&quot;,
     *         blob.getName&#40;&#41;,
     *         blob.isPrefix&#40;&#41;,
     *         blob.isDeleted&#40;&#41;,
     *         blob.getSnapshot&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.listBlobsByHierarchy#String-ListBlobsOptions -->
     *
     * @param delimiter The delimiter for blob hierarchy, "/" for hierarchy based on directories
     * @param options {@link ListBlobsOptions}
     * @return A reactive response emitting the prefixes and blobs.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<BlobItem> listBlobsByHierarchy(String delimiter, ListBlobsOptions options) {
        try {
            return listBlobsHierarchyWithOptionalTimeout(delimiter, options, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /*
     * Implementation for this paged listing operation, supporting an optional timeout provided by the synchronous
     * ContainerClient. Applies the given timeout to each
     * Mono<ResponseBase<ContainersListBlobHierarchySegmentHeaders, ListBlobsHierarchySegmentResponse>> backing the
     * PagedFlux.
     *
     * @param delimiter The delimiter for blob hierarchy, "/" for hierarchy based on directories
     * @param options {@link ListBlobsOptions}
     * @param timeout An optional timeout to be applied to the network asynchronous operations.
     * @return A reactive response emitting the listed blobs, flattened.
     */
    PagedFlux<BlobItem> listBlobsHierarchyWithOptionalTimeout(String delimiter, ListBlobsOptions options,
        Duration timeout) {
        BiFunction<String, Integer, Mono<PagedResponse<BlobItem>>> func =
            (marker, pageSize) -> {
                ListBlobsOptions finalOptions;
                /*
                 If pageSize was not set in a .byPage(int) method, the page size from options will be preserved.
                 Otherwise, prefer the new value.
                 */
                if (pageSize != null) {
                    if (options == null) {
                        finalOptions = new ListBlobsOptions().setMaxResultsPerPage(pageSize);
                    } else {
                        // Note that this prefers the value passed to .byPage(int) over the value on the options
                        finalOptions = new ListBlobsOptions()
                            .setMaxResultsPerPage(pageSize)
                            .setPrefix(options.getPrefix())
                            .setDetails(options.getDetails());
                    }
                } else {
                    finalOptions = options;
                }
                return listBlobsHierarchySegment(marker, delimiter, finalOptions, timeout)
                .map(response -> {
                    BlobHierarchyListSegment segment = response.getValue().getSegment();
                    List<BlobItem> value;
                    if (segment == null) {
                        value = Collections.emptyList();
                    } else {
                        value = new ArrayList<>(segment.getBlobItems().size() + segment.getBlobPrefixes().size());
                        segment.getBlobItems().forEach(item -> value.add(BlobItemConstructorProxy.create(item)));
                        segment.getBlobPrefixes().forEach(prefix -> value.add(new BlobItem()
                            .setName(ModelHelper.toBlobNameString(prefix.getName()))
                            .setIsPrefix(true)));
                    }

                    return new PagedResponseBase<>(
                        response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        value,
                        response.getValue().getNextMarker(),
                        response.getDeserializedHeaders());
                });
            };
        return new PagedFlux<>(pageSize -> func.apply(null, pageSize), func);
    }

    private Mono<ResponseBase<ContainersListBlobHierarchySegmentHeaders, ListBlobsHierarchySegmentResponse>>
        listBlobsHierarchySegment(String marker, String delimiter, ListBlobsOptions options, Duration timeout) {
        options = options == null ? new ListBlobsOptions() : options;
        if (options.getDetails().getRetrieveSnapshots()) {
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("Including snapshots in a hierarchical listing is not supported."));
        }

        ArrayList<ListBlobsIncludeItem> include =
            options.getDetails().toList().isEmpty() ? null : options.getDetails().toList();

        return StorageImplUtils.applyOptionalTimeout(
            this.azureBlobStorage.getContainers().listBlobHierarchySegmentWithResponseAsync(containerName, delimiter,
                options.getPrefix(), marker, options.getMaxResultsPerPage(), include, null, null,
                Context.NONE),
            timeout);
    }

    /**
     * Returns a reactive Publisher emitting the blobs in this container whose tags match the query expression. For more
     * information, including information on the query syntax, see the <a href="https://docs.microsoft.com/rest/api/storageservices/find-blobs-by-tags">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.findBlobsByTag#String -->
     * <pre>
     * client.findBlobsByTags&#40;&quot;where=tag=value&quot;&#41;.subscribe&#40;blob -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, blob.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.findBlobsByTag#String -->
     *
     * @param query Filters the results to return only blobs whose tags match the specified expression.
     * @return A reactive response emitting the list of blobs.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TaggedBlobItem> findBlobsByTags(String query) {
        try {
            return this.findBlobsByTags(new FindBlobsOptions(query));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Returns a reactive Publisher emitting the blobs in this container whose tags match the query expression. For more
     * information, including information on the query syntax, see the <a href="https://docs.microsoft.com/rest/api/storageservices/find-blobs-by-tags">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.findBlobsByTag#FindBlobsOptions -->
     * <pre>
     * client.findBlobsByTags&#40;new FindBlobsOptions&#40;&quot;where=tag=value&quot;&#41;.setMaxResultsPerPage&#40;10&#41;&#41;
     *     .subscribe&#40;blob -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, blob.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.findBlobsByTag#FindBlobsOptions -->
     *
     * @param options {@link FindBlobsOptions}
     * @return A reactive response emitting the list of blobs.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TaggedBlobItem> findBlobsByTags(FindBlobsOptions options) {
        try {
            return findBlobsByTags(options, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    PagedFlux<TaggedBlobItem> findBlobsByTags(FindBlobsOptions options, Duration timeout) {
        StorageImplUtils.assertNotNull("options", options);
        BiFunction<String, Integer, Mono<PagedResponse<TaggedBlobItem>>> func =
            (marker, pageSize) -> withContext(context -> this.findBlobsByTags(
                new FindBlobsOptions(options.getQuery()).setMaxResultsPerPage(pageSize), marker, timeout, context));
        return new PagedFlux<>(pageSize -> func.apply(null, pageSize), func);
    }

    PagedFlux<TaggedBlobItem> findBlobsByTags(FindBlobsOptions options, Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        BiFunction<String, Integer, Mono<PagedResponse<TaggedBlobItem>>> func =
            (marker, pageSize) -> {
                FindBlobsOptions finalOptions;
                if (pageSize != null) {
                    finalOptions = new FindBlobsOptions(options.getQuery())
                        .setMaxResultsPerPage(pageSize);
                } else {
                    finalOptions = options;
                }
                return this.findBlobsByTags(finalOptions, marker, timeout, context);
            };
        return new PagedFlux<>(pageSize -> func.apply(null, pageSize), func);
    }

    private Mono<PagedResponse<TaggedBlobItem>> findBlobsByTags(
        FindBlobsOptions options, String marker,
        Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        return StorageImplUtils.applyOptionalTimeout(
            this.azureBlobStorage.getContainers().filterBlobsWithResponseAsync(containerName, null, null,
                options.getQuery(), marker, options.getMaxResultsPerPage(), null, context), timeout)
            .map(response -> {
                List<TaggedBlobItem> value = response.getValue().getBlobs().stream()
                    .map(ModelHelper::populateTaggedBlobItem)
                    .collect(Collectors.toList());

                return new PagedResponseBase<>(
                    response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    value,
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders());
            });
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.getAccountInfo -->
     * <pre>
     * client.getAccountInfo&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Account Kind: %s, SKU: %s%n&quot;,
     *         response.getAccountKind&#40;&#41;,
     *         response.getSkuName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.getAccountInfo -->
     *
     * @return A reactive response containing the account info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<StorageAccountInfo> getAccountInfo() {
        return getAccountInfoWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.getAccountInfoWithResponse -->
     * <pre>
     * client.getAccountInfoWithResponse&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Account Kind: %s, SKU: %s%n&quot;,
     *         response.getValue&#40;&#41;.getAccountKind&#40;&#41;,
     *         response.getValue&#40;&#41;.getSkuName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.getAccountInfoWithResponse -->
     *
     * @return A reactive response containing the account info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse() {
        try {
            return withContext(this::getAccountInfoWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return this.azureBlobStorage.getContainers().getAccountInfoWithResponseAsync(containerName, context)
            .map(rb -> {
                ContainersGetAccountInfoHeaders hd = rb.getDeserializedHeaders();
                return new SimpleResponse<>(rb, new StorageAccountInfo(hd.getXMsSkuName(), hd.getXMsAccountKind()));
            });
    }

    // TODO: Reintroduce this API once service starts supporting it.
//    Mono<BlobContainerAsyncClient> rename(String destinationContainerName) {
//        return renameWithResponse(new BlobContainerRenameOptions(destinationContainerName)).flatMap(FluxUtil::toMono);
//    }

    // TODO: Reintroduce this API once service starts supporting it.
//    Mono<Response<BlobContainerAsyncClient>> renameWithResponse(BlobContainerRenameOptions options) {
//        try {
//            return withContext(context -> this.renameWithResponse(options, context));
//        } catch (RuntimeException ex) {
//            return monoError(LOGGER, ex);
//        }
//    }

//    Mono<Response<BlobContainerAsyncClient>> renameWithResponse(BlobContainerRenameOptions options, Context context) {
//        BlobContainerAsyncClient destinationContainerClient = getServiceAsyncClient()
//            .getBlobContainerAsyncClient(options.getDestinationContainerName());
//        return destinationContainerClient.renameWithResponseHelper(this.getBlobContainerName(), options, context);
//    }

//    Mono<Response<BlobContainerAsyncClient>> renameWithResponseHelper(String sourceContainerName,
//        BlobContainerRenameOptions options, Context context) {
//        StorageImplUtils.assertNotNull("options", options);
//        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
//            : options.getRequestConditions();
//        context = context == null ? Context.NONE : context;
//
//        if (!validateNoETag(requestConditions) || !validateNoTime(requestConditions)
//            || requestConditions.getTagsConditions() != null) {
//            throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
//                "Lease-Id is the only HTTP access condition supported for this API"));
//        }
//
//        return this.azureBlobStorage.getContainers().renameWithResponseAsync(containerName,
//            sourceContainerName, null, null, requestConditions.getLeaseId(),
//            context)
//            .onErrorMap(ModelHelper::mapToBlobStorageException)
//            .map(response -> new SimpleResponse<>(response, this));
//    }

    /**
     * Generates a user delegation SAS for the container using the specified {@link BlobServiceSasSignatureValues}.
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a user delegation SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey -->
     * <pre>
     * OffsetDateTime myExpiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * BlobContainerSasPermission myPermission = new BlobContainerSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * BlobServiceSasSignatureValues myValues = new BlobServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateUserDelegationSas&#40;values, userDelegationKey&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey -->
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link BlobServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information on
     * how to get a user delegation key.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues,
        UserDelegationKey userDelegationKey) {
        return generateUserDelegationSas(blobServiceSasSignatureValues, userDelegationKey, getAccountName(),
            Context.NONE);
    }

    /**
     * Generates a user delegation SAS for the container using the specified {@link BlobServiceSasSignatureValues}.
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a user delegation SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey-String-Context -->
     * <pre>
     * OffsetDateTime myExpiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * BlobContainerSasPermission myPermission = new BlobContainerSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * BlobServiceSasSignatureValues myValues = new BlobServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateUserDelegationSas&#40;values, userDelegationKey, accountName, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey-String-Context -->
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link BlobServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information on
     * how to get a user delegation key.
     * @param accountName The account name.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues,
        UserDelegationKey userDelegationKey, String accountName, Context context) {
        return generateUserDelegationSas(blobServiceSasSignatureValues, userDelegationKey, accountName,
            null, context);
    }

    /**
     * Generates a user delegation SAS for the container using the specified {@link BlobServiceSasSignatureValues}.
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a user delegation SAS.</p>
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link BlobServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information on
     * how to get a user delegation key.
     * @param accountName The account name.
     * @param stringToSignHandler For debugging purposes only. Returns the string to sign that was used to generate the
     * signature.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues,
        UserDelegationKey userDelegationKey, String accountName, Consumer<String> stringToSignHandler, Context context) {
        return new BlobSasImplUtil(blobServiceSasSignatureValues, getBlobContainerName())
            .generateUserDelegationSas(userDelegationKey, accountName, stringToSignHandler, context);
    }

    /**
     * Generates a service SAS for the container using the specified {@link BlobServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.generateSas#BlobServiceSasSignatureValues -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * BlobContainerSasPermission permission = new BlobContainerSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateSas&#40;values&#41;; &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.generateSas#BlobServiceSasSignatureValues -->
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues) {
        return generateSas(blobServiceSasSignatureValues, Context.NONE);
    }

    /**
     * Generates a service SAS for the container using the specified {@link BlobServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerAsyncClient.generateSas#BlobServiceSasSignatureValues-Context -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * BlobContainerSasPermission permission = new BlobContainerSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * client.generateSas&#40;values, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerAsyncClient.generateSas#BlobServiceSasSignatureValues-Context -->
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, Context context) {
        return generateSas(blobServiceSasSignatureValues, null, context);
    }

    /**
     * Generates a service SAS for the container using the specified {@link BlobServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param stringToSignHandler For debugging purposes only. Returns the string to sign that was used to generate the
     * signature.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues,
        Consumer<String> stringToSignHandler, Context context) {
        return new BlobSasImplUtil(blobServiceSasSignatureValues, getBlobContainerName())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), stringToSignHandler, context);
    }

//    private boolean validateNoTime(BlobRequestConditions modifiedRequestConditions) {
//        if (modifiedRequestConditions == null) {
//            return true;
//        }
//        return modifiedRequestConditions.getIfModifiedSince() == null
//            && modifiedRequestConditions.getIfUnmodifiedSince() == null;
//    }
}
