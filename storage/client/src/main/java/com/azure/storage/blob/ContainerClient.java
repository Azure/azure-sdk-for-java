// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ContainerGetAccessPolicyHeaders;
import com.azure.storage.blob.models.ContainerGetAccountInfoHeaders;
import com.azure.storage.blob.models.ContainerGetPropertiesHeaders;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.SignedIdentifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

/**
 * Client to a container. It may be obtained through a {@link ContainerClientBuilder} or via the method
 * {@link BlobServiceClient#createContainerClient(String)}. This class does not hold any
 * state about a particular blob but is instead a convenient way of sending off appropriate requests to
 * the resource on the service. It may also be used to construct URLs to blobs. Please refer to the
 * <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>Azure Docs</a>
 * for more information on containers.
 */
public final class ContainerClient {

    private ContainerAsyncClient containerAsyncClient;
    private ContainerClientBuilder builder;

    public static final String ROOT_CONTAINER_NAME = "$root";

    public static final String STATIC_WEBSITE_CONTAINER_NAME = "$web";

    public static final String LOG_CONTAINER_NAME = "$logs";

    /**
     * Package-private constructor for use by {@link ContainerClientBuilder}.
     * @param builder the container client builder
     */
    ContainerClient(ContainerClientBuilder builder) {
        this.builder = builder;
        this.containerAsyncClient  = new ContainerAsyncClient(builder);
    }

    /**
     * @return a new client {@link ContainerClientBuilder} instance.
     */
    public static ContainerClientBuilder containerClientBuilder() {
        return new ContainerClientBuilder();
    }

    /**
     * Creates a new {@link BlockBlobClient} object by concatenating the blobName to the end of
     * ContainerAsyncClient's URL. The new BlockBlobClient uses the same request policy pipeline as the ContainerAsyncClient.
     * To change the pipeline, create the BlockBlobClient and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's NewBlockBlobAsyncClient instead of calling this object's
     * NewBlockBlobAsyncClient method.
     *
     * @param blobName
     *         A {@code String} representing the name of the blob.
     *
     * @return A new {@link BlockBlobClient} object which references the blob with the specified name in this container.
     */
    public BlockBlobClient createBlockBlobClient(String blobName) {
        try {
            return new BlockBlobClient(this.builder.copyBuilder().endpoint(Utility.appendToURLPath(new URL(builder.endpoint()), blobName).toString()).buildImpl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates creates a new PageBlobClient object by concatenating blobName to the end of
     * ContainerAsyncClient's URL. The new PageBlobClient uses the same request policy pipeline as the ContainerAsyncClient.
     * To change the pipeline, create the PageBlobClient and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's NewPageBlobAsyncClient instead of calling this object's
     * NewPageBlobAsyncClient method.
     *
     * @param blobName
     *         A {@code String} representing the name of the blob.
     *
     * @return A new {@link PageBlobClient} object which references the blob with the specified name in this container.
     */
    public PageBlobClient createPageBlobClient(String blobName) {
        try {
            return new PageBlobClient(this.builder.copyBuilder().endpoint(Utility.appendToURLPath(new URL(builder.endpoint()), blobName).toString()).buildImpl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates creates a new AppendBlobClient object by concatenating blobName to the end of
     * ContainerAsyncClient's URL. The new AppendBlobClient uses the same request policy pipeline as the ContainerAsyncClient.
     * To change the pipeline, create the AppendBlobClient and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's NewAppendBlobAsyncClient instead of calling this object's
     * NewAppendBlobAsyncClient method.
     *
     * @param blobName
     *         A {@code String} representing the name of the blob.
     *
     * @return A new {@link AppendBlobClient} object which references the blob with the specified name in this container.
     */
    public AppendBlobClient createAppendBlobClient(String blobName) {
        try {
            return new AppendBlobClient(this.builder.copyBuilder().endpoint(Utility.appendToURLPath(new URL(builder.endpoint()), blobName).toString()).buildImpl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new BlobClient object by concatenating blobName to the end of
     * ContainerAsyncClient's URL. The new BlobClient uses the same request policy pipeline as the ContainerAsyncClient.
     * To change the pipeline, create the BlobClient and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's createBlobAsyncClient instead of calling this object's
     * createBlobAsyncClient method.
     *
     * @param blobName
     *         A {@code String} representing the name of the blob.
     *
     * @return A new {@link BlobClient} object which references the blob with the specified name in this container.
     */
    public BlobClient createBlobClient(String blobName) {
        try {
            return new BlobClient(this.builder.copyBuilder().endpoint(Utility.appendToURLPath(new URL(builder.endpoint()), blobName).toString()).buildImpl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     */
    public void create() {
        this.create(null, null, null, null);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     * @param accessType
     *         Specifies how the data in this container is available to the public. See the x-ms-blob-public-access header
     *         in the Azure Docs for more information. Pass null for no public access.
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     */
    public void create(Metadata metadata, PublicAccessType accessType, Duration timeout, Context context) {
        Mono<Void> response = containerAsyncClient.create(metadata, accessType, context);

        if (timeout == null) {
            response.block();
        } else {
            response.block(timeout);
        }
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later
     * deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     */
    public void delete() {
        this.delete(null, null, null);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later
     * deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @param accessConditions
     *         {@link ContainerAccessConditions}
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     */
    public void delete(ContainerAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<Void> response = containerAsyncClient.delete(accessConditions, context);

        if (timeout == null) {
            response.block();
        } else {
            response.block(timeout);
        }
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @return
     *      The container properties.
     */
    public ContainerGetPropertiesHeaders getProperties() {
        return this.getProperties(null, null, null);
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      The container properties.
     */
    public ContainerGetPropertiesHeaders getProperties(LeaseAccessConditions leaseAccessConditions,
            Duration timeout, Context context) {
        Mono<ContainerGetPropertiesHeaders> response = containerAsyncClient.getProperties(leaseAccessConditions, context);

        return timeout == null
            ? response.block()
            : response.block(timeout);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     */
    public void setMetadata(Metadata metadata) {
        this.setMetadata(metadata, null, null, null);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link ContainerAccessConditions}
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     */
    public void setMetadata(Metadata metadata,
            ContainerAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<Void> response = containerAsyncClient.setMetadata(metadata, accessConditions, context);

        if (timeout == null) {
            response.block();
        } else {
            response.block(timeout);
        }
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * @return
     *      The container access policy.
     */
    public ContainerGetAccessPolicyHeaders getAccessPolicy() {
        return this.getAccessPolicy(null, null, null);
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      The container access policy.
     */
    public ContainerGetAccessPolicyHeaders getAccessPolicy(LeaseAccessConditions leaseAccessConditions,
            Duration timeout, Context context) {
        Mono<ContainerGetAccessPolicyHeaders> response = containerAsyncClient.getAccessPolicy(leaseAccessConditions, context);

        return timeout == null
            ? response.block()
            : response.block(timeout);
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * @param accessType
     *         Specifies how the data in this container is available to the public. See the x-ms-blob-public-access header
     *         in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers
     *         A list of {@link SignedIdentifier} objects that specify the permissions for the container. Please see
     *         <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     *         for more information. Passing null will clear all access policies.
     */
    public void setAccessPolicy(PublicAccessType accessType,
            List<SignedIdentifier> identifiers) {
        this.setAccessPolicy(accessType, identifiers, null, null,null);
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * @param accessType
     *         Specifies how the data in this container is available to the public. See the x-ms-blob-public-access header
     *         in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers
     *         A list of {@link SignedIdentifier} objects that specify the permissions for the container. Please see
     *         <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     *         for more information. Passing null will clear all access policies.
     * @param accessConditions
     *         {@link ContainerAccessConditions}
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     */
    public void setAccessPolicy(PublicAccessType accessType,
                                      List<SignedIdentifier> identifiers, ContainerAccessConditions accessConditions,
                                Duration timeout, Context context) {
        Mono<Void> response = containerAsyncClient.setAccessPolicy(accessType, identifiers, accessConditions, context);

        if (timeout == null) {
            response.block();
        } else {
            response.block(timeout);
        }
    }

    /**
     * Returns a single segment of blobs starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @param options
     *         {@link ListBlobsOptions}
     *
     * @return
     *      The listed blobs, flattened.
     */
    public Iterable<BlobItem> listBlobsFlat(ListBlobsOptions options) {
        return this.listBlobsFlat(options, null, null);
    }

    /**
     * Returns a single segment of blobs starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @param options
     *         {@link ListBlobsOptions}
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      The listed blobs, flattened.
     */
    public Iterable<BlobItem> listBlobsFlat(ListBlobsOptions options, Duration timeout, Context context) {
        Flux<BlobItem> response = containerAsyncClient.listBlobsFlat(options, context);

        return timeout == null ?
            response.toIterable():
            response.timeout(timeout).toIterable();
    }

    /**
     * Returns a single segment of blobs and blob prefixes starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @param marker
     *         Identifies the portion of the list to be returned with the next list operation.
     *         This value is returned in the response of a previous list operation as the
     *         ListBlobsHierarchySegmentResponse.body().nextMarker(). Set to null to list the first segment.
     * @param delimiter
     *         The operation returns a BlobPrefix element in the response body that acts as a placeholder for all blobs
     *         whose names begin with the same substring up to the appearance of the delimiter character. The delimiter may
     *         be a single character or a string.
     * @param options
     *         {@link ListBlobsOptions}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_hierarchy "Sample code for ContainerAsyncClient.listBlobsHierarchySegment")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_hierarchy_helper "helper code for ContainerAsyncClient.listBlobsHierarchySegment")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
//    public Iterable<BlobHierarchyListSegment> listBlobsHierarchySegment(String marker, String delimiter,
//            ListBlobsOptions options) {
//        return this.listBlobsHierarchySegment(marker, delimiter, options, null, null);
//    }

    /**
     * Returns a single segment of blobs and blob prefixes starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @param marker
     *         Identifies the portion of the list to be returned with the next list operation.
     *         This value is returned in the response of a previous list operation as the
     *         ListBlobsHierarchySegmentResponse.body().nextMarker(). Set to null to list the first segment.
     * @param delimiter
     *         The operation returns a BlobPrefix element in the response body that acts as a placeholder for all blobs
     *         whose names begin with the same substring up to the appearance of the delimiter character. The delimiter may
     *         be a single character or a string.
     * @param options
     *         {@link ListBlobsOptions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_hierarchy "Sample code for ContainerAsyncClient.listBlobsHierarchySegment")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_hierarchy_helper "helper code for ContainerAsyncClient.listBlobsHierarchySegment")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
//    public Iterable<BlobHierarchyListSegment> listBlobsHierarchySegment(String marker, String delimiter,
//            ListBlobsOptions options, Duration timeout, Context context) {
//        Flux<BlobHierarchyListSegment> response = containerAsyncClient.listBlobsHierarchySegment(marker, delimiter, options, context);
//
//        return timeout == null ?
//            response.toIterable():
//            response.timeout(timeout).toIterable();
//    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * @param proposedId
     *      A {@code String} in any valid GUID format. May be null.
     * @param duration
     *         The  duration of the lease, in seconds, or negative one (-1) for a lease that
     *         never expires. A non-infinite lease can be between 15 and 60 seconds.
     *
     * @return
     *      The lease ID.
     */
    public String acquireLease(String proposedId, int duration) {
        return this.acquireLease(proposedId, duration, null, null);
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * @param proposedID
     *         A {@code String} in any valid GUID format. May be null.
     * @param duration
     *         The  duration of the lease, in seconds, or negative one (-1) for a lease that
     *         never expires. A non-infinite lease can be between 15 and 60 seconds.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return
     *      The lease ID.
     */
    public String acquireLease(String proposedID, int duration,
        ModifiedAccessConditions modifiedAccessConditions, Duration timeout) {
        Mono<String> response = containerAsyncClient
            .acquireLease(proposedID, duration, modifiedAccessConditions, null /*context*/);

        return timeout == null
            ? response.block()
            : response.block(timeout);
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     *
     * @return
     *      The renewed lease ID.
     */
    public String renewLease(String leaseID) {
        return this.renewLease(leaseID, null, null);
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return
     *      The renewed lease ID.
     */
    public String renewLease(String leaseID, ModifiedAccessConditions modifiedAccessConditions,
        Duration timeout) {
        Mono<String> response = containerAsyncClient
            .renewLease(leaseID, modifiedAccessConditions, null /*context*/);

        return timeout == null
            ? response.block()
            : response.block(timeout);
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     */
    public void releaseLease(String leaseID) {
        this.releaseLease(leaseID, null, null);
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * @param leaseID
     *         The leaseId of the active lease on the blob.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     */
    public void releaseLease(String leaseID,
        ModifiedAccessConditions modifiedAccessConditions, Duration timeout) {
        Mono<Void> response = containerAsyncClient
            .releaseLease(leaseID, modifiedAccessConditions, null /*context*/);

        if (timeout == null) {
            response.block();
        } else {
            response.block(timeout);
        }
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * @return
     *      The remaining time in the broken lease in seconds.
     */
    public int breakLease() {
        return this.breakLease(null, null, null);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * @param breakPeriodInSeconds
     *         An optional {@code Integer} representing the proposed duration of seconds that the lease should continue
     *         before it is broken, between 0 and 60 seconds. This break period is only used if it is shorter than the
     *         time remaining on the lease. If longer, the time remaining on the lease is used. A new lease will not be
     *         available before the break period has expired, but the lease may be held for longer than the break
     *         period.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return
     *      The remaining time in the broken lease in seconds.
     */
    public int breakLease(Integer breakPeriodInSeconds,
        ModifiedAccessConditions modifiedAccessConditions, Duration timeout) {
        Mono<Integer> response = containerAsyncClient
            .breakLease(breakPeriodInSeconds, modifiedAccessConditions, null /*context*/);

        return timeout == null
            ? response.block()
            : response.block(timeout);
    }

    /**
     * ChangeLease changes the blob's lease ID.
     *
     * @param leaseId
     *         The leaseId of the active lease on the blob.
     * @param proposedID
     *         A {@code String} in any valid GUID format.
     *
     * @return
     *      The new lease ID.
     */
    public String changeLease(String leaseId, String proposedID) {
        return this.changeLease(leaseId, proposedID, null, null);
    }

    /**
     * ChangeLease changes the blob's lease ID.  For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">Azure Docs</a>.
     *
     * @param leaseId
     *         The leaseId of the active lease on the blob.
     * @param proposedID
     *         A {@code String} in any valid GUID format.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     *
     * @return The new lease ID.
     */
    public String changeLease(String leaseId, String proposedID,
        ModifiedAccessConditions modifiedAccessConditions, Duration timeout) {
        Mono<String> response = containerAsyncClient
            .changeLease(leaseId, proposedID, modifiedAccessConditions, null /*context*/);

        return timeout == null
            ? response.block()
            : response.block(timeout);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @return
     *      The account info.
     */
    public ContainerGetAccountInfoHeaders getAccountInfo() {
        return this.getAccountInfo(null, null);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param timeout
     *         An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return
     *      The account info.
     */
    public ContainerGetAccountInfoHeaders getAccountInfo(Duration timeout, Context context) {
        Mono<ContainerGetAccountInfoHeaders> response = containerAsyncClient.getAccountInfo(context);

        return timeout == null ?
            response.block():
            response.block(timeout);
    }
}
