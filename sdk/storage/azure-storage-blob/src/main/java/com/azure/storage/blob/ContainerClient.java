// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
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
import reactor.core.publisher.Flux;
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
    private ContainerAsyncClient containerAsyncClient;

    public static final String ROOT_CONTAINER_NAME = ContainerAsyncClient.ROOT_CONTAINER_NAME;

    public static final String STATIC_WEBSITE_CONTAINER_NAME = ContainerAsyncClient.STATIC_WEBSITE_CONTAINER_NAME;

    public static final String LOG_CONTAINER_NAME = ContainerAsyncClient.LOG_CONTAINER_NAME;

    /**
     * Package-private constructor for use by {@link ContainerClientBuilder}.
     *
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
     * @param blobName A {@code String} representing the name of the blob.
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
     * @param blobName A {@code String} representing the name of the blob.
     * @param snapshot the snapshot identifier for the blob.
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
     * @param blobName A {@code String} representing the name of the blob.
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
     * @param blobName A {@code String} representing the name of the blob.
     * @param snapshot the snapshot identifier for the blob.
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
     * @param blobName A {@code String} representing the name of the blob.
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
     * @param blobName A {@code String} representing the name of the blob.
     * @param snapshot the snapshot identifier for the blob.
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
     *
     * @param blobName A {@code String} representing the name of the blob.
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
     * @param blobName A {@code String} representing the name of the blob.
     * @param snapshot the snapshot identifier for the blob.
     * @return A new {@link BlobClient} object which references the blob with the specified name in this container.
     */
    public BlobClient getBlobClient(String blobName, String snapshot) {
        return new BlobClient(containerAsyncClient.getBlobAsyncClient(blobName, snapshot));
    }

    /**
     * Initializes a {@link BlobServiceClient} object pointing to the storage account this container is in.
     *
     * @return A {@link BlobServiceClient} object pointing to the specified storage account
     */
    public BlobServiceClient getBlobServiceClient() {
        return new BlobServiceClient(containerAsyncClient.getBlobServiceAsyncClient());
    }

    /**
     * Gets the URL of the container represented by this client.
     *
     * @return the URL.
     */
    public URL getContainerUrl() {
        return containerAsyncClient.getContainerUrl();
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * @return true if the container exists, false if it doesn't
     */
    public Response<Boolean> exists() {
        return this.exists(null);
    }

    /**
     * Gets if the container this client represents exists in the cloud.
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return true if the container exists, false if it doesn't
     */
    public Response<Boolean> exists(Duration timeout) {
        Mono<Response<Boolean>> response = containerAsyncClient.exists();

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse create() {
        return this.create(null, null, null);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @param metadata {@link Metadata}
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse create(Metadata metadata, PublicAccessType accessType, Duration timeout) {
        Mono<VoidResponse> response = containerAsyncClient.create(metadata, accessType);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse delete() {
        return this.delete(null, null);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @param accessConditions {@link ContainerAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse delete(ContainerAccessConditions accessConditions, Duration timeout) {
        Mono<VoidResponse> response = containerAsyncClient.delete(accessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @return The container properties.
     */
    public Response<ContainerProperties> getProperties() {
        return this.getProperties(null, null);
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The container properties.
     */
    public Response<ContainerProperties> getProperties(LeaseAccessConditions leaseAccessConditions,
                                                       Duration timeout) {
        Mono<Response<ContainerProperties>> response = containerAsyncClient.getProperties(leaseAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * @param metadata {@link Metadata}
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse setMetadata(Metadata metadata) {
        return this.setMetadata(metadata, null, null);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * @param metadata {@link Metadata}
     * @param accessConditions {@link ContainerAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse setMetadata(Metadata metadata,
                                    ContainerAccessConditions accessConditions, Duration timeout) {
        Mono<VoidResponse> response = containerAsyncClient.setMetadata(metadata, accessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * @return The container access policy.
     */
    public Response<ContainerAccessPolicies> getAccessPolicy() {
        return this.getAccessPolicy(null, null);
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * @param leaseAccessConditions By setting lease access conditions, requests will fail if the provided lease does
     * not match the active lease on the blob.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The container access policy.
     */
    public Response<ContainerAccessPolicies> getAccessPolicy(LeaseAccessConditions leaseAccessConditions,
                                                             Duration timeout) {
        Mono<Response<ContainerAccessPolicies>> response = containerAsyncClient.getAccessPolicy(leaseAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link SignedIdentifier} objects that specify the permissions for the container.
     * Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse setAccessPolicy(PublicAccessType accessType,
                                        List<SignedIdentifier> identifiers) {
        return this.setAccessPolicy(accessType, identifiers, null, null);
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link SignedIdentifier} objects that specify the permissions for the container.
     * Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @param accessConditions {@link ContainerAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse setAccessPolicy(PublicAccessType accessType,
                                        List<SignedIdentifier> identifiers, ContainerAccessConditions accessConditions,
                                        Duration timeout) {
        Mono<VoidResponse> response = containerAsyncClient.setAccessPolicy(accessType, identifiers, accessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns a lazy loaded list of blobs in this container, with folder structures flattened. The returned {@link
     * Iterable} can be iterated through while new items are automatically retrieved as needed.
     *
     * <p>
     * Blob names are returned in lexicographic order.
     *
     * <p>
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @return The listed blobs, flattened.
     */
    public Iterable<BlobItem> listBlobsFlat() {
        return this.listBlobsFlat(new ListBlobsOptions(), null);
    }

    /**
     * Returns a lazy loaded list of blobs in this container, with folder structures flattened. The returned {@link
     * Iterable} can be iterated through while new items are automatically retrieved as needed.
     *
     * <p>
     * Blob names are returned in lexicographic order.
     *
     * <p>
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @param options {@link ListBlobsOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The listed blobs, flattened.
     */
    public Iterable<BlobItem> listBlobsFlat(ListBlobsOptions options, Duration timeout) {
        Flux<BlobItem> response = containerAsyncClient.listBlobsFlat(options);

        return timeout == null ? response.toIterable() : response.timeout(timeout).toIterable();
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
     * @param directory The directory to list blobs underneath
     * @return A reactive response emitting the prefixes and blobs.
     */
    public Iterable<BlobItem> listBlobsHierarchy(String directory) {
        return this.listBlobsHierarchy("/", new ListBlobsOptions().prefix(directory), null);
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
     * @param delimiter The delimiter for blob hierarchy, "/" for hierarchy based on directories
     * @param options {@link ListBlobsOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A reactive response emitting the prefixes and blobs.
     */
    public Iterable<BlobItem> listBlobsHierarchy(String delimiter, ListBlobsOptions options, Duration timeout) {
        Flux<BlobItem> response = containerAsyncClient.listBlobsHierarchy(delimiter, options);

        return timeout == null ? response.toIterable() : response.timeout(timeout).toIterable();
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * @param proposedId A {@code String} in any valid GUID format. May be null.
     * @param duration The  duration of the lease, in seconds, or negative one (-1) for a lease that never expires. A
     * non-infinite lease can be between 15 and 60 seconds.
     * @return The lease ID.
     */
    public Response<String> acquireLease(String proposedId, int duration) {
        return this.acquireLease(proposedId, duration, null, null);
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * @param proposedID A {@code String} in any valid GUID format. May be null.
     * @param duration The  duration of the lease, in seconds, or negative one (-1) for a lease that never expires. A
     * non-infinite lease can be between 15 and 60 seconds.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The lease ID.
     */
    public Response<String> acquireLease(String proposedID, int duration,
                                         ModifiedAccessConditions modifiedAccessConditions, Duration timeout) {
        Mono<Response<String>> response = containerAsyncClient
            .acquireLease(proposedID, duration, modifiedAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * @param leaseID The leaseId of the active lease on the blob.
     * @return The renewed lease ID.
     */
    public Response<String> renewLease(String leaseID) {
        return this.renewLease(leaseID, null, null);
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * @param leaseID The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The renewed lease ID.
     */
    public Response<String> renewLease(String leaseID, ModifiedAccessConditions modifiedAccessConditions,
                                       Duration timeout) {
        Mono<Response<String>> response = containerAsyncClient
            .renewLease(leaseID, modifiedAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * @param leaseID The leaseId of the active lease on the blob.
     * @return A response containing status code and HTTP headers
     */
    public VoidResponse releaseLease(String leaseID) {
        return this.releaseLease(leaseID, null, null);
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * @param leaseID The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse releaseLease(String leaseID,
                                     ModifiedAccessConditions modifiedAccessConditions, Duration timeout) {
        Mono<VoidResponse> response = containerAsyncClient
            .releaseLease(leaseID, modifiedAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * @return The remaining time in the broken lease.
     */
    public Response<Duration> breakLease() {
        return this.breakLease(null, null, null);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * @param breakPeriodInSeconds An optional {@code Integer} representing the proposed duration of seconds that the
     * lease should continue before it is broken, between 0 and 60 seconds. This break period is only used if it is
     * shorter than the time remaining on the lease. If longer, the time remaining on the lease is used. A new lease
     * will not be available before the break period has expired, but the lease may be held for longer than the break
     * period.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The remaining time in the broken lease.
     */
    public Response<Duration> breakLease(Integer breakPeriodInSeconds,
                                         ModifiedAccessConditions modifiedAccessConditions, Duration timeout) {
        Mono<Response<Duration>> response = containerAsyncClient
            .breakLease(breakPeriodInSeconds, modifiedAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * ChangeLease changes the blob's lease ID.
     *
     * @param leaseId The leaseId of the active lease on the blob.
     * @param proposedID A {@code String} in any valid GUID format.
     * @return The new lease ID.
     */
    public Response<String> changeLease(String leaseId, String proposedID) {
        return this.changeLease(leaseId, proposedID, null, null);
    }

    /**
     * ChangeLease changes the blob's lease ID.  For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure
     * Docs</a>.
     *
     * @param leaseId The leaseId of the active lease on the blob.
     * @param proposedID A {@code String} in any valid GUID format.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The new lease ID.
     */
    public Response<String> changeLease(String leaseId, String proposedID,
                                        ModifiedAccessConditions modifiedAccessConditions, Duration timeout) {
        Mono<Response<String>> response = containerAsyncClient
            .changeLease(leaseId, proposedID, modifiedAccessConditions);

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The account info.
     */
    public Response<StorageAccountInfo> getAccountInfo(Duration timeout) {
        Mono<Response<StorageAccountInfo>> response = containerAsyncClient.getAccountInfo();

        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Generates a user delegation SAS token with the specified parameters
     *
     * @param userDelegationKey The {@code UserDelegationKey} user delegation key for the SAS
     * @param accountName The {@code String} account name for the SAS
     * @param permissions The {@code ContainerSASPermissions} permission for the SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the SAS
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
     * @return A string that represents the SAS token
     */
    public String generateSAS(ContainerSASPermission permissions, OffsetDateTime expiryTime) {
        return this.containerAsyncClient.generateSAS(permissions, expiryTime);
    }

    /**
     * Generates a SAS token with the specified parameters
     *
     * @param identifier The {@code String} name of the access policy on the container this SAS references if any
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
     * @return A string that represents the SAS token
     */
    public String generateSAS(String identifier, ContainerSASPermission permissions, OffsetDateTime expiryTime,
                              OffsetDateTime startTime, String version, SASProtocol sasProtocol, IPRange ipRange, String cacheControl,
                              String contentDisposition, String contentEncoding, String contentLanguage, String contentType) {
        return this.containerAsyncClient.generateSAS(identifier, permissions, expiryTime, startTime, version,
            sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);
    }
}
