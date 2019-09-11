// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ContainerAccessConditions;
import com.azure.storage.blob.models.ContainerAccessPolicies;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.SignedIdentifier;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Client to a container. It may only be instantiated through a {@link ContainerClientBuilder} or via the method {@link
 * BlobServiceClient#getContainerClient(String)}. This class does not hold any state about a particular container but is
 * instead a convenient way of sending off appropriate requests to the resource on the service. It may also be used to
 * construct URLs to blobs.
 *
 * <p>
 * This client contains operations on a container. Operations on a blob are available on {@link BlobClient} through
 * {@link #getBlobClient(String)}, and operations on the service are available on {@link BlobServiceClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>Azure
 * Docs</a> for more information on containers.
 */
public final class ContainerClient {
    private final ContainerAsyncClient containerAsyncClient;

    public static final String ROOT_CONTAINER_NAME = ContainerAsyncClient.ROOT_CONTAINER_NAME;

    public static final String STATIC_WEBSITE_CONTAINER_NAME = ContainerAsyncClient.STATIC_WEBSITE_CONTAINER_NAME;

    public static final String LOG_CONTAINER_NAME = ContainerAsyncClient.LOG_CONTAINER_NAME;

    /**
     * Package-private constructor for use by {@link ContainerClientBuilder}.
     * @param containerAsyncClient the async container client
     */
    ContainerClient(ContainerAsyncClient containerAsyncClient) {
        this.containerAsyncClient = containerAsyncClient;
    }

    /**
     * Creates a new {@link BlockBlobClient} object by concatenating the blobName to the end of ContainerAsyncClient's
     * URL. The new BlockBlobClient uses the same request policy pipeline as the ContainerAsyncClient. To change the
     * pipeline, create the BlockBlobClient and then call its WithPipeline method passing in the desired pipeline
     * object. Or, call this package's NewBlockBlobAsyncClient instead of calling this object's NewBlockBlobAsyncClient
     * method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getBlockBlobClient#String}
     *
     * @param blobName A {@code String} representing the name of the blob.
     *
     * @return A new {@link BlockBlobClient} object which references the blob with the specified name in this container.
     */
    public BlockBlobClient getBlockBlobClient(String blobName) {
        return new BlockBlobClient(containerAsyncClient.getBlockBlobAsyncClient(blobName));
    }

    /**
     * Creates a new {@link BlockBlobClient} object by concatenating the blobName to the end of ContainerAsyncClient's
     * URL. The new BlockBlobClient uses the same request policy pipeline as the ContainerAsyncClient. To change the
     * pipeline, create the BlockBlobClient and then call its WithPipeline method passing in the desired pipeline
     * object. Or, call this package's NewBlockBlobAsyncClient instead of calling this object's NewBlockBlobAsyncClient
     * method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getBlockBlobClient#String-String}
     *
     * @param blobName A {@code String} representing the name of the blob.
     * @param snapshot the snapshot identifier for the blob.
     *
     * @return A new {@link BlockBlobClient} object which references the blob with the specified name in this container.
     */
    public BlockBlobClient getBlockBlobClient(String blobName, String snapshot) {
        return new BlockBlobClient(containerAsyncClient.getBlockBlobAsyncClient(blobName, snapshot));
    }

    /**
     * Creates creates a new PageBlobClient object by concatenating blobName to the end of ContainerAsyncClient's URL.
     * The new PageBlobClient uses the same request policy pipeline as the ContainerAsyncClient. To change the pipeline,
     * create the PageBlobClient and then call its WithPipeline method passing in the desired pipeline object. Or, call
     * this package's NewPageBlobAsyncClient instead of calling this object's NewPageBlobAsyncClient method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getPageBlobClient#String}
     *
     * @param blobName A {@code String} representing the name of the blob.
     *
     * @return A new {@link PageBlobClient} object which references the blob with the specified name in this container.
     */
    public PageBlobClient getPageBlobClient(String blobName) {
        return new PageBlobClient(containerAsyncClient.getPageBlobAsyncClient(blobName));
    }

    /**
     * Creates creates a new PageBlobClient object by concatenating blobName to the end of ContainerAsyncClient's URL.
     * The new PageBlobClient uses the same request policy pipeline as the ContainerAsyncClient. To change the pipeline,
     * create the PageBlobClient and then call its WithPipeline method passing in the desired pipeline object. Or, call
     * this package's NewPageBlobAsyncClient instead of calling this object's NewPageBlobAsyncClient method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getPageBlobClient#String-String}
     *
     * @param blobName A {@code String} representing the name of the blob.
     * @param snapshot the snapshot identifier for the blob.
     *
     * @return A new {@link PageBlobClient} object which references the blob with the specified name in this container.
     */
    public PageBlobClient getPageBlobClient(String blobName, String snapshot) {
        return new PageBlobClient(containerAsyncClient.getPageBlobAsyncClient(blobName, snapshot));
    }

    /**
     * Creates creates a new AppendBlobClient object by concatenating blobName to the end of ContainerAsyncClient's URL.
     * The new AppendBlobClient uses the same request policy pipeline as the ContainerAsyncClient. To change the
     * pipeline, create the AppendBlobClient and then call its WithPipeline method passing in the desired pipeline
     * object. Or, call this package's NewAppendBlobAsyncClient instead of calling this object's
     * NewAppendBlobAsyncClient method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getAppendBlobClient#String}
     *
     * @param blobName A {@code String} representing the name of the blob.
     *
     * @return A new {@link AppendBlobClient} object which references the blob with the specified name in this
     * container.
     */
    public AppendBlobClient getAppendBlobClient(String blobName) {
        return new AppendBlobClient(containerAsyncClient.getAppendBlobAsyncClient(blobName));
    }

    /**
     * Creates creates a new AppendBlobClient object by concatenating blobName to the end of ContainerAsyncClient's URL.
     * The new AppendBlobClient uses the same request policy pipeline as the ContainerAsyncClient. To change the
     * pipeline, create the AppendBlobClient and then call its WithPipeline method passing in the desired pipeline
     * object. Or, call this package's NewAppendBlobAsyncClient instead of calling this object's
     * NewAppendBlobAsyncClient method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getAppendBlobClient#String-String}
     *
     * @param blobName A {@code String} representing the name of the blob.
     * @param snapshot the snapshot identifier for the blob.
     *
     * @return A new {@link AppendBlobClient} object which references the blob with the specified name in this
     * container.
     */
    public AppendBlobClient getAppendBlobClient(String blobName, String snapshot) {
        return new AppendBlobClient(containerAsyncClient.getAppendBlobAsyncClient(blobName, snapshot));
    }

    /**
     * Initializes a new BlobClient object by concatenating blobName to the end of ContainerAsyncClient's URL. The new
     * BlobClient uses the same request policy pipeline as the ContainerAsyncClient. To change the pipeline, create the
     * BlobClient and then call its WithPipeline method passing in the desired pipeline object. Or, call this package's
     * getBlobAsyncClient instead of calling this object's getBlobAsyncClient method.
     * @param blobName A {@code String} representing the name of the blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getBlobClient#String}
     *
     * @return A new {@link BlobClient} object which references the blob with the specified name in this container.
     */
    public BlobClient getBlobClient(String blobName) {
        return new BlobClient(containerAsyncClient.getBlobAsyncClient(blobName));
    }

    /**
     * Initializes a new BlobClient object by concatenating blobName to the end of ContainerAsyncClient's URL. The new
     * BlobClient uses the same request policy pipeline as the ContainerAsyncClient. To change the pipeline, create the
     * BlobClient and then call its WithPipeline method passing in the desired pipeline object. Or, call this package's
     * getBlobAsyncClient instead of calling this object's getBlobAsyncClient method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getBlobClient#String-String}
     *
     * @param blobName A {@code String} representing the name of the blob.
     * @param snapshot the snapshot identifier for the blob.
     *
     * @return A new {@link BlobClient} object which references the blob with the specified name in this container.
     */
    public BlobClient getBlobClient(String blobName, String snapshot) {
        return new BlobClient(containerAsyncClient.getBlobAsyncClient(blobName, snapshot));
    }

    /**
     * Initializes a {@link BlobServiceClient} object pointing to the storage account this container is in.
     * @return A {@link BlobServiceClient} object pointing to the specified storage account
     */
    public BlobServiceClient getBlobServiceClient() {
        return new BlobServiceClient(containerAsyncClient.getBlobServiceAsyncClient());
    }

    /**
     * Gets the URL of the container represented by this client.
     * @return the URL.
     */
    public URL getContainerUrl() {
        return containerAsyncClient.getContainerUrl();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return containerAsyncClient.getHttpPipeline();
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.exists}
     *
     * @return true if the container exists, false if it doesn't
     */
    public Boolean exists() {
        return existsWithResponse(null, Context.NONE).value();
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.existsWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return true if the container exists, false if it doesn't
     */
    public Response<Boolean> existsWithResponse(Duration timeout, Context context) {
        Mono<Response<Boolean>> response = containerAsyncClient.existsWithResponse(context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.create}
     *
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
     * {@codesnippet com.azure.storage.blob.ContainerClient.createWithResponse#Metadata-PublicAccessType-Duration-Context}
     *
     * @param metadata {@link Metadata}
     * @param accessType Specifies how the data in this container is available to the public. See the
     *                   x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse createWithResponse(Metadata metadata, PublicAccessType accessType, Duration timeout, Context context) {
        Mono<VoidResponse> response = containerAsyncClient.createWithResponse(metadata, accessType, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.delete}
     */
    public void setDelete() {
        deleteWithResponse(null, null, Context.NONE);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.deleteWithResponse#ContainerAccessConditions-Duration-Context}
     *
     * @param accessConditions {@link ContainerAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse deleteWithResponse(ContainerAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<VoidResponse> response = containerAsyncClient.deleteWithResponse(accessConditions, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getProperties}
     *
     * @return The container properties.
     */
    public ContainerProperties getProperties() {
        return getPropertiesWithResponse(null, null, Context.NONE).value();
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getPropertiesWithResponse#LeaseAccessConditions-Duration-Context}
     *
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     *                              not match the active lease on the blob.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The container properties.
     */
    public Response<ContainerProperties> getPropertiesWithResponse(LeaseAccessConditions leaseAccessConditions,
                                                                   Duration timeout, Context context) {
        Mono<Response<ContainerProperties>> response = containerAsyncClient.getPropertiesWithResponse(leaseAccessConditions, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.setMetadata#Metadata}
     *
     * @param metadata {@link Metadata}
     *
     */
    public void setMetadata(Metadata metadata) {
        setMetadataWithResponse(metadata, null, null, Context.NONE);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.setMetadataWithResponse#Metadata-ContainerAccessConditions-Duration-Context}
     *
     * @param metadata {@link Metadata}
     * @param accessConditions {@link ContainerAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse setMetadataWithResponse(Metadata metadata,
                                                ContainerAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<VoidResponse> response = containerAsyncClient.setMetadataWithResponse(metadata, accessConditions, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getAccessPolicy}
     *
     * @return The container access policy.
     */
    public ContainerAccessPolicies getAccessPolicy() {
        return getAccessPolicyWithResponse(null, null, Context.NONE).value();
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getAccessPolicyWithResponse#LeaseAccessConditions-Duration-Context}
     *
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     *                              not match the active lease on the blob.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The container access policy.
     */
    public Response<ContainerAccessPolicies> getAccessPolicyWithResponse(LeaseAccessConditions leaseAccessConditions,
                                                                         Duration timeout, Context context) {
        Mono<Response<ContainerAccessPolicies>> response = containerAsyncClient.getAccessPolicyWithResponse(leaseAccessConditions, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.setAccessPolicy#PublicAccessType-List}
     *
     * @param accessType Specifies how the data in this container is available to the public. See the
     *                    x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link SignedIdentifier} objects that specify the permissions for the container.
     *                    Please see
     *                    <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     *                    for more information. Passing null will clear all access policies.
     */
    public void setAccessPolicy(PublicAccessType accessType,
                                List<SignedIdentifier> identifiers) {
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
     * {@codesnippet com.azure.storage.blob.ContainerClient.setAccessPolicyWithResponse#PublicAccessType-List-ContainerAccessConditions-Duration-Context}
     *
     * @param accessType Specifies how the data in this container is available to the public. See the
     *                         x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link SignedIdentifier} objects that specify the permissions for the container.
     *                         Please see
     *                         <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     *                         for more information. Passing null will clear all access policies.
     * @param accessConditions {@link ContainerAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse setAccessPolicyWithResponse(PublicAccessType accessType,
                                                    List<SignedIdentifier> identifiers, ContainerAccessConditions accessConditions,
                                                    Duration timeout, Context context) {
        Mono<VoidResponse> response = containerAsyncClient.setAccessPolicyWithResponse(accessType, identifiers, accessConditions, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
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
     * {@codesnippet com.azure.storage.blob.ContainerClient.listBlobsFlat}
     *
     * @return The listed blobs, flattened.
     */
    public PagedIterable<BlobItem> listBlobsFlat() {
        return this.listBlobsFlat(new ListBlobsOptions(), null);
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
     * {@codesnippet com.azure.storage.blob.ContainerClient.listBlobsFlat#ListBlobsOptions-Duration}
     *
     * @param options {@link ListBlobsOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return The listed blobs, flattened.
     */
    public PagedIterable<BlobItem> listBlobsFlat(ListBlobsOptions options, Duration timeout) {
        return new PagedIterable<>(containerAsyncClient.listBlobsFlatWithOptionalTimeout(options, timeout));
    }

    /**
     * Returns a reactive Publisher emitting all the blobs and directories (prefixes) under the given directory
     * (prefix). Directories will have {@link BlobItem#getIsPrefix()} set to true.
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
     * {@codesnippet com.azure.storage.blob.ContainerClient.listBlobsHierarchy#String}
     *
     * @param directory The directory to list blobs underneath
     *
     * @return A reactive response emitting the prefixes and blobs.
     */
    public PagedIterable<BlobItem> listBlobsHierarchy(String directory) {
        return this.listBlobsHierarchy("/", new ListBlobsOptions().setPrefix(directory), null);
    }

    /**
     * Returns a reactive Publisher emitting all the blobs and prefixes (directories) under the given prefix
     * (directory). Directories will have {@link BlobItem#getIsPrefix()} set to true.
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
     * {@codesnippet com.azure.storage.blob.ContainerClient.listBlobsHierarchy#String-ListBlobsOptions-Duration}
     *
     * @param delimiter The delimiter for blob hierarchy, "/" for hierarchy based on directories
     * @param options {@link ListBlobsOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return A reactive response emitting the prefixes and blobs.
     */
    public PagedIterable<BlobItem> listBlobsHierarchy(String delimiter, ListBlobsOptions options, Duration timeout) {
        return new PagedIterable<>(containerAsyncClient.listBlobsHierarchyWithOptionalTimeout(delimiter, options, timeout));
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.acquireLease#String-int}
     *
     * @param proposedId A {@code String} in any valid GUID format. May be null.
     * @param duration The  duration of the lease, in seconds, or negative one (-1) for a lease that never expires. A
     *                   non-infinite lease can be between 15 and 60 seconds.
     *
     * @return The lease ID.
     */
    public String acquireLease(String proposedId, int duration) {
        return acquireLeaseWithResponse(proposedId, duration, null, null, Context.NONE).value();
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.acquireLeaseWithResponse#String-int-ModifiedAccessConditions-Duration-Context}
     *
     * @param proposedID A {@code String} in any valid GUID format. May be null.
     * @param duration The  duration of the lease, in seconds, or negative one (-1) for a lease that never expires. A
     *                                 non-infinite lease can be between 15 and 60 seconds.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     *                                 LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     *                                 request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The lease ID.
     */
    public Response<String> acquireLeaseWithResponse(String proposedID, int duration,
                                                     ModifiedAccessConditions modifiedAccessConditions, Duration timeout, Context context) {
        Mono<Response<String>> response = containerAsyncClient
            .acquireLeaseWithResponse(proposedID, duration, modifiedAccessConditions, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.renewLease#String}
     *
     * @param leaseID The leaseId of the active lease on the blob.
     *
     * @return The renewed lease ID.
     */
    public String renewLease(String leaseID) {
        return renewLease(leaseID, null, null);
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.renewLease#String-ModifiedAccessConditions-Duration}
     *
     * @param leaseID The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     *                                 LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     *                                 request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return The renewed lease ID.
     */
    public String renewLease(String leaseID, ModifiedAccessConditions modifiedAccessConditions,
                             Duration timeout) {
        return renewLeaseWithResponse(leaseID, modifiedAccessConditions, timeout, Context.NONE).value();
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     * {@codesnippet com.azure.storage.blob.ContainerClient.renewLeaseWithResponse#String-ModifiedAccessConditions-Duration-Context}
     *
     * @param leaseID The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     *                                 LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     *                                 request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The renewed lease ID.
     */
    public Response<String> renewLeaseWithResponse(String leaseID, ModifiedAccessConditions modifiedAccessConditions,
                                                   Duration timeout, Context context) {
        Mono<Response<String>> response = containerAsyncClient
            .renewLeaseWithResponse(leaseID, modifiedAccessConditions, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.releaseLease#String}
     *
     * @param leaseID The leaseId of the active lease on the blob.
     *
     */
    public void releaseLease(String leaseID) {
        releaseLeaseWithResponse(leaseID, null, null, Context.NONE);
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.releaseLeaseWithResponse#String-ModifiedAccessConditions-Duration-Context}
     *
     * @param leaseID The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     *                                 LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     *                                 request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse releaseLeaseWithResponse(String leaseID,
                                                 ModifiedAccessConditions modifiedAccessConditions, Duration timeout, Context context) {
        Mono<VoidResponse> response = containerAsyncClient
            .releaseLeaseWithResponse(leaseID, modifiedAccessConditions, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.breakLease}
     *
     * @return The remaining time in the broken lease.
     */
    public Duration breakLease() {
        return breakLeaseWithResponse(null, null, null, Context.NONE).value();
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.breakLeaseWithResponse#Integer-ModifiedAccessConditions-Duration-Context}
     *
     * @param breakPeriodInSeconds An optional {@code Integer} representing the proposed duration of seconds that the
     *                                 lease should continue before it is broken, between 0 and 60 seconds. This break period is only used if it is
     *                                 shorter than the time remaining on the lease. If longer, the time remaining on the lease is used. A new lease
     *                                 will not be available before the break period has expired, but the lease may be held for longer than the break
     *                                 period.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     *                                 LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     *                                 request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The remaining time in the broken lease.
     */
    public Response<Duration> breakLeaseWithResponse(Integer breakPeriodInSeconds,
                                                     ModifiedAccessConditions modifiedAccessConditions, Duration timeout, Context context) {
        Mono<Response<Duration>> response = containerAsyncClient
            .breakLeaseWithResponse(breakPeriodInSeconds, modifiedAccessConditions, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * ChangeLease changes the blob's lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.changeLease#String-String}
     *
     * @param leaseId The leaseId of the active lease on the blob.
     * @param proposedID A {@code String} in any valid GUID format.
     *
     * @return The new lease ID.
     */
    public String changeLease(String leaseId, String proposedID) {
        return changeLeaseWithResponse(leaseId, proposedID, null, null, Context.NONE).value();
    }

    /**
     * ChangeLease changes the blob's lease ID.  For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.changeLeaseWithResponse#String-String-ModifiedAccessConditions-Duration-Context}
     *
     * @param leaseId The leaseId of the active lease on the blob.
     * @param proposedID A {@code String} in any valid GUID format.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     *                                 LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     *                                 request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The new lease ID.
     */
    public Response<String> changeLeaseWithResponse(String leaseId, String proposedID,
                                                    ModifiedAccessConditions modifiedAccessConditions, Duration timeout, Context context) {
        Mono<Response<String>> response = containerAsyncClient
            .changeLeaseWithResponse(leaseId, proposedID, modifiedAccessConditions, context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getAccountInfo#Duration}
     *
     * @return The account info.
     */
    public StorageAccountInfo getAccountInfo(Duration timeout) {
        return getAccountInfoWithResponse(timeout, Context.NONE).value();
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.getAccountInfoWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The account info.
     */
    public Response<StorageAccountInfo> getAccountInfoWithResponse(Duration timeout, Context context) {
        Mono<Response<StorageAccountInfo>> response = containerAsyncClient.getAccountInfoWithResponse(context);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Generates a user delegation SAS token with the specified parameters
     *
     * @param userDelegationKey The {@code UserDelegationKey} user delegation key for the SAS
     * @param accountName The {@code String} account name for the SAS
     * @param permissions The {@code ContainerSASPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     *
     * @return A string that represents the SAS token
     */
    public String generateUserDelegationSAS(UserDelegationKey userDelegationKey, String accountName,
        ContainerSASPermission permissions, OffsetDateTime expiryTime) {
        return this.containerAsyncClient.generateUserDelegationSAS(userDelegationKey, accountName, permissions,
            expiryTime);
    }

    /**
     * Generates a user delegation SAS token with the specified parameters
     *
     * @param userDelegationKey The {@code UserDelegationKey} user delegation key for the SAS
     * @param accountName The {@code String} account name for the SAS
     * @param permissions The {@code ContainerSASPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     *
     * @return A string that represents the SAS token
     */
    public String generateUserDelegationSAS(UserDelegationKey userDelegationKey, String accountName,
        ContainerSASPermission permissions, OffsetDateTime expiryTime, OffsetDateTime startTime, String version,
        SASProtocol sasProtocol, IPRange ipRange) {
        return this.containerAsyncClient.generateUserDelegationSAS(userDelegationKey, accountName, permissions,
            expiryTime, startTime, version, sasProtocol, ipRange);
    }

    /**
     * Generates a user delegation SAS token with the specified parameters
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.generateUserDelegationSAS#UserDelegationKey-String-ContainerSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-user-delegation-sas">Azure Docs</a></p>
     *
     * @param userDelegationKey The {@code UserDelegationKey} user delegation key for the SAS
     * @param accountName The {@code String} account name for the SAS
     * @param permissions The {@code ContainerSASPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     * @param cacheControl An optional {@code String} cache-control header for the SAS.
     * @param contentDisposition An optional {@code String} content-disposition header for the SAS.
     * @param contentEncoding An optional {@code String} content-encoding header for the SAS.
     * @param contentLanguage An optional {@code String} content-language header for the SAS.
     * @param contentType An optional {@code String} content-type header for the SAS.
     *
     * @return A string that represents the SAS token
     */
    public String generateUserDelegationSAS(UserDelegationKey userDelegationKey, String accountName,
        ContainerSASPermission permissions, OffsetDateTime expiryTime, OffsetDateTime startTime, String version,
        SASProtocol sasProtocol, IPRange ipRange, String cacheControl, String contentDisposition,
        String contentEncoding, String contentLanguage, String contentType) {
        return this.containerAsyncClient.generateUserDelegationSAS(userDelegationKey, accountName, permissions,
            expiryTime, startTime, version, sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding,
            contentLanguage, contentType);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param permissions The {@code ContainerSASPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     *
     * @return A string that represents the SAS token
     */
    public String generateSAS(ContainerSASPermission permissions, OffsetDateTime expiryTime) {
        return this.containerAsyncClient.generateSAS(permissions, expiryTime);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param identifier The {@code String} name of the access policy on the container this SAS references if any
     *
     * @return A string that represents the SAS token
     */
    public String generateSAS(String identifier) {
        return this.containerAsyncClient.generateSAS(identifier);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param identifier The {@code String} name of the access policy on the container this SAS references if any
     * @param permissions The {@code ContainerSASPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     *
     * @return A string that represents the SAS token
     */
    public String generateSAS(String identifier, ContainerSASPermission permissions, OffsetDateTime expiryTime,
        OffsetDateTime startTime, String version, SASProtocol sasProtocol, IPRange ipRange) {
        return this.containerAsyncClient.generateSAS(identifier, permissions, expiryTime, startTime, version,
            sasProtocol, ipRange);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.ContainerClient.generateSAS#String-ContainerSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-service-sas">Azure Docs</a></p>
     *
     * @param identifier The {@code String} name of the access policy on the container this SAS references if any
     * @param permissions The {@code ContainerSASPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
     * @param startTime An optional {@code OffsetDateTime} start time for the SAS
     * @param version An optional {@code String} version for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     * @param cacheControl An optional {@code String} cache-control header for the SAS.
     * @param contentDisposition An optional {@code String} content-disposition header for the SAS.
     * @param contentEncoding An optional {@code String} content-encoding header for the SAS.
     * @param contentLanguage An optional {@code String} content-language header for the SAS.
     * @param contentType An optional {@code String} content-type header for the SAS.
     *
     * @return A string that represents the SAS token
     */
    public String generateSAS(String identifier, ContainerSASPermission permissions, OffsetDateTime expiryTime,
        OffsetDateTime startTime, String version, SASProtocol sasProtocol, IPRange ipRange, String cacheControl,
        String contentDisposition, String contentEncoding, String contentLanguage, String contentType) {
        return this.containerAsyncClient.generateSAS(identifier, permissions, expiryTime, startTime, version,
            sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);
    }
}
