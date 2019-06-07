// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Blob;
import java.util.List;

import static com.azure.storage.blob.Utility.safeURLEncode;

/**
 * Represents a URL to a container. It may be obtained by direct construction or via the create method on a
 * {@link BlobServiceAsyncRawClient} object. This class does not hold any state about a particular blob but is instead a convenient way
 * of sending off appropriate requests to the resource on the service. It may also be used to construct URLs to blobs.
 * Please refer to the
 * <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>Azure Docs</a>
 * for more information on containers.
 */
public final class ContainerAsyncClient {

    ContainerAsyncRawClient containerAsyncRawClient;

    public static final String ROOT_CONTAINER_NAME = "$root";

    public static final String STATIC_WEBSITE_CONTAINER_NAME = "$web";

    public static final String LOG_CONTAINER_NAME = "$logs";


    /**
     * Creates a {@code ContainerAsyncClient} object pointing to the account specified by the URL and using the provided
     * pipeline to make HTTP requests.
     */
    ContainerAsyncClient(AzureBlobStorageBuilder azureBlobStorageBuilder) {
        containerAsyncRawClient = new ContainerAsyncRawClient(azureBlobStorageBuilder);
    }

    /**
     * Creates a new {@link BlockBlobAsyncRawClient} object by concatenating the blobName to the end of
     * ContainerAsyncClient's URL. The new BlockBlobAsyncRawClient uses the same request policy pipeline as the ContainerAsyncClient.
     * To change the pipeline, create the BlockBlobAsyncRawClient and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's NewBlockBlobAsyncClient instead of calling this object's
     * NewBlockBlobAsyncClient method.
     *
     * @param blobName
     *         A {@code String} representing the name of the blob.
     *
     * @return A new {@link BlockBlobAsyncRawClient} object which references the blob with the specified name in this container.
     */
    public BlockBlobAsyncClient createBlockBlobAsyncClient(String blobName) {
        AzureBlobStorageImpl newAzureBlobStorage = containerAsyncRawClient.createNewAzureBlobStorage(blobName);
        return new BlockBlobAsyncClient(newAzureBlobStorage);
    }

    /**
     * Creates creates a new PageBlobAsyncRawClient object by concatenating blobName to the end of
     * ContainerAsyncClient's URL. The new PageBlobAsyncRawClient uses the same request policy pipeline as the ContainerAsyncClient.
     * To change the pipeline, create the PageBlobAsyncRawClient and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's NewPageBlobAsyncClient instead of calling this object's
     * NewPageBlobAsyncClient method.
     *
     * @param blobName
     *         A {@code String} representing the name of the blob.
     *
     * @return A new {@link PageBlobAsyncRawClient} object which references the blob with the specified name in this container.
     */
    public PageBlobAsyncClient createPageBlobAsyncClient(String blobName) {
        AzureBlobStorageImpl newAzureBlobStorage = containerAsyncRawClient.createNewAzureBlobStorage(blobName);
        return new PageBlobAsyncClient(newAzureBlobStorage);
    }

    /**
     * Creates creates a new AppendBlobAsyncRawClient object by concatenating blobName to the end of
     * ContainerAsyncClient's URL. The new AppendBlobAsyncRawClient uses the same request policy pipeline as the ContainerAsyncClient.
     * To change the pipeline, create the AppendBlobAsyncRawClient and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's NewAppendBlobAsyncClient instead of calling this object's
     * NewAppendBlobAsyncClient method.
     *
     * @param blobName
     *         A {@code String} representing the name of the blob.
     *
     * @return A new {@link AppendBlobAsyncRawClient} object which references the blob with the specified name in this container.
     */
    public AppendBlobAsyncClient createAppendBlobAsyncClient(String blobName) {
        AzureBlobStorageImpl newAzureBlobStorage = containerAsyncRawClient.createNewAzureBlobStorage(blobName);
        return new AppendBlobAsyncClient(newAzureBlobStorage);
    }

    /**
     * Creates a new BlobAsyncRawClient object by concatenating blobName to the end of
     * ContainerAsyncClient's URL. The new BlobAsyncRawClient uses the same request policy pipeline as the ContainerAsyncClient.
     * To change the pipeline, create the BlobAsyncRawClient and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's createBlobAsyncClient instead of calling this object's
     * createBlobAsyncClient method.
     *
     * @param blobName
     *         A {@code String} representing the name of the blob.
     *
     * @return A new {@link BlobAsyncRawClient} object which references the blob with the specified name in this container.
     */
    public BlobAsyncClient createBlobAsyncClient(String blobName) {
        AzureBlobStorageImpl newAzureBlobStorage = containerAsyncRawClient.createNewAzureBlobStorage(blobName);
        return new BlobAsyncClient(newAzureBlobStorage);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_basic "Sample code for ContainerAsyncClient.create")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> create() {
        return this.create(null, null, null);
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
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_basic "Sample code for ContainerAsyncClient.create")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> create(Metadata metadata, PublicAccessType accessType, Context context) {
        return containerAsyncRawClient
            .create(metadata, accessType, context)
            .then();
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later
     * deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_basic "Sample code for ContainerAsyncClient.delete")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> delete() {
        return this.delete(null, null);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later
     * deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @param accessConditions
     *         {@link ContainerAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_basic "Sample code for ContainerAsyncClient.delete")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> delete(ContainerAccessConditions accessConditions, Context context) {
        return containerAsyncRawClient
            .delete(accessConditions, context)
            .then();
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_basic "Sample code for ContainerAsyncClient.getProperties")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainerGetPropertiesHeaders> getProperties() {
        return this.getProperties(null, null);
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_basic "Sample code for ContainerAsyncClient.getProperties")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainerGetPropertiesHeaders> getProperties(LeaseAccessConditions leaseAccessConditions,
            Context context) {
        return containerAsyncRawClient
            .getProperties(leaseAccessConditions, context)
            .map(ResponseBase::deserializedHeaders);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_basic "Sample code for ContainerAsyncClient.setMetadata")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> setMetadata(Metadata metadata) {
        return this.setMetadata(metadata, null, null);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * @param metadata
     *         {@link Metadata}
     * @param accessConditions
     *         {@link ContainerAccessConditions}
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_basic "Sample code for ContainerAsyncClient.setMetadata")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> setMetadata(Metadata metadata,
            ContainerAccessConditions accessConditions, Context context) {
        return containerAsyncRawClient
            .setMetadata(metadata, accessConditions, context)
            .then();
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_policy "Sample code for ContainerAsyncClient.getAccessPolicy")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainerGetAccessPolicyHeaders> getAccessPolicy() {
        return this.getAccessPolicy(null, null);
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * @param leaseAccessConditions
     *         By setting lease access conditions, requests will fail if the provided lease does not match the active
     *         lease on the blob.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_policy "Sample code for ContainerAsyncClient.getAccessPolicy")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainerGetAccessPolicyHeaders> getAccessPolicy(LeaseAccessConditions leaseAccessConditions,
            Context context) {
        return containerAsyncRawClient
            .getAccessPolicy(leaseAccessConditions, context)
            .map(ResponseBase::deserializedHeaders);
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
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_policy "Sample code for ContainerAsyncClient.setAccessPolicy")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> setAccessPolicy(PublicAccessType accessType,
            List<SignedIdentifier> identifiers) {
        return this.setAccessPolicy(accessType, identifiers, null, null);
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
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_policy "Sample code for ContainerAsyncClient.setAccessPolicy")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<Void> setAccessPolicy(PublicAccessType accessType,
                                      List<SignedIdentifier> identifiers, ContainerAccessConditions accessConditions, Context context) {
        return containerAsyncRawClient
            .setAccessPolicy(accessType, identifiers, accessConditions, context)
            .then();
    }

    // TODO: figure out if this is meant to stay private or change to public
    private boolean validateNoEtag(ModifiedAccessConditions modifiedAccessConditions) {
        if (modifiedAccessConditions == null) {
            return true;
        }
        return modifiedAccessConditions.ifMatch() == null && modifiedAccessConditions.ifNoneMatch() == null;
    }

    // TODO: Lease methods not in the skeleton
    /**
     * Acquires a lease on the container for delete operations. The lease duration must be between 15 to
     * 60 seconds, or infinite (-1). For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerAsyncClient.acquireLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param proposedId
     *      A {@code String} in any valid GUID format.
     * @param duration
     *         The duration of the lease, in seconds, or negative one (-1) for a lease that never expires.
     *         A non-infinite lease can be between 15 and 60 seconds.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerAsyncClient.acquireLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainersAcquireLeaseResponse> acquireLease(String proposedId, int duration) {
        return containerAsyncRawClient.acquireLease(proposedId, duration, null, null);
    }

    /**
     * Acquires a lease on the container for delete operations. The lease duration must be between 15 to
     * 60 seconds, or infinite (-1). For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @param proposedID
     *         A {@code String} in any valid GUID format.
     * @param duration
     *         The duration of the lease, in seconds, or negative one (-1) for a lease that never expires.
     *         A non-infinite lease can be between 15 and 60 seconds.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerAsyncClient.acquireLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainersAcquireLeaseResponse> acquireLease(String proposedID, int duration,
            ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return containerAsyncRawClient.acquireLease(proposedID, duration, modifiedAccessConditions, context);
    }

    /**
     * Renews the container's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @param leaseID
     *         The leaseId of the active lease on the container.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerAsyncClient.renewLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainersRenewLeaseResponse> renewLease(String leaseID) {
        return containerAsyncRawClient.renewLease(leaseID, null, null);
    }

    /**
     * Renews the container's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @param leaseID
     *         The leaseId of the active lease on the container.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerAsyncClient.renewLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainersRenewLeaseResponse> renewLease(String leaseID,
            ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return containerAsyncRawClient.renewLease(leaseID, modifiedAccessConditions, context);
    }

    /**
     * Releases the container's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @param leaseID
     *         The leaseId of the active lease on the container.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerAsyncClient.releaseLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainersReleaseLeaseResponse> releaseLease(String leaseID) {
        return containerAsyncRawClient.releaseLease(leaseID, null, null);
    }

    /**
     * Releases the container's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @param leaseID
     *         The leaseId of the active lease on the container.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerAsyncClient.releaseLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainersReleaseLeaseResponse> releaseLease(String leaseID,
            ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return containerAsyncRawClient.releaseLease(leaseID, modifiedAccessConditions, context);
    }

    /**
     * Breaks the container's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerAsyncClient.breakLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @return Emits the successful response.
     */
    public Mono<ContainersBreakLeaseResponse> breakLease() {
        return containerAsyncRawClient.breakLease(null, null, null);
    }

    /**
     * Breaks the container's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @param breakPeriodInSeconds
     *         An optional {@code Integer} representing the proposed duration of seconds that the lease should continue
     *         before it is broken, between 0 and 60 seconds. This break period is only used if it is shorter than the time
     *         remaining on the lease. If longer, the time remaining on the lease is used. A new lease will not be
     *         available before the break period has expired, but the lease may be held for longer than the break period.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerAsyncClient.breakLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainersBreakLeaseResponse> breakLease(Integer breakPeriodInSeconds,
            ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return containerAsyncRawClient.breakLease(breakPeriodInSeconds, modifiedAccessConditions, context);
    }

    /**
     * Changes the container's leaseAccessConditions. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @param leaseID
     *         The leaseId of the active lease on the container.
     * @param proposedID
     *         A {@code String} in any valid GUID format.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerAsyncClient.changeLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainersChangeLeaseResponse> changeLease(String leaseID, String proposedID) {
        return containerAsyncRawClient.changeLease(leaseID, proposedID, null, null);
    }

    /**
     * Changes the container's leaseAccessConditions. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @param leaseID
     *         The leaseId of the active lease on the container.
     * @param proposedID
     *         A {@code String} in any valid GUID format.
     * @param modifiedAccessConditions
     *         Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used
     *         to construct conditions related to when the blob was changed relative to the given request. The request
     *         will fail if the specified condition is not satisfied.
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerAsyncClient.changeLease")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainersChangeLeaseResponse> changeLease(String leaseID, String proposedID,
            ModifiedAccessConditions modifiedAccessConditions, Context context) {
        return containerAsyncRawClient.changeLease(leaseID, proposedID, modifiedAccessConditions, context);
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
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_flat "Sample code for ContainerAsyncClient.listBlobsFlatSegment")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_flat_helper "helper code for ContainerAsyncClient.listBlobsFlatSegment")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Flux<BlobItem> listBlobsFlatSegment(ListBlobsOptions options) {
        return this.listBlobsFlatSegment(options, null);
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
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_flat "Sample code for ContainerAsyncClient.listBlobsFlatSegment")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_flat_helper "helper code for ContainerAsyncClient.listBlobsFlatSegment")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */

    public Flux<BlobItem> listBlobsFlatSegment(ListBlobsOptions options, Context context) {
        return containerAsyncRawClient
            .listBlobsFlatSegment(null, options, context)
            .flatMapMany(response -> listBlobsFlatSegmentHelper(response.value().marker(), options, context, response));
    }

    private Flux<BlobItem> listBlobsFlatSegmentHelper(String marker, ListBlobsOptions options,
                                                      Context context, ContainersListBlobFlatSegmentResponse response){
        Flux<BlobItem> result = Flux.fromIterable(response.value().segment().blobItems());

        if (response.value().nextMarker() != null) {
            // Recursively add the continuation items to the observable.
            result = result.concatWith(containerAsyncRawClient.listBlobsFlatSegment(marker, options,
                context)
                .flatMapMany((r) ->
                    listBlobsFlatSegmentHelper(response.value().nextMarker(), options, context, r)));
        }

        return result;
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
    public Flux<BlobHierarchyListSegment> listBlobsHierarchySegment(String marker, String delimiter,
            ListBlobsOptions options) {
        return this.listBlobsHierarchySegment(marker, delimiter, options, null);
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
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
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
    public Flux<BlobHierarchyListSegment> listBlobsHierarchySegment(String marker, String delimiter,
            ListBlobsOptions options, Context context) {
        return containerAsyncRawClient
            .listBlobsHierarchySegment(marker, delimiter, options, context)
            .flatMapMany();
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=account_info "Sample code for ContainerAsyncClient.getAccountInfo")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainerGetAccountInfoHeaders> getAccountInfo() {
        return this.getAccountInfo(null);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * @param context
     *         {@code Context} offers a means of passing arbitrary data (key/value pairs) to an
     *         {@link com.azure.core.http.HttpPipeline}'s policy objects. Most applications do not need to pass
     *         arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}. Each context object is
     *         immutable. The {@code withContext} with data method creates a new {@code Context} object that refers to its
     *         parent, forming a linked list.
     *
     * @return Emits the successful response.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=account_info "Sample code for ContainerAsyncClient.getAccountInfo")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainerGetAccountInfoHeaders> getAccountInfo(Context context) {
        return containerAsyncRawClient
            .getAccountInfo(context)
            .map(ResponseBase::deserializedHeaders);
    }
}
