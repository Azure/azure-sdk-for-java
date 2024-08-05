// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.AzureBlobStorageImplBuilder;
import com.azure.storage.blob.implementation.models.ContainersRestoreHeaders;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.implementation.models.FilterBlobSegment;
import com.azure.storage.blob.implementation.models.ServicesFilterBlobsHeaders;
import com.azure.storage.blob.implementation.models.ServicesGetAccountInfoHeaders;
import com.azure.storage.blob.implementation.models.ServicesGetPropertiesHeaders;
import com.azure.storage.blob.implementation.models.ServicesGetStatisticsHeaders;
import com.azure.storage.blob.implementation.models.ServicesGetUserDelegationKeyHeaders;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.BlobContainerEncryptionScope;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobContainerListDetails;
import com.azure.storage.blob.models.BlobCorsRule;
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

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.azure.storage.common.implementation.StorageImplUtils.sendRequest;

/**
 * Client to a storage account. It may only be instantiated through a {@link BlobServiceClientBuilder}. This class does
 * not hold any state about a particular storage account but is instead a convenient way of sending off appropriate
 * requests to the resource on the service. It may also be used to construct URLs to blobs and containers.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link BlobContainerClient}
 * through {@link #getBlobContainerClient(String)}, and operations on a blob are available on {@link BlobClient}.
 *
 * <p>
 * Please see <a href=https://docs.microsoft.com/azure/storage/blobs/storage-blobs-introduction>here</a> for more
 * information on containers.
 */
@ServiceClient(builder = BlobServiceClientBuilder.class)
public final class BlobServiceClient {
    private static final ClientLogger LOGGER = new ClientLogger(BlobServiceClient.class);
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
    BlobServiceClient(HttpPipeline pipeline, String url,
        BlobServiceVersion serviceVersion, String accountName, CpkInfo customerProvidedKey, EncryptionScope encryptionScope,
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
     * Initializes a {@link BlobContainerClient} object pointing to the specified container. This method does not create
     * a container. It simply constructs the URL to the container and offers access to methods relevant to containers.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.getBlobContainerClient#String -->
     * <pre>
     * BlobContainerClient blobContainerClient = client.getBlobContainerClient&#40;&quot;containerName&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.getBlobContainerClient#String -->
     *
     * @param containerName The name of the container to point to.
     * @return A {@link BlobContainerClient} object pointing to the specified container
     */
    public BlobContainerClient getBlobContainerClient(String containerName) {
        if (CoreUtils.isNullOrEmpty(containerName)) {
            containerName = BlobContainerAsyncClient.ROOT_CONTAINER_NAME;
        }
        return new BlobContainerClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(),
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
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.createBlobContainer#String -->
     * <pre>
     * BlobContainerClient blobContainerClient = client.createBlobContainer&#40;&quot;containerName&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.createBlobContainer#String -->
     *
     * @param containerName Name of the container to create
     * @return The {@link BlobContainerClient} used to interact with the container created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobContainerClient createBlobContainer(String containerName) {
        return createBlobContainerWithResponse(containerName, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.createBlobContainerWithResponse#String-Map-PublicAccessType-Context -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * BlobContainerClient blobContainerClient = client.createBlobContainerWithResponse&#40;
     *     &quot;containerName&quot;,
     *     metadata,
     *     PublicAccessType.CONTAINER,
     *     context&#41;.getValue&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.createBlobContainerWithResponse#String-Map-PublicAccessType-Context -->
     *
     * @param containerName Name of the container to create
     * @param metadata Metadata to associate with the container. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link BlobContainerClient} used
     * to interact with the container created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobContainerClient> createBlobContainerWithResponse(String containerName,
        Map<String, String> metadata, PublicAccessType accessType, Context context) {
        BlobContainerClient client = getBlobContainerClient(containerName);
        return new SimpleResponse<>(client.createWithResponse(metadata, accessType, null, context), client);
    }

    /**
     * Creates a new container within a storage account if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.createBlobContainerIfNotExists#String -->
     * <pre>
     * BlobContainerClient blobContainerClient = client.createBlobContainerIfNotExists&#40;&quot;containerName&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.createBlobContainerIfNotExists#String -->
     *
     * @param containerName Name of the container to create
     * @return The {@link BlobContainerClient} used to interact with the container created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobContainerClient createBlobContainerIfNotExists(String containerName) {
        return createBlobContainerIfNotExistsWithResponse(containerName, null, Context.NONE).getValue();
    }

    /**
     * Creates a new container within a storage account if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.createBlobContainerIfNotExistsWithResponse#String-BlobContainerCreateOptions-Context -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * BlobContainerCreateOptions options = new BlobContainerCreateOptions&#40;&#41;.setMetadata&#40;metadata&#41;
     *     .setPublicAccessType&#40;PublicAccessType.CONTAINER&#41;;
     *
     * Response&lt;BlobContainerClient&gt; response = client.createBlobContainerIfNotExistsWithResponse&#40;&quot;containerName&quot;,
     *     options, context&#41;;
     *
     * if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *     System.out.println&#40;&quot;Already existed.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.createBlobContainerIfNotExistsWithResponse#String-BlobContainerCreateOptions-Context -->
     *
     * @param containerName Name of the container to create
     * @param options {@link BlobContainerCreateOptions}
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link BlobContainerClient} used
     * to interact with the container created. If {@link Response}'s status code is 201, a new container was
     * successfully created. If status code is 409, a container with the same name already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobContainerClient> createBlobContainerIfNotExistsWithResponse(String containerName,
        BlobContainerCreateOptions options, Context context) {
        BlobContainerClient client = getBlobContainerClient(containerName);
        return new SimpleResponse<>(client.createIfNotExistsWithResponse(options, null, context), client);
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.deleteBlobContainer#String -->
     * <pre>
     * try &#123;
     *     client.deleteBlobContainer&#40;&quot;container Name&quot;&#41;;
     *     System.out.printf&#40;&quot;Delete container completed with status %n&quot;&#41;;
     * &#125; catch &#40;UnsupportedOperationException error&#41; &#123;
     *     System.out.printf&#40;&quot;Delete container failed: %s%n&quot;, error&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.deleteBlobContainer#String -->
     *
     * @param containerName Name of the container to delete
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteBlobContainer(String containerName) {
        deleteBlobContainerWithResponse(containerName, Context.NONE);
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * @param containerName Name of the container to delete
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteBlobContainerWithResponse(String containerName, Context context) {
        return getBlobContainerClient(containerName).deleteWithResponse(null, null, context);
    }

    /**
     * Deletes the specified container in the storage account if it exists. For
     * more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.deleteBlobContainerIfExists#String -->
     * <pre>
     * boolean result = client.deleteBlobContainerIfExists&#40;&quot;container Name&quot;&#41;;
     * System.out.println&#40;&quot;Delete container completed: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.deleteBlobContainerIfExists#String -->
     *
     * @param containerName Name of the container to delete
     * @return {@code true} if the container is successfully deleted, {@code false} if the container does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteBlobContainerIfExists(String containerName) {
        return deleteBlobContainerIfExistsWithResponse(containerName, Context.NONE).getValue();
    }

    /**
     * Deletes the specified container in the storage account if it exists. For
     * more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.deleteBlobContainerIfExistsWithResponse#String-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * Response&lt;Boolean&gt; response = client.deleteBlobContainerIfExistsWithResponse&#40;&quot;containerName&quot;, context&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.deleteBlobContainerIfExistsWithResponse#String-Context -->
     *
     * @param containerName Name of the container to delete
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 202, the blob
     * container was successfully deleted. If status code is 404, the container does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteBlobContainerIfExistsWithResponse(String containerName, Context context) {
        return getBlobContainerClient(containerName).deleteIfExistsWithResponse(null, null, context);
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
     * Returns a lazy loaded list of containers in this account. The returned {@link PagedIterable} can be consumed
     * while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.listBlobContainers -->
     * <pre>
     * client.listBlobContainers&#40;&#41;.forEach&#40;container -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, container.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.listBlobContainers -->
     *
     * @return The list of containers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BlobContainerItem> listBlobContainers() {
        return this.listBlobContainers(new ListBlobContainersOptions(), null);
    }

    /**
     * Returns a lazy loaded list of containers in this account. The returned {@link PagedIterable} can be consumed
     * while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.listBlobContainers#ListBlobContainersOptions-Duration -->
     * <pre>
     * ListBlobContainersOptions options = new ListBlobContainersOptions&#40;&#41;
     *     .setPrefix&#40;&quot;containerNamePrefixToMatch&quot;&#41;
     *     .setDetails&#40;new BlobContainerListDetails&#40;&#41;.setRetrieveMetadata&#40;true&#41;&#41;;
     *
     * client.listBlobContainers&#40;options, timeout&#41;.forEach&#40;container -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, container.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.listBlobContainers#ListBlobContainersOptions-Duration -->
     *
     * @param options A {@link ListBlobContainersOptions} which specifies what data should be returned by the service.
     * If iterating by page, the page size passed to byPage methods such as
     * {@link PagedIterable#iterableByPage(int)} will be preferred over the value set on these options.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The list of containers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BlobContainerItem> listBlobContainers(ListBlobContainersOptions options, Duration timeout) {
        throwOnAnonymousAccess();
        BiFunction<String, Integer, PagedResponse<BlobContainerItem>> pageRetriever = (marker, pageSize) -> {
            ListBlobContainersOptions finalOptions = options != null ? options : new ListBlobContainersOptions();
            Integer finalPageSize = pageSize != null ? pageSize : finalOptions.getMaxResultsPerPage();
            return listBlobContainersSegment(marker, finalOptions.getDetails(), finalOptions.getPrefix(), finalPageSize,
                timeout);
        };
        return new PagedIterable<>(pageSize -> pageRetriever.apply(null, pageSize), pageRetriever);

    }

    private PagedResponse<BlobContainerItem> listBlobContainersSegment(String marker, BlobContainerListDetails details,
        String prefix, Integer maxResultsPerPage, Duration timeout) {
        // Set up the include types based on the details provided in the options
        List<ListBlobContainersIncludeType> include = ModelHelper.toIncludeTypes(details);

        Callable<PagedResponse<BlobContainerItem>> operation = () -> this.azureBlobStorage.getServices()
            .listBlobContainersSegmentSinglePage(prefix, marker, maxResultsPerPage, include,
                null, null, Context.NONE);
        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Returns a lazy loaded list of blobs in this account whose tags match the query expression. The returned
     * {@link PagedIterable} can be consumed while new items are automatically retrieved as needed. For more
     * information, including information on the query syntax, see the <a href="https://docs.microsoft.com/rest/api/storageservices/find-blobs-by-tags">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.findBlobsByTag#String -->
     * <pre>
     * client.findBlobsByTags&#40;&quot;where=tag=value&quot;&#41;.forEach&#40;blob -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, blob.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.findBlobsByTag#String -->
     *
     * @param query Filters the results to return only blobs whose tags match the specified expression.
     * @return The list of blobs.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TaggedBlobItem> findBlobsByTags(String query) {
        return this.findBlobsByTags(new FindBlobsOptions(query), null, Context.NONE);
    }

    /**
     * Returns a lazy loaded list of blobs in this account whose tags match the query expression. The returned
     * {@link PagedIterable} can be consumed while new items are automatically retrieved as needed. For more
     * information, including information on the query syntax, see the <a href="https://docs.microsoft.com/rest/api/storageservices/find-blobs-by-tags">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.findBlobsByTag#FindBlobsOptions-Duration -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * client.findBlobsByTags&#40;new FindBlobsOptions&#40;&quot;where=tag=value&quot;&#41;.setMaxResultsPerPage&#40;10&#41;, timeout, context&#41;
     *     .forEach&#40;blob -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, blob.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.findBlobsByTag#FindBlobsOptions-Duration -->
     *
     * @param options {@link FindBlobsOptions}. If iterating by page, the page size passed to byPage methods such as
     * {@link PagedIterable#iterableByPage(int)} will be preferred over the value set on these options.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The list of blobs.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<TaggedBlobItem> findBlobsByTags(FindBlobsOptions options, Duration timeout, Context context) {
        throwOnAnonymousAccess();
        StorageImplUtils.assertNotNull("options", options);
        BiFunction<String, Integer, PagedResponse<TaggedBlobItem>> func = (marker, pageSize) -> {
            FindBlobsOptions finalOptions = (pageSize != null)
                ? new FindBlobsOptions(options.getQuery()).setMaxResultsPerPage(pageSize) : options;
            return findBlobsByTagsHelper(finalOptions, marker, timeout, context);
        };
        return new PagedIterable<>(pageSize -> func.apply(null, pageSize), func);
    }

    private PagedResponse<TaggedBlobItem> findBlobsByTagsHelper(FindBlobsOptions options, String marker,
        Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;

        StorageImplUtils.assertNotNull("options", options);
        Callable<ResponseBase<ServicesFilterBlobsHeaders, FilterBlobSegment>> operation = () ->
            this.azureBlobStorage.getServices().filterBlobsWithResponse(null, null, options.getQuery(), marker,
                options.getMaxResultsPerPage(), null, finalContext);

        ResponseBase<ServicesFilterBlobsHeaders, FilterBlobSegment> response =
            StorageImplUtils.sendRequest(operation, timeout, BlobStorageException.class);

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
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.getProperties -->
     * <pre>
     * BlobServiceProperties properties = client.getProperties&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Hour metrics enabled: %b, Minute metrics enabled: %b%n&quot;,
     *     properties.getHourMetrics&#40;&#41;.isEnabled&#40;&#41;,
     *     properties.getMinuteMetrics&#40;&#41;.isEnabled&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.getProperties -->
     *
     * @return The storage account properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobServiceProperties getProperties() {
        return getPropertiesWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.getPropertiesWithResponse#Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * BlobServiceProperties properties = client.getPropertiesWithResponse&#40;timeout, context&#41;.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Hour metrics enabled: %b, Minute metrics enabled: %b%n&quot;,
     *     properties.getHourMetrics&#40;&#41;.isEnabled&#40;&#41;,
     *     properties.getMinuteMetrics&#40;&#41;.isEnabled&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.getPropertiesWithResponse#Duration-Context -->
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the storage account properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobServiceProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        throwOnAnonymousAccess();
        Callable<ResponseBase<ServicesGetPropertiesHeaders, BlobServiceProperties>> operation = () ->
            this.azureBlobStorage.getServices().getPropertiesWithResponse(null, null, finalContext);
        ResponseBase<ServicesGetPropertiesHeaders, BlobServiceProperties> response =
            StorageImplUtils.sendRequest(operation, timeout, BlobStorageException.class);

        return new SimpleResponse<>(response, response.getValue());
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
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.setProperties#BlobServiceProperties -->
     * <pre>
     * BlobRetentionPolicy loggingRetentionPolicy = new BlobRetentionPolicy&#40;&#41;.setEnabled&#40;true&#41;.setDays&#40;3&#41;;
     * BlobRetentionPolicy metricsRetentionPolicy = new BlobRetentionPolicy&#40;&#41;.setEnabled&#40;true&#41;.setDays&#40;1&#41;;
     *
     * BlobServiceProperties properties = new BlobServiceProperties&#40;&#41;
     *     .setLogging&#40;new BlobAnalyticsLogging&#40;&#41;
     *         .setWrite&#40;true&#41;
     *         .setDelete&#40;true&#41;
     *         .setVersion&#40;&quot;1.0&quot;&#41;
     *         .setRetentionPolicy&#40;loggingRetentionPolicy&#41;&#41;
     *     .setHourMetrics&#40;new BlobMetrics&#40;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setVersion&#40;&quot;1.0&quot;&#41;
     *         .setIncludeApis&#40;true&#41;
     *         .setRetentionPolicy&#40;metricsRetentionPolicy&#41;&#41;
     *     .setMinuteMetrics&#40;new BlobMetrics&#40;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setVersion&#40;&quot;1.0&quot;&#41;
     *         .setIncludeApis&#40;true&#41;
     *         .setRetentionPolicy&#40;metricsRetentionPolicy&#41;&#41;;
     *
     * try &#123;
     *     client.setProperties&#40;properties&#41;;
     *     System.out.printf&#40;&quot;Setting properties completed%n&quot;&#41;;
     * &#125; catch &#40;UnsupportedOperationException error&#41; &#123;
     *     System.out.printf&#40;&quot;Setting properties failed: %s%n&quot;, error&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.setProperties#BlobServiceProperties -->
     *
     * @param properties Configures the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setProperties(BlobServiceProperties properties) {
        setPropertiesWithResponse(properties, null, Context.NONE);
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
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.setPropertiesWithResponse#BlobServiceProperties-Duration-Context -->
     * <pre>
     * BlobRetentionPolicy loggingRetentionPolicy = new BlobRetentionPolicy&#40;&#41;.setEnabled&#40;true&#41;.setDays&#40;3&#41;;
     * BlobRetentionPolicy metricsRetentionPolicy = new BlobRetentionPolicy&#40;&#41;.setEnabled&#40;true&#41;.setDays&#40;1&#41;;
     *
     * BlobServiceProperties properties = new BlobServiceProperties&#40;&#41;
     *     .setLogging&#40;new BlobAnalyticsLogging&#40;&#41;
     *         .setWrite&#40;true&#41;
     *         .setDelete&#40;true&#41;
     *         .setVersion&#40;&quot;1.0&quot;&#41;
     *         .setRetentionPolicy&#40;loggingRetentionPolicy&#41;&#41;
     *     .setHourMetrics&#40;new BlobMetrics&#40;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setVersion&#40;&quot;1.0&quot;&#41;
     *         .setIncludeApis&#40;true&#41;
     *         .setRetentionPolicy&#40;metricsRetentionPolicy&#41;&#41;
     *     .setMinuteMetrics&#40;new BlobMetrics&#40;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setVersion&#40;&quot;1.0&quot;&#41;
     *         .setIncludeApis&#40;true&#41;
     *         .setRetentionPolicy&#40;metricsRetentionPolicy&#41;&#41;;
     *
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Setting properties completed with status %d%n&quot;,
     *     client.setPropertiesWithResponse&#40;properties, timeout, context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.setPropertiesWithResponse#BlobServiceProperties-Duration-Context -->
     *
     * @param properties Configures the service.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The storage account properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setPropertiesWithResponse(BlobServiceProperties properties, Duration timeout,
        Context context) {
        throwOnAnonymousAccess();
        BlobServiceProperties finalProperties;
        if (properties != null) {
            finalProperties = new BlobServiceProperties();

            // Logging
            finalProperties.setLogging(properties.getLogging());
            if (finalProperties.getLogging() != null) {
                StorageImplUtils.assertNotNull("Logging Version", finalProperties.getLogging().getVersion());
                ModelHelper.validateRetentionPolicy(finalProperties.getLogging().getRetentionPolicy(), "Logging Retention Policy");
            }

            // Hour Metrics
            finalProperties.setHourMetrics(properties.getHourMetrics());
            if (finalProperties.getHourMetrics() != null) {
                StorageImplUtils.assertNotNull("HourMetrics Version", finalProperties.getHourMetrics().getVersion());
                ModelHelper.validateRetentionPolicy(finalProperties.getHourMetrics().getRetentionPolicy(), "HourMetrics Retention "
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
                ModelHelper.validateRetentionPolicy(finalProperties.getMinuteMetrics().getRetentionPolicy(), "MinuteMetrics "
                    + "Retention Policy");
                if (finalProperties.getMinuteMetrics().isEnabled()) {
                    StorageImplUtils.assertNotNull("MinuteMetrics IncludeApis",
                        finalProperties.getHourMetrics().isIncludeApis());
                }
            }

            // CORS
            List<BlobCorsRule> corsRules = new ArrayList<>();
            for (BlobCorsRule rule : properties.getCors()) {
                corsRules.add(ModelHelper.validatedCorsRule(rule));
            }
            finalProperties.setCors(corsRules);

            // Default Service Version
            finalProperties.setDefaultServiceVersion(properties.getDefaultServiceVersion());

            // Delete Retention Policy
            finalProperties.setDeleteRetentionPolicy(properties.getDeleteRetentionPolicy());
            ModelHelper.validateRetentionPolicy(finalProperties.getDeleteRetentionPolicy(), "DeleteRetentionPolicy Days");

            // Static Website
            finalProperties.setStaticWebsite(properties.getStaticWebsite());
        } else {
            finalProperties = null;
        }
        Context finalContext = context == null ? Context.NONE : context;
        Callable<Response<Void>> operation = () -> this.azureBlobStorage.getServices()
            .setPropertiesNoCustomHeadersWithResponse(finalProperties, null, null, finalContext);
        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime -->
     * <pre>
     * System.out.printf&#40;&quot;User delegation key: %s%n&quot;,
     *     client.getUserDelegationKey&#40;delegationKeyStartTime, delegationKeyExpiryTime&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime -->
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return The user delegation key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UserDelegationKey getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry) {
        return getUserDelegationKeyWithResponse(start, expiry, null, Context.NONE).getValue();
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime-Duration-Context -->
     * <pre>
     * System.out.printf&#40;&quot;User delegation key: %s%n&quot;,
     *     client.getUserDelegationKeyWithResponse&#40;delegationKeyStartTime, delegationKeyExpiryTime, timeout, context&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime-Duration-Context -->
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the user delegation key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<UserDelegationKey> getUserDelegationKeyWithResponse(OffsetDateTime start, OffsetDateTime expiry,
        Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("expiry", expiry);
        if (start != null && !start.isBefore(expiry)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("`start` must be null or a datetime before `expiry`."));
        }
        throwOnAnonymousAccess();
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<ServicesGetUserDelegationKeyHeaders, UserDelegationKey>> operation = () ->
            this.azureBlobStorage.getServices().getUserDelegationKeyWithResponse(new KeyInfo()
                .setStart(start == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(start))
                .setExpiry(Constants.ISO_8601_UTC_DATE_FORMATTER.format(expiry)), null, null, finalContext);
        ResponseBase<ServicesGetUserDelegationKeyHeaders, UserDelegationKey> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        return new SimpleResponse<>(response, response.getValue());
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.getStatistics -->
     * <pre>
     * System.out.printf&#40;&quot;Geo-replication status: %s%n&quot;,
     *     client.getStatistics&#40;&#41;.getGeoReplication&#40;&#41;.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.getStatistics -->
     *
     * @return The storage account statistics.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobServiceStatistics getStatistics() {
        return getStatisticsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.getStatisticsWithResponse#Duration-Context -->
     * <pre>
     * System.out.printf&#40;&quot;Geo-replication status: %s%n&quot;,
     *     client.getStatisticsWithResponse&#40;timeout, context&#41;.getValue&#40;&#41;.getGeoReplication&#40;&#41;.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.getStatisticsWithResponse#Duration-Context -->
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} the storage account statistics.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobServiceStatistics> getStatisticsWithResponse(Duration timeout, Context context) {
        throwOnAnonymousAccess();
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<ServicesGetStatisticsHeaders, BlobServiceStatistics>> operation = () ->
            this.azureBlobStorage.getServices().getStatisticsWithResponse(null, null, finalContext);
        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.getAccountInfo -->
     * <pre>
     * StorageAccountInfo accountInfo = client.getAccountInfo&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Account kind: %s, SKU: %s%n&quot;, accountInfo.getAccountKind&#40;&#41;, accountInfo.getSkuName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.getAccountInfo -->
     *
     * @return The storage account info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StorageAccountInfo getAccountInfo() {
        return getAccountInfoWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the storage account info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StorageAccountInfo> getAccountInfoWithResponse(Duration timeout, Context context) {
        throwOnAnonymousAccess();
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<ServicesGetAccountInfoHeaders, Void>> operation = () ->
            this.azureBlobStorage.getServices().getAccountInfoWithResponse(finalContext);
        ResponseBase<ServicesGetAccountInfoHeaders, Void> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        ServicesGetAccountInfoHeaders hd = response.getDeserializedHeaders();
        return new SimpleResponse<>(response, new StorageAccountInfo(hd.getXMsSkuName(), hd.getXMsAccountKind(),
            hd.isXMsIsHnsEnabled()));
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
     * Checks if service client was built with credentials.
     */
    private void throwOnAnonymousAccess() {
        if (anonymousAccess) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Service client cannot be accessed without "
                + "credentials"));
        }
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p><strong>Generating an account SAS</strong></p>
     * <p>The snippet below generates an AccountSasSignatureValues object that lasts for two days and gives the user
     * read and list access to blob  and file shares.</p>
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.generateAccountSas#AccountSasSignatureValues -->
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
     * <!-- end com.azure.storage.blob.BlobServiceClient.generateAccountSas#AccountSasSignatureValues -->
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues) {
        return generateAccountSas(accountSasSignatureValues, Context.NONE);
    }

    /* TODO(gapra): REST Docs*/
    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p><strong>Generating an account SAS</strong></p>
     * <p>The snippet below generates an AccountSasSignatureValues object that lasts for two days and gives the user
     * read and list access to blob  and file shares.</p>
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.generateAccountSas#AccountSasSignatureValues-Context -->
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
     * <!-- end com.azure.storage.blob.BlobServiceClient.generateAccountSas#AccountSasSignatureValues-Context -->
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
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);    }

    /**
     * Restores a previously deleted container.
     * If the container associated with provided <code>deletedContainerName</code>
     * already exists, this call will result in a 409 (conflict).
     * This API is only functional if Container Soft Delete is enabled
     * for the storage account associated with the container.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.undeleteBlobContainer#String-String -->
     * <pre>
     * ListBlobContainersOptions listBlobContainersOptions = new ListBlobContainersOptions&#40;&#41;;
     * listBlobContainersOptions.getDetails&#40;&#41;.setRetrieveDeleted&#40;true&#41;;
     * client.listBlobContainers&#40;listBlobContainersOptions, null&#41;.forEach&#40;
     *     deletedContainer -&gt; &#123;
     *         BlobContainerClient blobContainerClient = client.undeleteBlobContainer&#40;
     *             deletedContainer.getName&#40;&#41;, deletedContainer.getVersion&#40;&#41;&#41;;
     *     &#125;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.undeleteBlobContainer#String-String -->
     *
     * @param deletedContainerName The name of the previously deleted container.
     * @param deletedContainerVersion The version of the previously deleted container.
     * @return The {@link BlobContainerClient} used to interact with the restored container.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobContainerClient undeleteBlobContainer(String deletedContainerName, String deletedContainerVersion) {
        return this.undeleteBlobContainerWithResponse(
            new UndeleteBlobContainerOptions(deletedContainerName, deletedContainerVersion), null,
            Context.NONE).getValue();
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
     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.undeleteBlobContainerWithResponse#UndeleteBlobContainerOptions-Duration-Context -->
     * <pre>
     * ListBlobContainersOptions listBlobContainersOptions = new ListBlobContainersOptions&#40;&#41;;
     * listBlobContainersOptions.getDetails&#40;&#41;.setRetrieveDeleted&#40;true&#41;;
     * client.listBlobContainers&#40;listBlobContainersOptions, null&#41;.forEach&#40;
     *     deletedContainer -&gt; &#123;
     *         BlobContainerClient blobContainerClient = client.undeleteBlobContainerWithResponse&#40;
     *             new UndeleteBlobContainerOptions&#40;deletedContainer.getName&#40;&#41;, deletedContainer.getVersion&#40;&#41;&#41;,
     *             timeout, context&#41;.getValue&#40;&#41;;
     *     &#125;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobServiceClient.undeleteBlobContainerWithResponse#UndeleteBlobContainerOptions-Duration-Context -->
     *
     * @param options {@link UndeleteBlobContainerOptions}.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link BlobContainerClient} used
     * to interact with the restored container.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobContainerClient> undeleteBlobContainerWithResponse(UndeleteBlobContainerOptions options,
        Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        boolean hasOptionalDestinationContainerName = options.getDestinationContainerName() != null;
        String finalDestinationContainerName =
            hasOptionalDestinationContainerName ? options.getDestinationContainerName()
                : options.getDeletedContainerName();
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<ContainersRestoreHeaders, Void>> operation = () ->
            this.azureBlobStorage.getContainers().restoreWithResponse(finalDestinationContainerName, null, null,
                options.getDeletedContainerName(), options.getDeletedContainerVersion(), finalContext);
        ResponseBase<ContainersRestoreHeaders, Void> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        return new SimpleResponse<>(response, getBlobContainerClient(finalDestinationContainerName));
    }

//    /**
//     * Renames an existing blob container.
//     *
//     * <p><strong>Code Samples</strong></p>
//     *
//     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.renameBlobContainer#String-String -->
//     * <!-- end com.azure.storage.blob.BlobServiceClient.renameBlobContainer#String-String -->
//     *
//     * @param sourceContainerName The current name of the container.
//     * @param destinationContainerName The new name of the container.
//     * @return A {@link BlobContainerClient} used to interact with the renamed container.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    BlobContainerClient renameBlobContainer(String sourceContainerName, String destinationContainerName) {
//        return renameBlobContainerWithResponse(sourceContainerName, new BlobContainerRenameOptions(destinationContainerName
//        ), null, Context.NONE).getValue();
//    }
//
//    /**
//     * Renames an existing blob container.
//     *
//     * <p><strong>Code Samples</strong></p>
//     *
//     * <!-- src_embed com.azure.storage.blob.BlobServiceClient.renameBlobContainerWithResponse#String-BlobContainerRenameOptions-Duration-Context -->
//     * <!-- end com.azure.storage.blob.BlobServiceClient.renameBlobContainerWithResponse#String-BlobContainerRenameOptions-Duration-Context -->
//     *
//     * @param options {@link BlobContainerRenameOptions}
//     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
//     * @param context Additional context that is passed through the Http pipeline during the service call.
//     * @return A {@link Response} whose {@link Response#getValue() value} contains a
//     * {@link BlobContainerClient} used to interact with the renamed container.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    Response<BlobContainerClient> renameBlobContainerWithResponse(String sourceContainerName,
//        BlobContainerRenameOptions options, Duration timeout, Context context) {
//        Mono<Response<BlobContainerClient>> response =
//            this.blobServiceAsyncClient.renameBlobContainerWithResponse(sourceContainerName, options, context)
//                .map(r -> new SimpleResponse<>(r, getBlobContainerClient(r.getValue().getBlobContainerName())));
//
//        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
//    }
}
