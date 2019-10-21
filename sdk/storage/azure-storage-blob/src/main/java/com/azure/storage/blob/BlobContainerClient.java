// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobContainerAccessConditions;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

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
 * Please refer to the <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>Azure
 * Docs</a> for more information on containers.
 */
@ServiceClient(builder = BlobContainerClientBuilder.class)
public final class BlobContainerClient {
    private final BlobContainerAsyncClient blobContainerAsyncClient;

    public static final String ROOT_CONTAINER_NAME = BlobContainerAsyncClient.ROOT_CONTAINER_NAME;

    public static final String STATIC_WEBSITE_CONTAINER_NAME = BlobContainerAsyncClient.STATIC_WEBSITE_CONTAINER_NAME;

    public static final String LOG_CONTAINER_NAME = BlobContainerAsyncClient.LOG_CONTAINER_NAME;

    /**
     * Package-private constructor for use by {@link BlobContainerClientBuilder}.
     *
     * @param blobContainerAsyncClient the async container client
     */
    BlobContainerClient(BlobContainerAsyncClient blobContainerAsyncClient) {
        this.blobContainerAsyncClient = blobContainerAsyncClient;
    }


    /**
     * Initializes a new BlobClient object by concatenating blobName to the end of ContainerAsyncClient's URL. The new
     * BlobClient uses the same request policy pipeline as the ContainerAsyncClient. To change the pipeline, create the
     * BlobClient and then call its WithPipeline method passing in the desired pipeline object. Or, call this package's
     * getBlobAsyncClient instead of calling this object's getBlobAsyncClient method.
     *
     * @param blobName A {@code String} representing the name of the blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.getBlobClient#String}
     * @return A new {@link BlobClient} object which references the blob with the specified name in this container.
     */
    public BlobClient getBlobClient(String blobName) {
        return new BlobClient(blobContainerAsyncClient.getBlobAsyncClient(blobName));
    }

    /**
     * Initializes a new BlobClient object by concatenating blobName to the end of ContainerAsyncClient's URL. The new
     * BlobClient uses the same request policy pipeline as the ContainerAsyncClient. To change the pipeline, create the
     * BlobClient and then call its WithPipeline method passing in the desired pipeline object. Or, call this package's
     * getBlobAsyncClient instead of calling this object's getBlobAsyncClient method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.getBlobClient#String-String}
     *
     * @param blobName A {@code String} representing the name of the blob.
     * @param snapshot the snapshot identifier for the blob.
     * @return A new {@link BlobClient} object which references the blob with the specified name in this container.
     */
    public BlobClient getBlobClient(String blobName, String snapshot) {
        return new BlobClient(blobContainerAsyncClient.getBlobAsyncClient(blobName, snapshot));
    }

    /**
     * Get the container name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.getBlobContainerName}
     *
     * @return The name of container.
     */
    public String getBlobContainerName() {
        return this.blobContainerAsyncClient.getBlobContainerName();
    }

    /**
     * Gets the URL of the container represented by this client.
     *
     * @return the URL.
     */
    public String getBlobContainerUrl() {
        return blobContainerAsyncClient.getBlobContainerUrl();
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.blobContainerAsyncClient.getAccountName();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public BlobServiceVersion getServiceVersion() {
        return this.blobContainerAsyncClient.getServiceVersion();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return blobContainerAsyncClient.getHttpPipeline();
    }

    /**
     * Gets the {@link CpkInfo} associated with this client that will be passed to {@link BlobClient BlobClients} when
     * {@link #getBlobClient(String) getBlobClient} is called.
     *
     * @return the customer provided key used for encryption.
     */
    public CpkInfo getCustomerProvidedKey() {
        return blobContainerAsyncClient.getCustomerProvidedKey();
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.exists}
     *
     * @return true if the container exists, false if it doesn't
     */
    public boolean exists() {
        return existsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.existsWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return true if the container exists, false if it doesn't
     */
    public Response<Boolean> existsWithResponse(Duration timeout, Context context) {
        Mono<Response<Boolean>> response = blobContainerAsyncClient.existsWithResponse(context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.create}
     */
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
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.createWithResponse#Map-PublicAccessType-Duration-Context}
     *
     * @param metadata Metadata to associate with the container.
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> createWithResponse(Map<String, String> metadata, PublicAccessType accessType,
        Duration timeout, Context context) {
        Mono<Response<Void>> response = blobContainerAsyncClient.createWithResponse(metadata, accessType, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.delete}
     */
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
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.deleteWithResponse#BlobContainerAccessConditions-Duration-Context}
     *
     * @param accessConditions {@link BlobContainerAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> deleteWithResponse(BlobContainerAccessConditions accessConditions, Duration timeout,
        Context context) {
        Mono<Response<Void>> response = blobContainerAsyncClient.deleteWithResponse(accessConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.getProperties}
     *
     * @return The container properties.
     */
    public BlobContainerProperties getProperties() {
        return getPropertiesWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.getPropertiesWithResponse#LeaseAccessConditions-Duration-Context}
     *
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The container properties.
     */
    public Response<BlobContainerProperties> getPropertiesWithResponse(LeaseAccessConditions leaseAccessConditions,
        Duration timeout, Context context) {
        Mono<Response<BlobContainerProperties>> response = blobContainerAsyncClient
            .getPropertiesWithResponse(leaseAccessConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.setMetadata#Map}
     *
     * @param metadata Metadata to associate with the container.
     */
    public void setMetadata(Map<String, String> metadata) {
        setMetadataWithResponse(metadata, null, null, Context.NONE);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.setMetadataWithResponse#Map-BlobContainerAccessConditions-Duration-Context}
     * @param metadata Metadata to associate with the container.
     * @param accessConditions {@link BlobContainerAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> setMetadataWithResponse(Map<String, String> metadata,
        BlobContainerAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<Response<Void>> response = blobContainerAsyncClient.setMetadataWithResponse(metadata, accessConditions,
            context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.getAccessPolicy}
     *
     * @return The container access policy.
     */
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
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.getAccessPolicyWithResponse#LeaseAccessConditions-Duration-Context}
     *
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The container access policy.
     */
    public Response<BlobContainerAccessPolicies> getAccessPolicyWithResponse(
        LeaseAccessConditions leaseAccessConditions,
        Duration timeout, Context context) {
        Mono<Response<BlobContainerAccessPolicies>> response = blobContainerAsyncClient
            .getAccessPolicyWithResponse(leaseAccessConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.setAccessPolicy#PublicAccessType-List}
     *
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link BlobSignedIdentifier} objects that specify the permissions for the container.
     * Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     */
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
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobContainerAccessConditions-Duration-Context}
     *
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link BlobSignedIdentifier} objects that specify the permissions for the container.
     * Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @param accessConditions {@link BlobContainerAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> setAccessPolicyWithResponse(PublicAccessType accessType,
        List<BlobSignedIdentifier> identifiers, BlobContainerAccessConditions accessConditions,
        Duration timeout, Context context) {
        Mono<Response<Void>> response = blobContainerAsyncClient
            .setAccessPolicyWithResponse(accessType, identifiers, accessConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
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
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.listBlobs}
     *
     * @return The listed blobs, flattened.
     */
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
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.listBlobs#ListBlobsOptions-Duration}
     *
     * @param options {@link ListBlobsOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The listed blobs, flattened.
     */
    public PagedIterable<BlobItem> listBlobs(ListBlobsOptions options, Duration timeout) {
        return new PagedIterable<>(blobContainerAsyncClient.listBlobsFlatWithOptionalTimeout(options, timeout));
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
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.listBlobsByHierarchy#String}
     *
     * @param directory The directory to list blobs underneath
     * @return A reactive response emitting the prefixes and blobs.
     */
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
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.listBlobsByHierarchy#String-ListBlobsOptions-Duration}
     *
     * @param delimiter The delimiter for blob hierarchy, "/" for hierarchy based on directories
     * @param options {@link ListBlobsOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A reactive response emitting the prefixes and blobs.
     */
    public PagedIterable<BlobItem> listBlobsByHierarchy(String delimiter, ListBlobsOptions options, Duration timeout) {
        return new PagedIterable<>(blobContainerAsyncClient
            .listBlobsHierarchyWithOptionalTimeout(delimiter, options, timeout));
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.getAccountInfo#Duration}
     * @return The account info.
     */
    public StorageAccountInfo getAccountInfo(Duration timeout) {
        return getAccountInfoWithResponse(timeout, Context.NONE).getValue();
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobContainerClient.getAccountInfoWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The account info.
     */
    public Response<StorageAccountInfo> getAccountInfoWithResponse(Duration timeout, Context context) {
        Mono<Response<StorageAccountInfo>> response = blobContainerAsyncClient.getAccountInfoWithResponse(context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }
}
