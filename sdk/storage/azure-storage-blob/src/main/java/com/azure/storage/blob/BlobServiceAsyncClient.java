// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.AzureBlobStorageImplBuilder;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.implementation.models.ServicesGetAccountInfoHeaders;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.BlobContainerEncryptionScope;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobContainerListDetails;
import com.azure.storage.blob.models.BlobCorsRule;
import com.azure.storage.blob.models.BlobRetentionPolicy;
import com.azure.storage.blob.models.BlobServiceProperties;
import com.azure.storage.blob.models.BlobServiceStatistics;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.KeyInfo;
import com.azure.storage.blob.models.ListBlobContainersIncludeType;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.TaggedBlobItem;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.options.BlobContainerCreateOptions;
import com.azure.storage.blob.options.FindBlobsOptions;
import com.azure.storage.blob.options.UndeleteBlobContainerOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.AccountSasImplUtil;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;

/**
 * Client to a storage account. It may only be instantiated through a {@link BlobServiceClientBuilder}. This class does
 * not hold any state about a particular storage account but is instead a convenient way of sending off appropriate
 * requests to the resource on the service. It may also be used to construct URLs to blobs and containers.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link
 * BlobContainerAsyncClient} through {@link #getBlobContainerAsyncClient(String)}, and operations on a blob are
 * available on {@link BlobAsyncClient}.
 *
 * <p>
 * Please see the <a href="https://docs.microsoft.com/azure/storage/blobs/storage-blobs-introduction">Azure Docs</a> for
 * more information on containers.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core project
 * (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong> start the actual network
 * operation, until {@code .subscribe()} is called on the reactive response. You can simply convert one of these
 * responses to a {@link java.util.concurrent.CompletableFuture} object through {@link Mono#toFuture()}.
 */
@ServiceClient(builder = BlobServiceClientBuilder.class, isAsync = true)
public final class BlobServiceAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(BlobServiceAsyncClient.class);

    private final AzureBlobStorageImpl azureBlobStorage;

    private final String accountName;
    private final BlobServiceVersion serviceVersion;
    private final CpkInfo customerProvidedKey; // only used to pass down to blob clients
    private final EncryptionScope encryptionScope; // only used to pass down to blob clients
    private final BlobContainerEncryptionScope blobContainerEncryptionScope; // only used to pass down to container
    // clients
    private final boolean anonymousAccess;

    /**
     * Package-private constructor for use by {@link BlobServiceClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param customerProvidedKey Customer provided key used during encryption of the blob's data on the server, pass
     * {@code null} to allow the service to use its own encryption.
     * @param encryptionScope Encryption scope used during encryption of the blob's data on the server, pass
     * {@code null} to allow the service to use its own encryption.
     * @param anonymousAccess Whether the client was built with anonymousAccess
     */
    BlobServiceAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName,
        CpkInfo customerProvidedKey, EncryptionScope encryptionScope,
        BlobContainerEncryptionScope blobContainerEncryptionScope, boolean anonymousAccess) {
        /* Check to make sure the uri is valid. We don't want the error to occur later in the generated layer
           when the sas token has already been applied. */
        try {
            URI.create(url);
        } catch (IllegalArgumentException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
        this.azureBlobStorage = new AzureBlobStorageImplBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.serviceVersion = serviceVersion;

        this.accountName = accountName;
        this.customerProvidedKey = customerProvidedKey;
        this.encryptionScope = encryptionScope;
        this.blobContainerEncryptionScope = blobContainerEncryptionScope;
        this.anonymousAccess = anonymousAccess;
    }

    /**
     * Initializes a {@link BlobContainerAsyncClient} object pointing to the specified container. This method does not
     * create a container. It simply constructs the URL to the container and offers access to methods relevant to
     * containers.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.getBlobContainerAsyncClient#String -->
     * <pre>
     * BlobContainerAsyncClient blobContainerAsyncClient = client.getBlobContainerAsyncClient&#40;&quot;containerName&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.getBlobContainerAsyncClient#String -->
     *
     * @param containerName The name of the container to point to. A value of null or empty string will be interpreted
     *                      as pointing to the root container and will be replaced by "$root".
     * @return A {@link BlobContainerAsyncClient} object pointing to the specified container
     */
    public BlobContainerAsyncClient getBlobContainerAsyncClient(String containerName) {
        if (CoreUtils.isNullOrEmpty(containerName)) {
            containerName = BlobContainerAsyncClient.ROOT_CONTAINER_NAME;
        }

        return new BlobContainerAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(),
            getAccountName(), containerName, customerProvidedKey, encryptionScope, blobContainerEncryptionScope);
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
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public BlobServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainer#String -->
     * <pre>
     * BlobContainerAsyncClient blobContainerAsyncClient =
     *     client.createBlobContainer&#40;&quot;containerName&quot;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainer#String -->
     *
     * @param containerName Name of the container to create
     * @return A {@link Mono} containing a {@link BlobContainerAsyncClient} used to interact with the container created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlobContainerAsyncClient> createBlobContainer(String containerName) {
        return createBlobContainerWithResponse(containerName, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainerWithResponse#String-Map-PublicAccessType -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     *
     * BlobContainerAsyncClient containerClient = client
     *     .createBlobContainerWithResponse&#40;&quot;containerName&quot;, metadata, PublicAccessType.CONTAINER&#41;.block&#40;&#41;.getValue&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainerWithResponse#String-Map-PublicAccessType -->
     *
     * @param containerName Name of the container to create
     * @param metadata Metadata to associate with the container. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * BlobContainerAsyncClient} used to interact with the container created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlobContainerAsyncClient>> createBlobContainerWithResponse(String containerName,
        Map<String, String> metadata, PublicAccessType accessType) {
        try {
            return withContext(context -> createBlobContainerWithResponse(containerName, metadata, accessType,
                context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<BlobContainerAsyncClient>> createBlobContainerWithResponse(String containerName,
        Map<String, String> metadata, PublicAccessType accessType, Context context) {
        throwOnAnonymousAccess();
        BlobContainerAsyncClient blobContainerAsyncClient = getBlobContainerAsyncClient(containerName);

        return blobContainerAsyncClient.createWithResponse(metadata, accessType, context)
            .map(response -> new SimpleResponse<>(response, blobContainerAsyncClient));
    }

    /**
     * Creates a new container within a storage account if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainerIfNotExists#String -->
     * <pre>
     * BlobContainerAsyncClient blobContainerAsyncClient =
     *     client.createBlobContainerIfNotExists&#40;&quot;containerName&quot;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainerIfNotExists#String -->
     *
     * @param containerName Name of the container to create
     * @return A {@link Mono} containing a {@link BlobContainerAsyncClient} used to interact with the container created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlobContainerAsyncClient> createBlobContainerIfNotExists(String containerName) {
        return createBlobContainerIfNotExistsWithResponse(containerName, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new container within a storage account if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainerIfNotExistsWithResponse#String-BlobContainerCreateOptions -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * BlobContainerCreateOptions options = new BlobContainerCreateOptions&#40;&#41;.setMetadata&#40;metadata&#41;
     *     .setPublicAccessType&#40;PublicAccessType.CONTAINER&#41;;
     *
     * client.createBlobContainerIfNotExistsWithResponse&#40;&quot;containerName&quot;, options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *         System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully created.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainerIfNotExistsWithResponse#String-BlobContainerCreateOptions -->
     *
     * @param containerName Name of the container to create
     * @param options {@link BlobContainerCreateOptions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * BlobContainerAsyncClient} used to interact with the container created. If {@link Response}'s status code is 201,
     * a new container was successfully created. If status code is 409, a container with the same name already existed
     * at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlobContainerAsyncClient>> createBlobContainerIfNotExistsWithResponse(String containerName,
        BlobContainerCreateOptions options) {
        try {
            return withContext(context -> createBlobContainerIfNotExistsWithResponse(containerName, options,
                context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<BlobContainerAsyncClient>> createBlobContainerIfNotExistsWithResponse(String containerName,
        BlobContainerCreateOptions options, Context context) {
        try {
            options = options == null ? new BlobContainerCreateOptions() : options;
            return createBlobContainerWithResponse(containerName, options.getMetadata(), options.getPublicAccessType(),
                context).onErrorResume(t -> t instanceof BlobStorageException && ((BlobStorageException) t)
                .getStatusCode() == 409, t -> {
                    HttpResponse response = ((BlobStorageException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), this.getBlobContainerAsyncClient(containerName)));
                });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainer#String -->
     * <pre>
     * client.deleteBlobContainer&#40;&quot;containerName&quot;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Delete container completed%n&quot;&#41;,
     *     error -&gt; System.out.printf&#40;&quot;Delete container failed: %s%n&quot;, error&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainer#String -->
     *
     * @param containerName Name of the container to delete
     * @return A {@link Mono} containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteBlobContainer(String containerName) {
        return deleteBlobContainerWithResponse(containerName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainerWithResponse#String-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * client.deleteBlobContainerWithResponse&#40;&quot;containerName&quot;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Delete container completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainerWithResponse#String-Context -->
     *
     * @param containerName Name of the container to delete
     * @return A {@link Mono} containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteBlobContainerWithResponse(String containerName) {
        try {
            return withContext(context -> deleteBlobContainerWithResponse(containerName, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteBlobContainerWithResponse(String containerName, Context context) {
        throwOnAnonymousAccess();
        return getBlobContainerAsyncClient(containerName).deleteWithResponse(null, context);
    }

    /**
     * Deletes the specified container in the storage account if it exists. For
     * more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainerIfExists#String -->
     * <pre>
     * client.deleteBlobContainerIfExists&#40;&quot;containerName&quot;&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainerIfExists#String -->
     *
     * @param containerName Name of the container to delete
     * @return A reactive {@link Mono} signaling completion. {@code true} indicates that the container was deleted.
     * {@code false} indicates the container does not exist at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteBlobContainerIfExists(String containerName) {
        return deleteBlobContainerIfExistsWithResponse(containerName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified container in the storage account if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainerIfExistsWithResponse#String -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * client.deleteBlobContainerIfExistsWithResponse&#40;&quot;containerName&quot;&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainerIfExistsWithResponse#String -->
     *
     * @param containerName Name of the container to delete
     * @return A reactive response signaling completion. If {@link Response}'s status code is 202, the blob container was
     * successfully deleted. If status code is 404, the blob container does not exist.
     *
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteBlobContainerIfExistsWithResponse(String containerName) {
        try {
            return withContext(context -> deleteBlobContainerIfExistsWithResponse(containerName, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> deleteBlobContainerIfExistsWithResponse(String containerName, Context context) {
        try {
            return deleteBlobContainerWithResponse(containerName, context)
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
     * Gets the URL of the storage account represented by this client.
     *
     * @return the URL.
     */
    public String getAccountUrl() {
        return azureBlobStorage.getUrl();
    }

    /**
     * Returns a reactive Publisher emitting all the containers in this account lazily as needed. For more information,
     * see the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.listBlobContainers -->
     * <pre>
     * client.listBlobContainers&#40;&#41;.subscribe&#40;container -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, container.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.listBlobContainers -->
     *
     * @return A reactive response emitting the list of containers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<BlobContainerItem> listBlobContainers() {
        return this.listBlobContainers(new ListBlobContainersOptions());
    }

    /**
     * Returns a reactive Publisher emitting all the containers in this account lazily as needed. For more information,
     * see the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.listBlobContainers#ListBlobContainersOptions -->
     * <pre>
     * ListBlobContainersOptions options = new ListBlobContainersOptions&#40;&#41;
     *     .setPrefix&#40;&quot;containerNamePrefixToMatch&quot;&#41;
     *     .setDetails&#40;new BlobContainerListDetails&#40;&#41;.setRetrieveMetadata&#40;true&#41;&#41;;
     *
     * client.listBlobContainers&#40;options&#41;.subscribe&#40;container -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, container.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.listBlobContainers#ListBlobContainersOptions -->
     *
     * @param options A {@link ListBlobContainersOptions} which specifies what data should be returned by the service.
     * @return A reactive response emitting the list of containers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<BlobContainerItem> listBlobContainers(ListBlobContainersOptions options) {
        try {
            return listBlobContainersWithOptionalTimeout(options, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    PagedFlux<BlobContainerItem> listBlobContainersWithOptionalTimeout(ListBlobContainersOptions options,
        Duration timeout) {
        throwOnAnonymousAccess();
        BiFunction<String, Integer, Mono<PagedResponse<BlobContainerItem>>> func =
            (marker, pageSize) -> {
                ListBlobContainersOptions finalOptions;
                if (pageSize != null) {
                    if (options == null) {
                        finalOptions = new ListBlobContainersOptions().setMaxResultsPerPage(pageSize);
                    } else {
                        finalOptions = new ListBlobContainersOptions()
                            .setMaxResultsPerPage(pageSize)
                            .setDetails(options.getDetails())
                            .setPrefix(options.getPrefix());
                    }
                } else {
                    finalOptions = options;
                }
                return listBlobContainersSegment(marker, finalOptions, timeout);
            };

        return new PagedFlux<>(pageSize -> func.apply(null, pageSize), func);
    }

    private Mono<PagedResponse<BlobContainerItem>> listBlobContainersSegment(String marker,
        ListBlobContainersOptions options, Duration timeout) {
        options = options == null ? new ListBlobContainersOptions() : options;

        return StorageImplUtils.applyOptionalTimeout(
            this.azureBlobStorage.getServices().listBlobContainersSegmentSinglePageAsync(
                options.getPrefix(), marker, options.getMaxResultsPerPage(),
                toIncludeTypes(options.getDetails()),
                null, null, Context.NONE), timeout);
    }

    /**
     * Returns a reactive Publisher emitting the blobs in this account whose tags match the query expression. For more
     * information, including information on the query syntax, see the <a href="https://docs.microsoft.com/rest/api/storageservices/find-blobs-by-tags">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.findBlobsByTag#String -->
     * <pre>
     * client.findBlobsByTags&#40;&quot;where=tag=value&quot;&#41;.subscribe&#40;blob -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, blob.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.findBlobsByTag#String -->
     *
     * @param query Filters the results to return only blobs whose tags match the specified expression.
     * @return A reactive response emitting the list of blobs.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TaggedBlobItem> findBlobsByTags(String query) {
        return this.findBlobsByTags(new FindBlobsOptions(query));
    }

    /**
     * Returns a reactive Publisher emitting the blobs in this account whose tags match the query expression. For more
     * information, including information on the query syntax, see the <a href="https://docs.microsoft.com/rest/api/storageservices/find-blobs-by-tags">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobAsyncServiceClient.findBlobsByTag#FindBlobsOptions -->
     * <pre>
     * client.findBlobsByTags&#40;new FindBlobsOptions&#40;&quot;where=tag=value&quot;&#41;.setMaxResultsPerPage&#40;10&#41;&#41;
     *     .subscribe&#40;blob -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, blob.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobAsyncServiceClient.findBlobsByTag#FindBlobsOptions -->
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
        throwOnAnonymousAccess();
        StorageImplUtils.assertNotNull("options", options);
        BiFunction<String, Integer, Mono<PagedResponse<TaggedBlobItem>>> func =
            (marker, pageSize) -> withContext(context -> this.findBlobsByTags(
                new FindBlobsOptions(options.getQuery()).setMaxResultsPerPage(pageSize), marker, timeout, context));
        return new PagedFlux<>(pageSize -> func.apply(null, pageSize), func);
    }

    PagedFlux<TaggedBlobItem> findBlobsByTags(FindBlobsOptions options, Duration timeout, Context context) {
        throwOnAnonymousAccess();
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
        throwOnAnonymousAccess();
        StorageImplUtils.assertNotNull("options", options);
        return StorageImplUtils.applyOptionalTimeout(
            this.azureBlobStorage.getServices().filterBlobsWithResponseAsync(null, null,
                options.getQuery(), marker, options.getMaxResultsPerPage(), options.getFilterBlobsIncludeItems(),
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE)), timeout)
            .map(response -> {
                List<TaggedBlobItem> value = response.getValue().getBlobs() == null
                    ? Collections.emptyList()
                    : response.getValue().getBlobs().stream()
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
     * Converts {@link BlobContainerListDetails} into list of {@link ListBlobContainersIncludeType}
     * that contains only options selected. If no option is selected then null is returned.
     *
     * @return a list of selected options converted into {@link ListBlobContainersIncludeType}, null if none
     * of options has been selected.
     */
    private List<ListBlobContainersIncludeType> toIncludeTypes(BlobContainerListDetails blobContainerListDetails) {
        boolean hasDetails = blobContainerListDetails != null
            && (blobContainerListDetails.getRetrieveMetadata()
            || blobContainerListDetails.getRetrieveDeleted()
            || blobContainerListDetails.getRetrieveSystemContainers());
        if (hasDetails) {
            List<ListBlobContainersIncludeType> flags = new ArrayList<>(3);
            if (blobContainerListDetails.getRetrieveDeleted()) {
                flags.add(ListBlobContainersIncludeType.DELETED);
            }
            if (blobContainerListDetails.getRetrieveMetadata()) {
                flags.add(ListBlobContainersIncludeType.METADATA);
            }
            if (blobContainerListDetails.getRetrieveSystemContainers()) {
                flags.add(ListBlobContainersIncludeType.SYSTEM);
            }
            return flags;
        } else {
            return null;
        }
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.getProperties -->
     * <pre>
     * client.getProperties&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Hour metrics enabled: %b, Minute metrics enabled: %b%n&quot;,
     *         response.getHourMetrics&#40;&#41;.isEnabled&#40;&#41;,
     *         response.getMinuteMetrics&#40;&#41;.isEnabled&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.getProperties -->
     *
     * @return A reactive response containing the storage account properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlobServiceProperties> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.getPropertiesWithResponse -->
     * <pre>
     * client.getPropertiesWithResponse&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Hour metrics enabled: %b, Minute metrics enabled: %b%n&quot;,
     *         response.getValue&#40;&#41;.getHourMetrics&#40;&#41;.isEnabled&#40;&#41;,
     *         response.getValue&#40;&#41;.getMinuteMetrics&#40;&#41;.isEnabled&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.getPropertiesWithResponse -->
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the storage
     * account properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlobServiceProperties>> getPropertiesWithResponse() {
        try {
            return withContext(this::getPropertiesWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<BlobServiceProperties>> getPropertiesWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        throwOnAnonymousAccess();
        return this.azureBlobStorage.getServices().getPropertiesWithResponseAsync(null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> new SimpleResponse<>(rb, rb.getValue()));
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     * <p>This method checks to ensure the properties being sent follow the specifications indicated in the Azure Docs.
     * If CORS policies are set, CORS parameters that are not set default to the empty string.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.setProperties#BlobServiceProperties -->
     * <pre>
     * BlobRetentionPolicy loggingRetentionPolicy = new BlobRetentionPolicy&#40;&#41;.setEnabled&#40;true&#41;.setDays&#40;3&#41;;
     * BlobRetentionPolicy metricsRetentionPolicy = new BlobRetentionPolicy&#40;&#41;.setEnabled&#40;true&#41;.setDays&#40;1&#41;;
     *
     * BlobServiceProperties properties = new BlobServiceProperties&#40;&#41;
     *     .setLogging&#40;new BlobAnalyticsLogging&#40;&#41;
     *         .setWrite&#40;true&#41;
     *         .setDelete&#40;true&#41;
     *         .setRetentionPolicy&#40;loggingRetentionPolicy&#41;&#41;
     *     .setHourMetrics&#40;new BlobMetrics&#40;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setRetentionPolicy&#40;metricsRetentionPolicy&#41;&#41;
     *     .setMinuteMetrics&#40;new BlobMetrics&#40;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setRetentionPolicy&#40;metricsRetentionPolicy&#41;&#41;;
     *
     * client.setProperties&#40;properties&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Setting properties completed%n&quot;&#41;,
     *     error -&gt; System.out.printf&#40;&quot;Setting properties failed: %s%n&quot;, error&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.setProperties#BlobServiceProperties -->
     *
     * @param properties Configures the service.
     * @return A {@link Mono} containing the storage account properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setProperties(BlobServiceProperties properties) {
        return setPropertiesWithResponse(properties).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     * <p>This method checks to ensure the properties being sent follow the specifications indicated in the Azure Docs.
     * If CORS policies are set, CORS parameters that are not set default to the empty string.</p>
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.setPropertiesWithResponse#BlobServiceProperties -->
     * <pre>
     * BlobRetentionPolicy loggingRetentionPolicy = new BlobRetentionPolicy&#40;&#41;.setEnabled&#40;true&#41;.setDays&#40;3&#41;;
     * BlobRetentionPolicy metricsRetentionPolicy = new BlobRetentionPolicy&#40;&#41;.setEnabled&#40;true&#41;.setDays&#40;1&#41;;
     *
     * BlobServiceProperties properties = new BlobServiceProperties&#40;&#41;
     *     .setLogging&#40;new BlobAnalyticsLogging&#40;&#41;
     *         .setWrite&#40;true&#41;
     *         .setDelete&#40;true&#41;
     *         .setRetentionPolicy&#40;loggingRetentionPolicy&#41;&#41;
     *     .setHourMetrics&#40;new BlobMetrics&#40;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setRetentionPolicy&#40;metricsRetentionPolicy&#41;&#41;
     *     .setMinuteMetrics&#40;new BlobMetrics&#40;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setRetentionPolicy&#40;metricsRetentionPolicy&#41;&#41;;
     *
     * client.setPropertiesWithResponse&#40;properties&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Setting properties completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.setPropertiesWithResponse#BlobServiceProperties -->
     *
     * @param properties Configures the service.
     * @return A {@link Mono} containing the storage account properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setPropertiesWithResponse(BlobServiceProperties properties) {
        try {
            return withContext(context -> setPropertiesWithResponse(properties, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> setPropertiesWithResponse(BlobServiceProperties properties, Context context) {
        throwOnAnonymousAccess();
        BlobServiceProperties finalProperties = null;
        if (properties != null) {
            finalProperties = new BlobServiceProperties();

            // Logging
            finalProperties.setLogging(properties.getLogging());
            if (finalProperties.getLogging() != null) {
                StorageImplUtils.assertNotNull("Logging Version", finalProperties.getLogging().getVersion());
                validateRetentionPolicy(finalProperties.getLogging().getRetentionPolicy(), "Logging Retention Policy");
            }

            // Hour Metrics
            finalProperties.setHourMetrics(properties.getHourMetrics());
            if (finalProperties.getHourMetrics() != null) {
                StorageImplUtils.assertNotNull("HourMetrics Version", finalProperties.getHourMetrics().getVersion());
                validateRetentionPolicy(finalProperties.getHourMetrics().getRetentionPolicy(), "HourMetrics Retention "
                    + "Policy");
                if (finalProperties.getHourMetrics().isEnabled()) {
                    StorageImplUtils.assertNotNull("HourMetrics IncludeApis",
                        finalProperties.getHourMetrics().isIncludeApis());
                }
            }

            // Minute Metrics
            finalProperties.setMinuteMetrics(properties.getMinuteMetrics());
            if (finalProperties.getMinuteMetrics() != null) {
                StorageImplUtils.assertNotNull("MinuteMetrics Version",
                    finalProperties.getMinuteMetrics().getVersion());
                validateRetentionPolicy(finalProperties.getMinuteMetrics().getRetentionPolicy(), "MinuteMetrics "
                    + "Retention Policy");
                if (finalProperties.getMinuteMetrics().isEnabled()) {
                    StorageImplUtils.assertNotNull("MinuteMetrics IncludeApis",
                        finalProperties.getHourMetrics().isIncludeApis());
                }
            }

            // CORS
            if (properties.getCors() != null) {
                List<BlobCorsRule> corsRules = new ArrayList<>();
                for (BlobCorsRule rule : properties.getCors()) {
                    corsRules.add(validatedCorsRule(rule));
                }
                finalProperties.setCors(corsRules);
            }

            // Default Service Version
            finalProperties.setDefaultServiceVersion(properties.getDefaultServiceVersion());

            // Delete Retention Policy
            finalProperties.setDeleteRetentionPolicy(properties.getDeleteRetentionPolicy());
            validateRetentionPolicy(finalProperties.getDeleteRetentionPolicy(), "DeleteRetentionPolicy Days");

            // Static Website
            finalProperties.setStaticWebsite(properties.getStaticWebsite());

        }
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.getServices().setPropertiesWithResponseAsync(finalProperties, null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Sets any null fields to "" since the service requires all Cors rules to be set if some are set.
     * @param originalRule {@link BlobCorsRule}
     * @return The validated {@link BlobCorsRule}
     */
    private BlobCorsRule validatedCorsRule(BlobCorsRule originalRule) {
        if (originalRule == null) {
            return null;
        }
        BlobCorsRule validRule = new BlobCorsRule();
        validRule.setAllowedHeaders(StorageImplUtils.emptyIfNull(originalRule.getAllowedHeaders()));
        validRule.setAllowedMethods(StorageImplUtils.emptyIfNull(originalRule.getAllowedMethods()));
        validRule.setAllowedOrigins(StorageImplUtils.emptyIfNull(originalRule.getAllowedOrigins()));
        validRule.setExposedHeaders(StorageImplUtils.emptyIfNull(originalRule.getExposedHeaders()));
        validRule.setMaxAgeInSeconds(originalRule.getMaxAgeInSeconds());
        return validRule;
    }

    /**
     * Validates a {@link BlobRetentionPolicy} according to service specs for set properties.
     * @param retentionPolicy {@link BlobRetentionPolicy}
     * @param policyName The name of the variable for errors.
     */
    private void validateRetentionPolicy(BlobRetentionPolicy retentionPolicy, String policyName) {
        if (retentionPolicy == null) {
            return;
        }
        if (retentionPolicy.isEnabled()) {
            StorageImplUtils.assertInBounds(policyName, retentionPolicy.getDays(), 1, 365);
        }
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime -->
     * <pre>
     * client.getUserDelegationKey&#40;delegationKeyStartTime, delegationKeyExpiryTime&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;User delegation key: %s%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime -->
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return A {@link Mono} containing the user delegation key.
     * @throws IllegalArgumentException If {@code start} isn't null and is after {@code expiry}.
     * @throws NullPointerException If {@code expiry} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UserDelegationKey> getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry) {
        return getUserDelegationKeyWithResponse(start, expiry).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime -->
     * <pre>
     * client.getUserDelegationKeyWithResponse&#40;delegationKeyStartTime, delegationKeyExpiryTime&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;User delegation key: %s%n&quot;, response.getValue&#40;&#41;.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime -->
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} containing the user
     * delegation key.
     * @throws IllegalArgumentException If {@code start} isn't null and is after {@code expiry}.
     * @throws NullPointerException If {@code expiry} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<UserDelegationKey>> getUserDelegationKeyWithResponse(OffsetDateTime start,
        OffsetDateTime expiry) {
        try {
            return withContext(context -> getUserDelegationKeyWithResponse(start, expiry, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<UserDelegationKey>> getUserDelegationKeyWithResponse(OffsetDateTime start, OffsetDateTime expiry,
        Context context) {
        StorageImplUtils.assertNotNull("expiry", expiry);
        if (start != null && !start.isBefore(expiry)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("`start` must be null or a datetime before `expiry`."));
        }
        throwOnAnonymousAccess();
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.getServices().getUserDelegationKeyWithResponseAsync(
                new KeyInfo()
                    .setStart(start == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(start))
                    .setExpiry(Constants.ISO_8601_UTC_DATE_FORMATTER.format(expiry)),
                null, null, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> new SimpleResponse<>(rb, rb.getValue()));
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.getStatistics -->
     * <pre>
     * client.getStatistics&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Geo-replication status: %s%n&quot;, response.getGeoReplication&#40;&#41;.getStatus&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.getStatistics -->
     *
     * @return A {@link Mono} containing the storage account statistics.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlobServiceStatistics> getStatistics() {
        return getStatisticsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.getStatisticsWithResponse -->
     * <pre>
     * client.getStatisticsWithResponse&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Geo-replication status: %s%n&quot;, response.getValue&#40;&#41;.getGeoReplication&#40;&#41;.getStatus&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.getStatisticsWithResponse -->
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} containing the
     * storage account statistics.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlobServiceStatistics>> getStatisticsWithResponse() {
        try {
            return withContext(this::getStatisticsWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<BlobServiceStatistics>> getStatisticsWithResponse(Context context) {
        throwOnAnonymousAccess();
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.getServices().getStatisticsWithResponseAsync(null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> new SimpleResponse<>(rb, rb.getValue()));
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-account-information">Azure Docs</a>.
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.getAccountInfo -->
     * <pre>
     * client.getAccountInfo&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Account kind: %s, SKU: %s%n&quot;, response.getAccountKind&#40;&#41;, response.getSkuName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.getAccountInfo -->
     *
     * @return A {@link Mono} containing the storage account info.
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
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.getAccountInfoWithResponse -->
     * <pre>
     * client.getAccountInfoWithResponse&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Account kind: %s, SKU: %s%n&quot;, response.getValue&#40;&#41;.getAccountKind&#40;&#41;,
     *         response.getValue&#40;&#41;.getSkuName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.getAccountInfoWithResponse -->
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} the storage account
     * info.
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
        throwOnAnonymousAccess();
        return this.azureBlobStorage.getServices().getAccountInfoWithResponseAsync(context)
            .map(rb -> {
                ServicesGetAccountInfoHeaders hd = rb.getDeserializedHeaders();
                return new SimpleResponse<>(rb, new StorageAccountInfo(hd.getXMsSkuName(), hd.getXMsAccountKind()));
            });
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
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p>The snippet below generates a SAS that lasts for two days and gives the user read and list access to blob
     * containers and file shares.</p>
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.generateAccountSas#AccountSasSignatureValues -->
     * <pre>
     * AccountSasPermission permissions = new AccountSasPermission&#40;&#41;
     *     .setListPermission&#40;true&#41;
     *     .setReadPermission&#40;true&#41;;
     * AccountSasResourceType resourceTypes = new AccountSasResourceType&#40;&#41;.setContainer&#40;true&#41;;
     * AccountSasService services = new AccountSasService&#40;&#41;.setBlobAccess&#40;true&#41;.setFileAccess&#40;true&#41;;
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plus&#40;Duration.ofDays&#40;2&#41;&#41;;
     *
     * AccountSasSignatureValues sasValues =
     *     new AccountSasSignatureValues&#40;expiryTime, permissions, services, resourceTypes&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * String sas = client.generateAccountSas&#40;sasValues&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.generateAccountSas#AccountSasSignatureValues -->
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues) {
        return generateAccountSas(accountSasSignatureValues, Context.NONE);
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p>The snippet below generates a SAS that lasts for two days and gives the user read and list access to blob
     * containers and file shares.</p>
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.generateAccountSas#AccountSasSignatureValues-Context -->
     * <pre>
     * AccountSasPermission permissions = new AccountSasPermission&#40;&#41;
     *     .setListPermission&#40;true&#41;
     *     .setReadPermission&#40;true&#41;;
     * AccountSasResourceType resourceTypes = new AccountSasResourceType&#40;&#41;.setContainer&#40;true&#41;;
     * AccountSasService services = new AccountSasService&#40;&#41;.setBlobAccess&#40;true&#41;.setFileAccess&#40;true&#41;;
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plus&#40;Duration.ofDays&#40;2&#41;&#41;;
     *
     * AccountSasSignatureValues sasValues =
     *     new AccountSasSignatureValues&#40;expiryTime, permissions, services, resourceTypes&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * String sas = client.generateAccountSas&#40;sasValues, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.generateAccountSas#AccountSasSignatureValues-Context -->
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues, Context context) {
        throwOnAnonymousAccess();
        return new AccountSasImplUtil(accountSasSignatureValues,
            this.encryptionScope == null ? null : this.encryptionScope.getEncryptionScope())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
    }

    /**
     * Checks if service client was built with credentials.
     */
    private void throwOnAnonymousAccess() {
        if (anonymousAccess) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Service client cannot be accessed without "
                + "credentials"));
        }
    }

    /**
     * Restores a previously deleted container.
     * If the container associated with provided <code>deletedContainerName</code>
     * already exists, this call will result in a 409 (conflict).
     * This API is only functional if Container Soft Delete is enabled
     * for the storage account associated with the container.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.undeleteBlobContainer#String-String -->
     * <pre>
     * ListBlobContainersOptions listBlobContainersOptions = new ListBlobContainersOptions&#40;&#41;;
     * listBlobContainersOptions.getDetails&#40;&#41;.setRetrieveDeleted&#40;true&#41;;
     * client.listBlobContainers&#40;listBlobContainersOptions&#41;.flatMap&#40;
     *     deletedContainer -&gt; &#123;
     *         Mono&lt;BlobContainerAsyncClient&gt; blobContainerClient = client.undeleteBlobContainer&#40;
     *             deletedContainer.getName&#40;&#41;, deletedContainer.getVersion&#40;&#41;&#41;;
     *         return blobContainerClient;
     *     &#125;
     * &#41;.then&#40;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.undeleteBlobContainer#String-String -->
     *
     * @param deletedContainerName The name of the previously deleted container.
     * @param deletedContainerVersion The version of the previously deleted container.
     * @return A {@link Mono} containing a {@link BlobContainerAsyncClient} used
     * to interact with the restored container.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlobContainerAsyncClient> undeleteBlobContainer(String deletedContainerName,
        String deletedContainerVersion) {
        try {
            return this.undeleteBlobContainerWithResponse(new UndeleteBlobContainerOptions(deletedContainerName,
                deletedContainerVersion))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Restores a previously deleted container. The restored container
     * will be renamed to the <code>destinationContainerName</code> if provided in <code>options</code>.
     * Otherwise <code>deletedContainerName</code> is used as destination container name.
     * If the container associated with provided <code>destinationContainerName</code>
     * already exists, this call will result in a 409 (conflict).
     * This API is only functional if Container Soft Delete is enabled
     * for the storage account associated with the container.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.undeleteBlobContainerWithResponse#UndeleteBlobContainerOptions -->
     * <pre>
     * ListBlobContainersOptions listBlobContainersOptions = new ListBlobContainersOptions&#40;&#41;;
     * listBlobContainersOptions.getDetails&#40;&#41;.setRetrieveDeleted&#40;true&#41;;
     * client.listBlobContainers&#40;listBlobContainersOptions&#41;.flatMap&#40;
     *     deletedContainer -&gt; &#123;
     *         Mono&lt;BlobContainerAsyncClient&gt; blobContainerClient = client.undeleteBlobContainerWithResponse&#40;
     *             new UndeleteBlobContainerOptions&#40;deletedContainer.getName&#40;&#41;, deletedContainer.getVersion&#40;&#41;&#41;&#41;
     *             .map&#40;Response::getValue&#41;;
     *         return blobContainerClient;
     *     &#125;
     * &#41;.then&#40;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.undeleteBlobContainerWithResponse#UndeleteBlobContainerOptions -->
     *
     * @param options {@link UndeleteBlobContainerOptions}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * BlobContainerAsyncClient} used to interact with the restored container.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlobContainerAsyncClient>> undeleteBlobContainerWithResponse(
        UndeleteBlobContainerOptions options) {
        try {
            return withContext(context -> undeleteBlobContainerWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<BlobContainerAsyncClient>> undeleteBlobContainerWithResponse(
        UndeleteBlobContainerOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        boolean hasOptionalDestinationContainerName = options.getDestinationContainerName() != null;
        String finalDestinationContainerName =
            hasOptionalDestinationContainerName ? options.getDestinationContainerName()
                : options.getDeletedContainerName();
        context = context == null ? Context.NONE : context;
        return this.azureBlobStorage.getContainers().restoreWithResponseAsync(finalDestinationContainerName, null,
            null, options.getDeletedContainerName(), options.getDeletedContainerVersion(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response,
                getBlobContainerAsyncClient(finalDestinationContainerName)));
    }

//    /**
//     * Renames an existing blob container.
//     *
//     * <p><strong>Code Samples</strong></p>
//     *
//     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.renameBlobContainer#String-String -->
//     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.renameBlobContainer#String-String -->
//     *
//     * @param sourceContainerName The current name of the container.
//     * @param destinationContainerName The new name of the container.
//     * @return A {@link Mono} containing a {@link BlobContainerAsyncClient} used to interact with the renamed container.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    Mono<BlobContainerAsyncClient> renameBlobContainer(String sourceContainerName,
//        String destinationContainerName) {
//        return renameBlobContainerWithResponse(sourceContainerName,
//            new BlobContainerRenameOptions(destinationContainerName)).flatMap(FluxUtil::toMono);
//    }
//
//    /**
//     * Renames an existing blob container.
//     *
//     * <p><strong>Code Samples</strong></p>
//     *
//     * <!-- src_embed com.azure.storage.blob.BlobServiceAsyncClient.renameBlobContainerWithResponse#String-BlobContainerRenameOptions -->
//     * <!-- end com.azure.storage.blob.BlobServiceAsyncClient.renameBlobContainerWithResponse#String-BlobContainerRenameOptions -->
//     *
//     * @param sourceContainerName The current name of the container.
//     * @param options {@link BlobContainerRenameOptions}
//     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a
//     * {@link BlobContainerAsyncClient} used to interact with the renamed container.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    Mono<Response<BlobContainerAsyncClient>> renameBlobContainerWithResponse(String sourceContainerName,
//        BlobContainerRenameOptions options) {
//        try {
//            return withContext(context -> renameBlobContainerWithResponse(sourceContainerName, options, context));
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
//    }
//
//    Mono<Response<BlobContainerAsyncClient>> renameBlobContainerWithResponse(String sourceContainerName,
//        BlobContainerRenameOptions options, Context context) {
//        BlobContainerAsyncClient destinationContainerClient = getBlobContainerAsyncClient(
//            options.getDestinationContainerName());
//        return destinationContainerClient.renameWithResponseHelper(sourceContainerName, options, context);
//    }
}
