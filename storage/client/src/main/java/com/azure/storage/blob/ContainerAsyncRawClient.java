// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.Context;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.ContainersAcquireLeaseResponse;
import com.azure.storage.blob.models.ContainersBreakLeaseResponse;
import com.azure.storage.blob.models.ContainersChangeLeaseResponse;
import com.azure.storage.blob.models.ContainersCreateResponse;
import com.azure.storage.blob.models.ContainersDeleteResponse;
import com.azure.storage.blob.models.ContainersGetAccessPolicyResponse;
import com.azure.storage.blob.models.ContainersGetAccountInfoResponse;
import com.azure.storage.blob.models.ContainersGetPropertiesResponse;
import com.azure.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.storage.blob.models.ContainersListBlobHierarchySegmentResponse;
import com.azure.storage.blob.models.ContainersReleaseLeaseResponse;
import com.azure.storage.blob.models.ContainersRenewLeaseResponse;
import com.azure.storage.blob.models.ContainersSetAccessPolicyResponse;
import com.azure.storage.blob.models.ContainersSetMetadataResponse;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.SignedIdentifier;
import reactor.core.publisher.Mono;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.azure.storage.blob.Utility.postProcessResponse;

/**
 * Represents a URL to a container. It may be obtained by direct construction or via the create method on a
 * {@link BlobServiceAsyncRawClient} object. This class does not hold any state about a particular blob but is instead a convenient way
 * of sending off appropriate requests to the resource on the service. It may also be used to construct URLs to blobs.
 * Please refer to the
 * <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>Azure Docs</a>
 * for more information on containers.
 */
final class ContainerAsyncRawClient {

    public static final String ROOT_CONTAINER_NAME = "$root";

    public static final String STATIC_WEBSITE_CONTAINER_NAME = "$web";

    public static final String LOG_CONTAINER_NAME = "$logs";

    AzureBlobStorageImpl azureBlobStorage;

    /**
     * Creates a {@code ContainerAsyncClient} object pointing to the account specified by the URL and using the provided
     * pipeline to make HTTP requests.
     */
    ContainerAsyncRawClient(AzureBlobStorageImpl azureBlobStorage) {
        this.azureBlobStorage = azureBlobStorage;
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
    public Mono<ContainersCreateResponse> create() {
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
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
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
    public Mono<ContainersCreateResponse> create(Metadata metadata, PublicAccessType accessType, Context context) {
        metadata = metadata == null ? new Metadata() : metadata;
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.azureBlobStorage.containers().createWithRestResponseAsync(
                null, null, metadata, accessType, null, context));

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
    public Mono<ContainersDeleteResponse> delete() {
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
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
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
    public Mono<ContainersDeleteResponse> delete(ContainerAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new ContainerAccessConditions() : accessConditions;
        context = context == null ? Context.NONE : context;

        if (!validateNoEtag(accessConditions.modifiedAccessConditions())) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new UnsupportedOperationException("ETag access conditions are not supported for this API.");
        }

        return postProcessResponse(this.azureBlobStorage.containers()
                .deleteWithRestResponseAsync(null, null, null,
                    accessConditions.leaseAccessConditions(), accessConditions.modifiedAccessConditions(), context));
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
    public Mono<ContainersGetPropertiesResponse> getProperties() {
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
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
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
    public Mono<ContainersGetPropertiesResponse> getProperties(LeaseAccessConditions leaseAccessConditions,
            Context context) {
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.azureBlobStorage.containers()
                .getPropertiesWithRestResponseAsync(null, null, null,
                    leaseAccessConditions, context));
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
    public Mono<ContainersSetMetadataResponse> setMetadata(Metadata metadata) {
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
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
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
    public Mono<ContainersSetMetadataResponse> setMetadata(Metadata metadata,
                                                           ContainerAccessConditions accessConditions, Context context) {
        metadata = metadata == null ? new Metadata() : metadata;
        accessConditions = accessConditions == null ? new ContainerAccessConditions() : accessConditions;
        context = context == null ? Context.NONE : context;
        if (!validateNoEtag(accessConditions.modifiedAccessConditions())
                || accessConditions.modifiedAccessConditions().ifUnmodifiedSince() != null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new UnsupportedOperationException(
                    "If-Modified-Since is the only HTTP access condition supported for this API");
        }

        return postProcessResponse(this.azureBlobStorage.containers()
                .setMetadataWithRestResponseAsync(null, null, metadata, null,
                        accessConditions.leaseAccessConditions(), accessConditions.modifiedAccessConditions(), context));
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
    public Mono<ContainersGetAccessPolicyResponse> getAccessPolicy() {
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
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
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
    public Mono<ContainersGetAccessPolicyResponse> getAccessPolicy(LeaseAccessConditions leaseAccessConditions,
            Context context) {
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.azureBlobStorage.containers().getAccessPolicyWithRestResponseAsync(
                null, null, null, leaseAccessConditions, context));
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
    public Mono<ContainersSetAccessPolicyResponse> setAccessPolicy(PublicAccessType accessType,
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
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
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
    public Mono<ContainersSetAccessPolicyResponse> setAccessPolicy(PublicAccessType accessType,
                                                                   List<SignedIdentifier> identifiers, ContainerAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new ContainerAccessConditions() : accessConditions;
        context = context == null ? Context.NONE : context;

        if (!validateNoEtag(accessConditions.modifiedAccessConditions())) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new UnsupportedOperationException("ETag access conditions are not supported for this API.");
        }

        /*
        We truncate to seconds because the service only supports nanoseconds or seconds, but doing an
        OffsetDateTime.now will only give back milliseconds (more precise fields are zeroed and not serialized). This
        allows for proper serialization with no real detriment to users as sub-second precision on active time for
        signed identifiers is not really necessary.
         */
        if (identifiers != null) {
            for (SignedIdentifier identifier : identifiers) {
                if (identifier.accessPolicy() != null && identifier.accessPolicy().start() != null) {
                    identifier.accessPolicy().start(
                            identifier.accessPolicy().start().truncatedTo(ChronoUnit.SECONDS));
                }
                if (identifier.accessPolicy() != null && identifier.accessPolicy().expiry() != null) {
                    identifier.accessPolicy().expiry(
                            identifier.accessPolicy().expiry().truncatedTo(ChronoUnit.SECONDS));
                }
            }
        }

        return postProcessResponse(this.azureBlobStorage.containers()
                .setAccessPolicyWithRestResponseAsync(null, identifiers, null, accessType,
                    null, accessConditions.leaseAccessConditions(), accessConditions.modifiedAccessConditions(),
                    context));

    }

    private boolean validateNoEtag(ModifiedAccessConditions modifiedAccessConditions) {
        if (modifiedAccessConditions == null) {
            return true;
        }
        return modifiedAccessConditions.ifMatch() == null && modifiedAccessConditions.ifNoneMatch() == null;
    }

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
        return this.acquireLease(proposedId, duration, null, null);
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
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
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
        if (!this.validateNoEtag(modifiedAccessConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new UnsupportedOperationException(
                    "ETag access conditions are not supported for this API.");
        }
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.azureBlobStorage.containers().acquireLeaseWithRestResponseAsync(
                null, null, duration, proposedID, null, modifiedAccessConditions, context));
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
        return this.renewLease(leaseID, null, null);
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
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
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
        if (!this.validateNoEtag(modifiedAccessConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new UnsupportedOperationException(
                    "ETag access conditions are not supported for this API.");
        }
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.azureBlobStorage.containers().renewLeaseWithRestResponseAsync(null,
            leaseID, null, null, modifiedAccessConditions, context));
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
        return this.releaseLease(leaseID, null, null);
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
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
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
        if (!this.validateNoEtag(modifiedAccessConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new UnsupportedOperationException(
                    "ETag access conditions are not supported for this API.");
        }
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.azureBlobStorage.containers().releaseLeaseWithRestResponseAsync(
            null, leaseID, null, null, modifiedAccessConditions, context));
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
        return this.breakLease(null, null, null);
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
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
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
        if (!this.validateNoEtag(modifiedAccessConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new UnsupportedOperationException(
                    "ETag access conditions are not supported for this API.");
        }
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.azureBlobStorage.containers().breakLeaseWithRestResponseAsync(null,
            null, breakPeriodInSeconds, null, modifiedAccessConditions, context));

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
        return this.changeLease(leaseID, proposedID, null, null);
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
     *         {@link HttpPipeline}'s policy objects. Most applications do not need to pass
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
        if (!this.validateNoEtag(modifiedAccessConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new UnsupportedOperationException(
                    "ETag access conditions are not supported for this API.");
        }
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.azureBlobStorage.containers().changeLeaseWithRestResponseAsync(null,
            leaseID, proposedID, null, null, modifiedAccessConditions, context));
    }

    /**
     * Returns a single segment of blobs starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @param marker
     *         Identifies the portion of the list to be returned with the next list operation.
     *         This value is returned in the response of a previous list operation as the
     *         ListBlobsFlatSegmentResponse.body().nextMarker(). Set to null to list the first segment.
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
    public Mono<ContainersListBlobFlatSegmentResponse> listBlobsFlatSegment(String marker, ListBlobsOptions options) {
        return this.listBlobsFlatSegment(marker, options, null);
    }

    /**
     * Returns a single segment of blobs starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @param marker
     *         Identifies the portion of the list to be returned with the next list operation.
     *         This value is returned in the response of a previous list operation as the
     *         ListBlobsFlatSegmentResponse.body().nextMarker(). Set to null to list the first segment.
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
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_flat "Sample code for ContainerAsyncClient.listBlobsFlatSegment")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_flat_helper "helper code for ContainerAsyncClient.listBlobsFlatSegment")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainersListBlobFlatSegmentResponse> listBlobsFlatSegment(String marker, ListBlobsOptions options,
            Context context) {
        options = options == null ? new ListBlobsOptions() : options;
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.azureBlobStorage.containers()
                .listBlobFlatSegmentWithRestResponseAsync(null, options.prefix(), marker,
                    options.maxResults(), options.details().toList(), null, null, context));
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
    public Mono<ContainersListBlobHierarchySegmentResponse> listBlobsHierarchySegment(String marker, String delimiter,
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
    public Mono<ContainersListBlobHierarchySegmentResponse> listBlobsHierarchySegment(String marker, String delimiter,
            ListBlobsOptions options, Context context) {
        options = options == null ? new ListBlobsOptions() : options;
        if (options.details().snapshots()) {
            throw new UnsupportedOperationException("Including snapshots in a hierarchical listing is not supported.");
        }
        context = context == null ? Context.NONE : context;

        return postProcessResponse(this.azureBlobStorage.containers()
                .listBlobHierarchySegmentWithRestResponseAsync(null, delimiter, options.prefix(), marker,
                    options.maxResults(), options.details().toList(), null, null, context));
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
    public Mono<ContainersGetAccountInfoResponse> getAccountInfo() {
        return this.getAccountInfo(null);
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
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
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=account_info "Sample code for ContainerAsyncClient.getAccountInfo")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public Mono<ContainersGetAccountInfoResponse> getAccountInfo(Context context) {
        context = context == null ? Context.NONE : context;

        return postProcessResponse(
                this.azureBlobStorage.containers().getAccountInfoWithRestResponseAsync(null, context));
    }
}
