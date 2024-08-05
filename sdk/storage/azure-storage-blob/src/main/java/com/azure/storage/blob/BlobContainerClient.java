// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.SharedExecutorService;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.AzureBlobStorageImplBuilder;
import com.azure.storage.blob.implementation.accesshelpers.BlobItemConstructorProxy;
import com.azure.storage.blob.implementation.models.BlobHierarchyListSegment;
import com.azure.storage.blob.implementation.models.BlobSignedIdentifierWrapper;
import com.azure.storage.blob.implementation.models.ContainersFilterBlobsHeaders;
import com.azure.storage.blob.implementation.models.ContainersGetAccessPolicyHeaders;
import com.azure.storage.blob.implementation.models.ContainersGetAccountInfoHeaders;
import com.azure.storage.blob.implementation.models.ContainersGetPropertiesHeaders;
import com.azure.storage.blob.implementation.models.ContainersListBlobFlatSegmentHeaders;
import com.azure.storage.blob.implementation.models.ContainersListBlobHierarchySegmentHeaders;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.implementation.models.FilterBlobSegment;
import com.azure.storage.blob.implementation.models.ListBlobsFlatSegmentResponse;
import com.azure.storage.blob.implementation.models.ListBlobsHierarchySegmentResponse;
import com.azure.storage.blob.implementation.util.BlobSasImplUtil;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.BlobContainerEncryptionScope;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.models.BlobErrorCode;
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

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.storage.common.implementation.StorageImplUtils.sendRequest;

/**
 * Client to a container. It may only be instantiated through a {@link BlobContainerClientBuilder} or via the method
 * {@link BlobServiceClient#getBlobContainerClient(String)}. This class does not hold any state about a particular
 * container but is instead a convenient way of sending off appropriate requests to the resource on the service. It may
 * also be used to construct URLs to blobs.
 *
 * <p>
 * This client contains operations on a container. Operations on a blob are available on {@link BlobClient} through
 * {@link #getBlobClient(String)}, and operations on the service are available on {@link BlobServiceClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/azure/storage/blobs/storage-blobs-introduction>Azure
 * Docs</a> for more information on containers.
 */
@ServiceClient(builder = BlobContainerClientBuilder.class)
public final class BlobContainerClient {
    /**
     * Special container name for the root container in the Storage account.
     */
    public static final String ROOT_CONTAINER_NAME = BlobContainerAsyncClient.ROOT_CONTAINER_NAME;

    /**
     * Special container name for the static website container in the Storage account.
     */
    public static final String STATIC_WEBSITE_CONTAINER_NAME = BlobContainerAsyncClient.STATIC_WEBSITE_CONTAINER_NAME;

    /**
     * Special container name for the logs container in the Storage account.
     */
    public static final String LOG_CONTAINER_NAME = BlobContainerAsyncClient.LOG_CONTAINER_NAME;
    private static final ClientLogger LOGGER = new ClientLogger(BlobContainerClient.class);
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
     * @param client the async container client
     */
    BlobContainerClient(BlobContainerAsyncClient client) {
        this(client.getHttpPipeline(), client.getAccountUrl(), client.getServiceVersion(),
            client.getAccountName(), client.getBlobContainerName(), client.getCustomerProvidedKey(),
            new EncryptionScope().setEncryptionScope(client.getEncryptionScope()),
            client.getBlobContainerEncryptionScope());
    }

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
    BlobContainerClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName,
        String containerName, CpkInfo customerProvidedKey, EncryptionScope encryptionScope,
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
     * Initializes a new BlobClient object by concatenating blobName to the end of ContainerAsyncClient's URL. The new
     * BlobClient uses the same request policy pipeline as the ContainerAsyncClient.
     *
     * @param blobName A {@code String} representing the name of the blob. If the blob name contains special characters,
     *  pass in the url encoded version of the blob name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.getBlobClient#String -->
     * <pre>
     * BlobClient blobClient = client.getBlobClient&#40;blobName&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.getBlobClient#String -->
     * @return A new {@link BlobClient} object which references the blob with the specified name in this container.
     */
    public BlobClient getBlobClient(String blobName) {
        return getBlobClient(blobName, null);
    }

    /**
     * Initializes a new BlobClient object by concatenating blobName to the end of ContainerAsyncClient's URL. The new
     * BlobClient uses the same request policy pipeline as the ContainerAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.getBlobClient#String-String -->
     * <pre>
     * BlobClient blobClient = client.getBlobClient&#40;blobName, snapshot&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.getBlobClient#String-String -->
     *
     * @param blobName A {@code String} representing the name of the blob. If the blob name contains special characters,
     * pass in the url encoded version of the blob name.
     * @param snapshot the snapshot identifier for the blob.
     * @return A new {@link BlobClient} object which references the blob with the specified name in this container.
     */
    public BlobClient getBlobClient(String blobName, String snapshot) {
        return new BlobClient(new BlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(),
            getAccountName(), getBlobContainerName(), blobName, snapshot, getCustomerProvidedKey(), encryptionScope));
    }

    /**
     * Initializes a new BlobClient object by concatenating blobName to the end of ContainerAsyncClient's URL. The new
     * BlobClient uses the same request policy pipeline as the ContainerAsyncClient.
     *
     * @param blobName A {@code String} representing the name of the blob. If the blob name contains special characters,
     * pass in the url encoded version of the blob name.
     * @param versionId the version identifier for the blob, pass {@code null} to interact with the latest blob version.
     * @return A new {@link BlobClient} object which references the blob with the specified name in this container.
     */
    public BlobClient getBlobVersionClient(String blobName, String versionId) {
        return new BlobClient(new BlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(),
            getAccountName(), getBlobContainerName(), blobName, null, getCustomerProvidedKey(), encryptionScope,
            versionId));
    }

    /**
     * Get the container name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.getBlobContainerName -->
     * <pre>
     * String containerName = client.getBlobContainerName&#40;&#41;;
     * System.out.println&#40;&quot;The name of the blob is &quot; + containerName&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.getBlobContainerName -->
     *
     * @return The name of container.
     */
    public String getBlobContainerName() {
        return containerName;
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
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Get a client pointing to the account.
     *
     * @return {@link BlobServiceClient}
     */
    public BlobServiceClient getServiceClient() {
        CustomerProvidedKey encryptionKey = this.customerProvidedKey == null ? null
            : new CustomerProvidedKey(this.customerProvidedKey.getEncryptionKey());
        return new BlobServiceClientBuilder()
            .endpoint(this.getBlobContainerUrl())
            .pipeline(this.getHttpPipeline())
            .serviceVersion(this.serviceVersion)
            .blobContainerEncryptionScope(this.blobContainerEncryptionScope)
            .encryptionScope(this.getEncryptionScope())
            .customerProvidedKey(encryptionKey).buildClient();
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
     * Gets the {@link CpkInfo} associated with this client that will be passed to {@link BlobClient BlobClients} when
     * {@link #getBlobClient(String) getBlobClient} is called.
     *
     * @return the customer provided key used for encryption.
     */
    public CpkInfo getCustomerProvidedKey() {
        return customerProvidedKey;
    }

    /**
     * Gets the {@code encryption scope} used to encrypt this blob's content on the server.
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
     * Gets if the container this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.exists -->
     * <pre>
     * System.out.printf&#40;&quot;Exists? %b%n&quot;, client.exists&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.exists -->
     *
     * @return true if the container exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean exists() {
        return existsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.existsWithResponse#Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * System.out.printf&#40;&quot;Exists? %b%n&quot;, client.existsWithResponse&#40;timeout, context&#41;.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.existsWithResponse#Duration-Context -->
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return true if the container exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> existsWithResponse(Duration timeout, Context context) {
        try {
            Response<BlobContainerProperties> response = getPropertiesWithResponse(null, timeout, context);
            return new SimpleResponse<>(response, true);
        } catch (RuntimeException e) {
            if (ModelHelper.checkContainerDoesNotExistStatusCode(e) && e instanceof HttpResponseException) {
                HttpResponse response = ((HttpResponseException) e).getResponse();
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), false);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        }
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.create -->
     * <pre>
     * try &#123;
     *     client.create&#40;&#41;;
     *     System.out.printf&#40;&quot;Create completed%n&quot;&#41;;
     * &#125; catch &#40;BlobStorageException error&#41; &#123;
     *     if &#40;error.getErrorCode&#40;&#41;.equals&#40;BlobErrorCode.CONTAINER_ALREADY_EXISTS&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Can't create container. It already exists %n&quot;&#41;;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.create -->
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void create() {
        createWithResponse(null, null, null, Context.NONE);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.createWithResponse#Map-PublicAccessType-Duration-Context -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Create completed with status %d%n&quot;,
     *     client.createWithResponse&#40;metadata, PublicAccessType.CONTAINER, timeout, context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.createWithResponse#Map-PublicAccessType-Duration-Context -->
     *
     * @param metadata Metadata to associate with the container. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> createWithResponse(Map<String, String> metadata, PublicAccessType accessType,
        Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<Response<Void>> operation = () -> this.azureBlobStorage.getContainers()
            .createNoCustomHeadersWithResponse(containerName, null, metadata, accessType, null,
                blobContainerEncryptionScope, finalContext);
        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Creates a new container within a storage account if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.createIfNotExists -->
     * <pre>
     * boolean result = client.createIfNotExists&#40;&#41;;
     * System.out.println&#40;&quot;Create completed: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.createIfNotExists -->
     *
     * @return {@code true} if container is successfully created, {@code false} if container already exists.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean createIfNotExists() {
        return createIfNotExistsWithResponse(null, null, null).getValue();
    }

    /**
     * Creates a new container within a storage account if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.createIfNotExistsWithResponse#BlobContainerCreateOptions-Duration-Context -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * BlobContainerCreateOptions options = new BlobContainerCreateOptions&#40;&#41;.setMetadata&#40;metadata&#41;
     *     .setPublicAccessType&#40;PublicAccessType.CONTAINER&#41;;
     *
     * Response&lt;Boolean&gt; response = client.createIfNotExistsWithResponse&#40;options, timeout, context&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *     System.out.println&#40;&quot;Already existed.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.createIfNotExistsWithResponse#BlobContainerCreateOptions-Duration-Context -->
     *
     * @param options {@link BlobContainerCreateOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 201, a new
     * container was successfully created. If status code is 409, a container already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> createIfNotExistsWithResponse(BlobContainerCreateOptions options, Duration timeout,
        Context context) {
        BlobContainerCreateOptions finalOptions = options == null ? new BlobContainerCreateOptions() : options;
        try {
            Response<Void> response = createWithResponse(finalOptions.getMetadata(), finalOptions.getPublicAccessType(),
                timeout, context);
            return new SimpleResponse<>(response, true);
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 409 && e.getErrorCode().equals(BlobErrorCode.CONTAINER_ALREADY_EXISTS)) {
                HttpResponse res = e.getResponse();
                return new SimpleResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), false);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.delete -->
     * <pre>
     * try &#123;
     *     client.delete&#40;&#41;;
     *     System.out.printf&#40;&quot;Delete completed%n&quot;&#41;;
     * &#125; catch &#40;BlobStorageException error&#41; &#123;
     *     if &#40;error.getErrorCode&#40;&#41;.equals&#40;BlobErrorCode.CONTAINER_NOT_FOUND&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Delete failed. Container was not found %n&quot;&#41;;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.delete -->
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        deleteWithResponse(null, null, Context.NONE);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.deleteWithResponse#BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, client.deleteWithResponse&#40;
     *     requestConditions, timeout, context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.deleteWithResponse#BlobRequestConditions-Duration-Context -->
     *
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(BlobRequestConditions requestConditions, Duration timeout,
        Context context) {
        BlobRequestConditions finalRequestConditions = requestConditions == null ? new BlobRequestConditions()
            : requestConditions;
        if (!ModelHelper.validateNoETag(requestConditions)) {
            // Throwing is preferred to Mono.error because this will error out immediately instead of waiting until
            // subscription.
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("ETag access conditions are not supported for this API."));
        }
        Context finalContext = context == null ? Context.NONE : context;

        Callable<Response<Void>> operation = () -> this.azureBlobStorage.getContainers()
            .deleteNoCustomHeadersWithResponse(containerName, null, finalRequestConditions.getLeaseId(),
                finalRequestConditions.getIfModifiedSince(), finalRequestConditions.getIfUnmodifiedSince(), null,
                finalContext);

        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Marks the specified container for deletion if it exists. The container and any blobs contained within
     * it are later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.deleteIfExists -->
     * <pre>
     * boolean result = client.deleteIfExists&#40;&#41;;
     * System.out.println&#40;&quot;Delete completed: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.deleteIfExists -->
     * @return {@code true} if container is successfully deleted, {@code false} if container does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteIfExists() {
        return deleteIfExistsWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Marks the specified container for deletion if it exists. The container and any blobs contained within it
     * are later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.deleteIfExistsWithResponse#BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * Response&lt;Boolean&gt; response = client.deleteIfExistsWithResponse&#40;requestConditions, timeout, context&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.deleteIfExistsWithResponse#BlobRequestConditions-Duration-Context -->
     *
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 202, the container
     * was successfully deleted. If status code is 404, the container does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteIfExistsWithResponse(BlobRequestConditions requestConditions, Duration timeout,
        Context context) {
        try {
            Response<Void> response = this.deleteWithResponse(requestConditions, timeout, context);
            return new SimpleResponse<>(response, true);
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 404 && e.getErrorCode().equals(BlobErrorCode.CONTAINER_NOT_FOUND)) {
                HttpResponse res = e.getResponse();
                return new SimpleResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), false);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        }
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.getProperties -->
     * <pre>
     * BlobContainerProperties properties = client.getProperties&#40;&#41;;
     * System.out.printf&#40;&quot;Public Access Type: %s, Legal Hold? %b, Immutable? %b%n&quot;,
     *     properties.getBlobPublicAccess&#40;&#41;,
     *     properties.hasLegalHold&#40;&#41;,
     *     properties.hasImmutabilityPolicy&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.getProperties -->
     *
     * @return The container properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobContainerProperties getProperties() {
        return getPropertiesWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.getPropertiesWithResponse#String-Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * BlobContainerProperties properties = client.getPropertiesWithResponse&#40;leaseId, timeout, context&#41;
     *     .getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Public Access Type: %s, Legal Hold? %b, Immutable? %b%n&quot;,
     *     properties.getBlobPublicAccess&#40;&#41;,
     *     properties.hasLegalHold&#40;&#41;,
     *     properties.hasImmutabilityPolicy&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.getPropertiesWithResponse#String-Duration-Context -->
     *
     * @param leaseId The lease ID the active lease on the container must match.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The container properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobContainerProperties> getPropertiesWithResponse(String leaseId, Duration timeout,
        Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<ContainersGetPropertiesHeaders, Void>> operation = () ->
            this.azureBlobStorage.getContainers().getPropertiesWithResponse(containerName, null, leaseId, null,
                finalContext);
        ResponseBase<ContainersGetPropertiesHeaders, Void> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        ContainersGetPropertiesHeaders hd = response.getDeserializedHeaders();
        BlobContainerProperties properties = new BlobContainerProperties(hd.getXMsMeta(), hd.getETag(),
            hd.getLastModified(), hd.getXMsLeaseDuration(), hd.getXMsLeaseState(), hd.getXMsLeaseStatus(),
            hd.getXMsBlobPublicAccess(), Boolean.TRUE.equals(hd.isXMsHasImmutabilityPolicy()),
            Boolean.TRUE.equals(hd.isXMsHasLegalHold()), hd.getXMsDefaultEncryptionScope(),
            hd.isXMsDenyEncryptionScopeOverride(), hd.isXMsImmutableStorageWithVersioningEnabled());
        return new SimpleResponse<>(response, properties);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.setMetadata#Map -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * try &#123;
     *     client.setMetadata&#40;metadata&#41;;
     *     System.out.printf&#40;&quot;Set metadata completed with status %n&quot;&#41;;
     * &#125; catch &#40;UnsupportedOperationException error&#41; &#123;
     *     System.out.printf&#40;&quot;Fail while setting metadata %n&quot;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.setMetadata#Map -->
     *
     * @param metadata Metadata to associate with the container. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setMetadata(Map<String, String> metadata) {
        setMetadataWithResponse(metadata, null, null, Context.NONE);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.setMetadataWithResponse#Map-BlobRequestConditions-Duration-Context -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Set metadata completed with status %d%n&quot;,
     *     client.setMetadataWithResponse&#40;metadata, requestConditions, timeout, context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.setMetadataWithResponse#Map-BlobRequestConditions-Duration-Context -->
     * @param metadata Metadata to associate with the container. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setMetadataWithResponse(Map<String, String> metadata,
        BlobRequestConditions requestConditions, Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        BlobRequestConditions finalRequestConditions = requestConditions == null ? new BlobRequestConditions()
            : requestConditions;
        if (!ModelHelper.validateNoETag(finalRequestConditions) || finalRequestConditions.getIfUnmodifiedSince() != null) {
            // Throwing is preferred to Mono.error because this will error out immediately instead of waiting until
            // subscription.
            throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
                "If-Modified-Since is the only HTTP access condition supported for this API"));
        }
        Callable<Response<Void>> operation = () -> azureBlobStorage.getContainers().setMetadataWithResponse(
            containerName, null, finalRequestConditions.getLeaseId(), metadata,
            finalRequestConditions.getIfModifiedSince(), null, finalContext);
        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.getAccessPolicy -->
     * <pre>
     * BlobContainerAccessPolicies accessPolicies = client.getAccessPolicy&#40;&#41;;
     * System.out.printf&#40;&quot;Blob Access Type: %s%n&quot;, accessPolicies.getBlobAccessType&#40;&#41;&#41;;
     *
     * for &#40;BlobSignedIdentifier identifier : accessPolicies.getIdentifiers&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Identifier Name: %s, Permissions %s%n&quot;,
     *         identifier.getId&#40;&#41;,
     *         identifier.getAccessPolicy&#40;&#41;.getPermissions&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.getAccessPolicy -->
     *
     * @return The container access policy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlobContainerAccessPolicies getAccessPolicy() {
        return getAccessPolicyWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.getAccessPolicyWithResponse#String-Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * BlobContainerAccessPolicies accessPolicies = client.getAccessPolicyWithResponse&#40;leaseId, timeout, context&#41;
     *     .getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Blob Access Type: %s%n&quot;, accessPolicies.getBlobAccessType&#40;&#41;&#41;;
     *
     * for &#40;BlobSignedIdentifier identifier : accessPolicies.getIdentifiers&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Identifier Name: %s, Permissions %s%n&quot;,
     *         identifier.getId&#40;&#41;,
     *         identifier.getAccessPolicy&#40;&#41;.getPermissions&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.getAccessPolicyWithResponse#String-Duration-Context -->
     *
     * @param leaseId The lease ID the active lease on the container must match.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The container access policy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlobContainerAccessPolicies> getAccessPolicyWithResponse(String leaseId, Duration timeout,
        Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<ContainersGetAccessPolicyHeaders, BlobSignedIdentifierWrapper>> operation = () ->
            this.azureBlobStorage.getContainers().getAccessPolicyWithResponse(containerName, null, leaseId, null,
                finalContext);
        ResponseBase<ContainersGetAccessPolicyHeaders, BlobSignedIdentifierWrapper> response = sendRequest(operation,
            timeout, BlobStorageException.class);
        return new SimpleResponse<>(response, new BlobContainerAccessPolicies(
            response.getDeserializedHeaders().getXMsBlobPublicAccess(), response.getValue().items()));
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.setAccessPolicy#PublicAccessType-List -->
     * <pre>
     * BlobSignedIdentifier identifier = new BlobSignedIdentifier&#40;&#41;
     *     .setId&#40;&quot;name&quot;&#41;
     *     .setAccessPolicy&#40;new BlobAccessPolicy&#40;&#41;
     *         .setStartsOn&#40;OffsetDateTime.now&#40;&#41;&#41;
     *         .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;7&#41;&#41;
     *         .setPermissions&#40;&quot;permissionString&quot;&#41;&#41;;
     *
     * try &#123;
     *     client.setAccessPolicy&#40;PublicAccessType.CONTAINER, Collections.singletonList&#40;identifier&#41;&#41;;
     *     System.out.printf&#40;&quot;Set Access Policy completed %n&quot;&#41;;
     * &#125; catch &#40;UnsupportedOperationException error&#41; &#123;
     *     System.out.printf&#40;&quot;Set Access Policy completed %s%n&quot;, error&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.setAccessPolicy#PublicAccessType-List -->
     *
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link BlobSignedIdentifier} objects that specify the permissions for the container.
     * Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setAccessPolicy(PublicAccessType accessType,
        List<BlobSignedIdentifier> identifiers) {
        setAccessPolicyWithResponse(accessType, identifiers, null, null, Context.NONE);
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobRequestConditions-Duration-Context -->
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
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Set access policy completed with status %d%n&quot;,
     *     client.setAccessPolicyWithResponse&#40;PublicAccessType.CONTAINER,
     *         Collections.singletonList&#40;identifier&#41;,
     *         requestConditions,
     *         timeout,
     *         context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobRequestConditions-Duration-Context -->
     *
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link BlobSignedIdentifier} objects that specify the permissions for the container.
     * Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setAccessPolicyWithResponse(PublicAccessType accessType,
        List<BlobSignedIdentifier> identifiers, BlobRequestConditions requestConditions,
        Duration timeout, Context context) {
        BlobRequestConditions finalRequestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;

        if (!ModelHelper.validateNoETag(requestConditions)) {
            // Throwing is preferred to Mono.error because this will error out immediately instead of waiting until
            // subscription.
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("ETag access conditions are not supported for this API."));
        }
        List<BlobSignedIdentifier> finalIdentifiers = ModelHelper.truncateTimeForBlobSignedIdentifier(identifiers);
        Context finalContext = context == null ? Context.NONE : context;
        Callable<Response<Void>> operation = () -> this.azureBlobStorage.getContainers()
            .setAccessPolicyNoCustomHeadersWithResponse(containerName, null, finalRequestConditions.getLeaseId(),
                accessType, finalRequestConditions.getIfModifiedSince(),
                finalRequestConditions.getIfUnmodifiedSince(), null, finalIdentifiers, finalContext);
        return sendRequest(operation, timeout, BlobStorageException.class);
    }

    /**
     * Returns a lazy loaded list of blobs in this container, with folder structures flattened. The returned {@link
     * PagedIterable} can be consumed through while new items are automatically retrieved as needed.
     *
     * <p>
     * Blob names are returned in lexicographic order.
     *
     * <p>
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.listBlobs -->
     * <pre>
     * client.listBlobs&#40;&#41;.forEach&#40;blob -&gt;
     *     System.out.printf&#40;&quot;Name: %s, Directory? %b%n&quot;, blob.getName&#40;&#41;, blob.isPrefix&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.listBlobs -->
     *
     * @return The listed blobs, flattened.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BlobItem> listBlobs() {
        return this.listBlobs(new ListBlobsOptions(), null);
    }

    /**
     * Returns a lazy loaded list of blobs in this container, with folder structures flattened. The returned {@link
     * PagedIterable} can be consumed through while new items are automatically retrieved as needed.
     *
     * <p>
     * Blob names are returned in lexicographic order.
     *
     * <p>
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.listBlobs#ListBlobsOptions-Duration -->
     * <pre>
     * ListBlobsOptions options = new ListBlobsOptions&#40;&#41;
     *     .setPrefix&#40;&quot;prefixToMatch&quot;&#41;
     *     .setDetails&#40;new BlobListDetails&#40;&#41;
     *         .setRetrieveDeletedBlobs&#40;true&#41;
     *         .setRetrieveSnapshots&#40;true&#41;&#41;;
     *
     * client.listBlobs&#40;options, timeout&#41;.forEach&#40;blob -&gt;
     *     System.out.printf&#40;&quot;Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n&quot;,
     *         blob.getName&#40;&#41;,
     *         blob.isPrefix&#40;&#41;,
     *         blob.isDeleted&#40;&#41;,
     *         blob.getSnapshot&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.listBlobs#ListBlobsOptions-Duration -->
     *
     * @param options {@link ListBlobsOptions}. If iterating by page, the page size passed to byPage methods such as
     * {@link PagedIterable#iterableByPage(int)} will be preferred over the value set on these options.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The listed blobs, flattened.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BlobItem> listBlobs(ListBlobsOptions options, Duration timeout) {
        return this.listBlobs(options, null, timeout);
    }

    /**
     * Returns a lazy loaded list of blobs in this container, with folder structures flattened. The returned {@link
     * PagedIterable} can be consumed through while new items are automatically retrieved as needed.
     *
     * <p>
     * Blob names are returned in lexicographic order.
     *
     * <p>
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.listBlobs#ListBlobsOptions-String-Duration -->
     * <pre>
     * ListBlobsOptions options = new ListBlobsOptions&#40;&#41;
     *     .setPrefix&#40;&quot;prefixToMatch&quot;&#41;
     *     .setDetails&#40;new BlobListDetails&#40;&#41;
     *         .setRetrieveDeletedBlobs&#40;true&#41;
     *         .setRetrieveSnapshots&#40;true&#41;&#41;;
     *
     * String continuationToken = &quot;continuationToken&quot;;
     *
     * client.listBlobs&#40;options, continuationToken, timeout&#41;.forEach&#40;blob -&gt;
     *     System.out.printf&#40;&quot;Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n&quot;,
     *         blob.getName&#40;&#41;,
     *         blob.isPrefix&#40;&#41;,
     *         blob.isDeleted&#40;&#41;,
     *         blob.getSnapshot&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.listBlobs#ListBlobsOptions-String-Duration -->
     *
     * @param options {@link ListBlobsOptions}. If iterating by page, the page size passed to byPage methods such as
     * {@link PagedIterable#iterableByPage(int)} will be preferred over the value set on these options.
     * @param continuationToken Identifies the portion of the list to be returned with the next list operation.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The listed blobs, flattened.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BlobItem> listBlobs(ListBlobsOptions options, String continuationToken, Duration timeout) {
        BiFunction<String, Integer, PagedResponse<BlobItem>> retriever = (nextMarker, pageSize) -> {
            ListBlobsOptions finalOptions = options == null ? new ListBlobsOptions() : options;
            if (pageSize != null) {
                finalOptions.setMaxResultsPerPage(pageSize);
            }
            ArrayList<ListBlobsIncludeItem> include =
                finalOptions.getDetails().toList().isEmpty() ? null : finalOptions.getDetails().toList();

            Supplier<PagedResponse<BlobItem>> operation = () -> {
                ResponseBase<ContainersListBlobFlatSegmentHeaders, ListBlobsFlatSegmentResponse> response =
                    this.azureBlobStorage.getContainers().listBlobFlatSegmentWithResponse(
                        containerName, finalOptions.getPrefix(), nextMarker, finalOptions.getMaxResultsPerPage(),
                        include, null, null, Context.NONE);

                List<BlobItem> value = response.getValue().getSegment() == null ? Collections.emptyList()
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
            };
            try {
                return timeout != null ? CoreUtils.getResultWithTimeout(SharedExecutorService.getInstance()
                    .submit(operation::get), timeout) : operation.get();
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException("Failed to retrieve blobs with timeout.", e));
            }
        };
        return new PagedIterable<>(pageSize -> retriever.apply(continuationToken, pageSize), retriever);
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
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.listBlobsByHierarchy#String -->
     * <pre>
     * client.listBlobsByHierarchy&#40;&quot;directoryName&quot;&#41;.forEach&#40;blob -&gt;
     *     System.out.printf&#40;&quot;Name: %s, Directory? %b%n&quot;, blob.getName&#40;&#41;, blob.isPrefix&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.listBlobsByHierarchy#String -->
     *
     * @param directory The directory to list blobs underneath
     * @return A reactive response emitting the prefixes and blobs.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BlobItem> listBlobsByHierarchy(String directory) {
        return this.listBlobsByHierarchy("/", new ListBlobsOptions().setPrefix(directory), null);
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
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.listBlobsByHierarchy#String-ListBlobsOptions-Duration -->
     * <pre>
     * ListBlobsOptions options = new ListBlobsOptions&#40;&#41;
     *     .setPrefix&#40;&quot;directoryName&quot;&#41;
     *     .setDetails&#40;new BlobListDetails&#40;&#41;
     *         .setRetrieveDeletedBlobs&#40;true&#41;
     *         .setRetrieveSnapshots&#40;false&#41;&#41;;
     *
     * client.listBlobsByHierarchy&#40;&quot;&#47;&quot;, options, timeout&#41;.forEach&#40;blob -&gt;
     *     System.out.printf&#40;&quot;Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n&quot;,
     *         blob.getName&#40;&#41;,
     *         blob.isPrefix&#40;&#41;,
     *         blob.isDeleted&#40;&#41;,
     *         blob.getSnapshot&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.listBlobsByHierarchy#String-ListBlobsOptions-Duration -->
     *
     * @param delimiter The delimiter for blob hierarchy, "/" for hierarchy based on directories
     * @param options {@link ListBlobsOptions}. If iterating by page, the page size passed to byPage methods such as
     * {@link PagedIterable#iterableByPage(int)} will be preferred over the value set on these options.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A reactive response emitting the prefixes and blobs.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BlobItem> listBlobsByHierarchy(String delimiter, ListBlobsOptions options, Duration timeout) {
        BiFunction<String, Integer, PagedResponse<BlobItem>> func = (marker, pageSize) -> {
            ListBlobsOptions finalOptions;
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
                finalOptions = options == null ? new ListBlobsOptions() : options;
            }
            return listBlobsHierarchySegment(marker, delimiter, finalOptions, timeout);
        };
        return new PagedIterable<>(pageSize -> func.apply(null, pageSize), func);
    }

    private PagedResponse<BlobItem> listBlobsHierarchySegment(String marker, String delimiter, ListBlobsOptions options,
        Duration timeout) {
        if (options.getDetails().getRetrieveSnapshots()) {
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("Including snapshots in a hierarchical listing is not supported."));
        }
        ArrayList<ListBlobsIncludeItem> include = options.getDetails().toList().isEmpty() ? null
            : options.getDetails().toList();

        Callable<ResponseBase<ContainersListBlobHierarchySegmentHeaders, ListBlobsHierarchySegmentResponse>> operation =
            () -> azureBlobStorage.getContainers().listBlobHierarchySegmentWithResponse(containerName, delimiter,
                options.getPrefix(), marker, options.getMaxResultsPerPage(), include, null, null, Context.NONE);

        ResponseBase<ContainersListBlobHierarchySegmentHeaders, ListBlobsHierarchySegmentResponse> response =
            StorageImplUtils.sendRequest(operation, timeout, BlobStorageException.class);

        BlobHierarchyListSegment segment = response.getValue().getSegment();
        List<BlobItem> value = new ArrayList<>();
        if (segment != null) {
            segment.getBlobItems().forEach(item -> value.add(BlobItemConstructorProxy.create(item)));
            segment.getBlobPrefixes().forEach(prefix -> value.add(new BlobItem()
                .setName(ModelHelper.toBlobNameString(prefix.getName()))
                .setIsPrefix(true)));
        }

        return new PagedResponseBase<>(
            response.getRequest(), response.getStatusCode(), response.getHeaders(), value,
            response.getValue().getNextMarker(), response.getDeserializedHeaders());
    }

    /**
     * Returns a lazy loaded list of blobs in this container whose tags match the query expression. The returned
     * {@link PagedIterable} can be consumed while new items are automatically retrieved as needed. For more
     * information, including information on the query syntax, see the <a href="https://docs.microsoft.com/rest/api/storageservices/find-blobs-by-tags">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * TODO
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
        StorageImplUtils.assertNotNull("options", options);
        BiFunction<String, Integer, PagedResponse<TaggedBlobItem>> func = (marker, pageSize) -> {
            // Use pageSize if provided, otherwise use maxResultsPerPage from options
            FindBlobsOptions finalOptions = (pageSize != null)
                ? new FindBlobsOptions(options.getQuery()).setMaxResultsPerPage(pageSize) : options;

            return findBlobsByTagsHelper(finalOptions, marker, timeout, context);
        };
        return new PagedIterable<>(pageSize -> func.apply(null, pageSize), func);
    }

    private PagedResponse<TaggedBlobItem> findBlobsByTagsHelper(
        FindBlobsOptions options, String marker,
        Duration timeout, Context context) {
        Callable<ResponseBase<ContainersFilterBlobsHeaders, FilterBlobSegment>> operation = () ->
            this.azureBlobStorage.getContainers().filterBlobsWithResponse(
                containerName, null, null, options.getQuery(), marker,
                options.getMaxResultsPerPage(), null, context);

        ResponseBase<ContainersFilterBlobsHeaders, FilterBlobSegment> response =
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
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.getAccountInfo#Duration -->
     * <pre>
     * StorageAccountInfo accountInfo = client.getAccountInfo&#40;timeout&#41;;
     * System.out.printf&#40;&quot;Account Kind: %s, SKU: %s%n&quot;, accountInfo.getAccountKind&#40;&#41;, accountInfo.getSkuName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.getAccountInfo#Duration -->
     * @return The account info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StorageAccountInfo getAccountInfo(Duration timeout) {
        return getAccountInfoWithResponse(timeout, Context.NONE).getValue();
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.getAccountInfoWithResponse#Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * StorageAccountInfo accountInfo = client.getAccountInfoWithResponse&#40;timeout, context&#41;.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Account Kind: %s, SKU: %s%n&quot;, accountInfo.getAccountKind&#40;&#41;, accountInfo.getSkuName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.getAccountInfoWithResponse#Duration-Context -->
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The account info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StorageAccountInfo> getAccountInfoWithResponse(Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<ContainersGetAccountInfoHeaders, Void>> operation = () ->
            this.azureBlobStorage.getContainers().getAccountInfoWithResponse(containerName, finalContext);
        ResponseBase<ContainersGetAccountInfoHeaders, Void> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        ContainersGetAccountInfoHeaders hd = response.getDeserializedHeaders();
        return new SimpleResponse<>(response, new StorageAccountInfo(hd.getXMsSkuName(), hd.getXMsAccountKind()));
    }

    // TODO: Reintroduce this API once service starts supporting it.
//    BlobContainerClient rename(String destinationContainerName) {
//        return renameWithResponse(new BlobContainerRenameOptions(destinationContainerName
//        ), null, Context.NONE).getValue();
//    }

    // TODO: Reintroduce this API once service starts supporting it.
//    Response<BlobContainerClient> renameWithResponse(BlobContainerRenameOptions options, Duration timeout,
//        Context context) {
//        Mono<Response<BlobContainerClient>> response = this.client.renameWithResponse(options, context)
//                .map(r -> new SimpleResponse<>(r, new BlobContainerClient(r.getValue())));
//
//        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
//    }

    /**
     * Generates a user delegation SAS for the container using the specified {@link BlobServiceSasSignatureValues}.
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a user delegation SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey -->
     * <pre>
     * OffsetDateTime myExpiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * BlobContainerSasPermission myPermission = new BlobContainerSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * BlobServiceSasSignatureValues myValues = new BlobServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateUserDelegationSas&#40;values, userDelegationKey&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey -->
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link BlobServiceClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information on
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
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey-String-Context -->
     * <pre>
     * OffsetDateTime myExpiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * BlobContainerSasPermission myPermission = new BlobContainerSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * BlobServiceSasSignatureValues myValues = new BlobServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateUserDelegationSas&#40;values, userDelegationKey, accountName, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey-String-Context -->
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link BlobServiceClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information on
     * how to get a user delegation key..
     * @param accountName The account name.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues,
        UserDelegationKey userDelegationKey, String accountName, Context context) {
        return new BlobSasImplUtil(blobServiceSasSignatureValues, getBlobContainerName())
            .generateUserDelegationSas(userDelegationKey, accountName, context);
    }

    /**
     * Generates a service SAS for the container using the specified {@link BlobServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link BlobServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.generateSas#BlobServiceSasSignatureValues -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * BlobContainerSasPermission permission = new BlobContainerSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateSas&#40;values&#41;; &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * </pre>
     * <!-- end com.azure.storage.blob.BlobContainerClient.generateSas#BlobServiceSasSignatureValues -->
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
     * <!-- src_embed com.azure.storage.blob.BlobContainerClient.generateSas#BlobServiceSasSignatureValues-Context -->
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
     * <!-- end com.azure.storage.blob.BlobContainerClient.generateSas#BlobServiceSasSignatureValues-Context -->
     *
     * @param blobServiceSasSignatureValues {@link BlobServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, Context context) {
        return new BlobSasImplUtil(blobServiceSasSignatureValues, getBlobContainerName())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
    }
}
