/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.*;
import com.microsoft.rest.v2.http.HttpPipeline;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.microsoft.azure.storage.blob.Utility.safeURLEncode;
import static com.microsoft.azure.storage.blob.Utility.addErrorWrappingToSingle;

/**
 * Represents a URL to a container. It may be obtained by direct construction or via the create method on a
 * {@link ServiceURL} object. This class does not hold any state about a particular blob but is instead a convenient way
 * of sending off appropriate requests to the resource on the service. It may also be used to construct URLs to blobs.
 * Please refer to the
 * <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>Azure Docs</a>
 * for more information on containers.
 */
public final class ContainerURL extends StorageURL {

    public static final String ROOT_CONTAINER_NAME = "$root";

    public static final String STATIC_WEBSITE_CONTAINER_NAME = "$web";

    public static final String LOG_CONTAINER_NAME = "$logs";

    public ContainerURL(URL url, HttpPipeline pipeline) {
        super(url, pipeline);
    }

    /**
     * Creates a new {@link ContainerURL} with the given pipeline.
     *
     * @param pipeline
     *      An {@link HttpPipeline} object to set.
     * @return
     *      A {@link ContainerURL} object with the given pipeline.
     */
    public ContainerURL withPipeline(HttpPipeline pipeline) {
        try {
            return new ContainerURL(new URL(this.storageClient.url()), pipeline);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new {@link BlockBlobURL} object by concatenating the blobName to the end of
     * ContainerURL's URL. The new BlockBlobUrl uses the same request policy pipeline as the ContainerURL.
     * To change the pipeline, create the BlockBlobUrl and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's NewBlockBlobUrl instead of calling this object's
     * NewBlockBlobUrl method.
     *
     * @param blobName
     *      A {@code String} representing the name of the blob.
     * @return
     *      A new {@link BlockBlobURL} object which references the blob with the specified name in this container.
     */
    public BlockBlobURL createBlockBlobURL(String blobName) {
        blobName = safeURLEncode(blobName);
        try {
            return new BlockBlobURL(StorageURL.appendToURLPath(new URL(this.storageClient.url()), blobName),
                    this.storageClient.httpPipeline());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates creates a new PageBlobURL object by concatenating blobName to the end of
     * ContainerURL's URL. The new PageBlobURL uses the same request policy pipeline as the ContainerURL.
     * To change the pipeline, create the PageBlobURL and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's NewPageBlobURL instead of calling this object's
     * NewPageBlobURL method.
     *
     * @param blobName
     *      A {@code String} representing the name of the blob.
     * @return
     *      A new {@link PageBlobURL} object which references the blob with the specified name in this container.
     */
    public PageBlobURL createPageBlobURL(String blobName) {
        blobName = safeURLEncode(blobName);
        try {
            return new PageBlobURL(StorageURL.appendToURLPath(new URL(this.storageClient.url()), blobName),
                    this.storageClient.httpPipeline());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates creates a new AppendBlobURL object by concatenating blobName to the end of
     * ContainerURL's URL. The new AppendBlobURL uses the same request policy pipeline as the ContainerURL.
     * To change the pipeline, create the AppendBlobURL and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's NewAppendBlobURL instead of calling this object's
     * NewAppendBlobURL method.
     *
     * @param blobName
     *      A {@code String} representing the name of the blob.
     * @return
     *      A new {@link AppendBlobURL} object which references the blob with the specified name in this container.
     */
    public AppendBlobURL createAppendBlobURL(String blobName) {
        blobName = safeURLEncode(blobName);
        try {
            return new AppendBlobURL(StorageURL.appendToURLPath(new URL(this.storageClient.url()), blobName),
                    this.storageClient.httpPipeline());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new BlobURL object by concatenating blobName to the end of
     * ContainerURL's URL. The new BlobURL uses the same request policy pipeline as the ContainerURL.
     * To change the pipeline, create the BlobURL and then call its WithPipeline method passing in the
     * desired pipeline object. Or, call this package's createBlobURL instead of calling this object's
     * createBlobURL method.
     *
     * @param blobName
     *      A {@code String} representing the name of the blob.
     * @return
     *      A new {@link BlobURL} object which references the blob with the specified name in this container.
     */
    public BlobURL createBlobURL(String blobName) {
        blobName = safeURLEncode(blobName);
        try {
            return new BlobURL(StorageURL.appendToURLPath(new URL(this.storageClient.url()), blobName),
                    this.storageClient.httpPipeline());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_basic "Sample code for ContainerURL.create")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param metadata
     *      {@link Metadata}
     * @param accessType
     *      Specifies how the data in this container is available to the public. See the x-ms-blob-public-access header
     *      in the Azure Docs for more information. Pass null for no public access.
     * @return
     *      Emits the successful response.
     */
    public Single<ContainerCreateResponse> create(
            Metadata metadata, PublicAccessType accessType) {
        metadata = metadata == null ? Metadata.NONE : metadata;
            return addErrorWrappingToSingle(this.storageClient.generatedContainers().createWithRestResponseAsync(
                    null, metadata, accessType, null));

    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later
     * deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_basic "Sample code for ContainerURL.delete")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param accessConditions
     *      {@link ContainerAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainerDeleteResponse> delete(
            ContainerAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? ContainerAccessConditions.NONE : accessConditions;

        if (!accessConditions.getHttpAccessConditions().getIfMatch().equals(ETag.NONE) ||
                !accessConditions.getHttpAccessConditions().getIfNoneMatch().equals(ETag.NONE)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("ETag access conditions are not supported for this API.");
        }

        return addErrorWrappingToSingle(this.storageClient.generatedContainers()
                .deleteWithRestResponseAsync(null,
                accessConditions.getLeaseAccessConditions().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                null));
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_basic "Sample code for ContainerURL.getProperties")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param leaseAccessConditions
     *      {@link LeaseAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainerGetPropertiesResponse> getProperties(
            LeaseAccessConditions leaseAccessConditions) {
        leaseAccessConditions = leaseAccessConditions == null ? LeaseAccessConditions.NONE : leaseAccessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedContainers()
                .getPropertiesWithRestResponseAsync(null,
                leaseAccessConditions.getLeaseId(), null));
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_basic "Sample code for ContainerURL.setMetadata")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param metadata
     *      {@link Metadata}
     * @param accessConditions
     *      {@link ContainerAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainerSetMetadataResponse> setMetadata(
            Metadata metadata, ContainerAccessConditions accessConditions) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        accessConditions = accessConditions == null ? ContainerAccessConditions.NONE : accessConditions;
        if (accessConditions.getHttpAccessConditions().getIfMatch() != ETag.NONE ||
                accessConditions.getHttpAccessConditions().getIfNoneMatch() != ETag.NONE ||
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince() != null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException(
                    "If-Modified-Since is the only HTTP access condition supported for this API");
        }

        return addErrorWrappingToSingle(this.storageClient.generatedContainers()
                .setMetadataWithRestResponseAsync(null,
                accessConditions.getLeaseAccessConditions().getLeaseId(), metadata,
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),null));
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_policy "Sample code for ContainerURL.getAccessPolicy")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param leaseAccessConditions
     *      {@link LeaseAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainerGetAccessPolicyResponse> getAccessPolicy(
            LeaseAccessConditions leaseAccessConditions) {
        leaseAccessConditions = leaseAccessConditions == null ? LeaseAccessConditions.NONE : leaseAccessConditions;

        return addErrorWrappingToSingle(this.storageClient.generatedContainers().getAccessPolicyWithRestResponseAsync(
                null, leaseAccessConditions.getLeaseId(), null));
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * Note that, for each signed identifier, we will truncate the start and expiry times to the nearest second to
     * ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_policy "Sample code for ContainerURL.setAccessPolicy")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param accessType
     *      Specifies how the data in this container is available to the public. See the x-ms-blob-public-access header
     *      in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers
     *      A list of {@link SignedIdentifier} objects that specify the permissions for the container. Please see
     *      <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     *      for more information. Passing null will clear all access policies.
     * @param accessConditions
     *      {@link ContainerAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainerSetAccessPolicyResponse> setAccessPolicy(
            PublicAccessType accessType, List<SignedIdentifier> identifiers,
            ContainerAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? ContainerAccessConditions.NONE : accessConditions;

        if (!accessConditions.getHttpAccessConditions().getIfMatch().equals(ETag.NONE) ||
                !accessConditions.getHttpAccessConditions().getIfNoneMatch().equals(ETag.NONE)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("ETag access conditions are not supported for this API.");
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
                    identifier.accessPolicy().withStart(
                            identifier.accessPolicy().start().truncatedTo(ChronoUnit.SECONDS));
                }
                if (identifier.accessPolicy() != null && identifier.accessPolicy().expiry() != null) {
                    identifier.accessPolicy().withExpiry(
                            identifier.accessPolicy().expiry().truncatedTo(ChronoUnit.SECONDS));
                }
            }
        }

        // TODO: validate that empty list clears permissions and null list does not change list. Document behavior.
        return addErrorWrappingToSingle(this.storageClient.generatedContainers()
                .setAccessPolicyWithRestResponseAsync(identifiers, null,
                accessConditions.getLeaseAccessConditions().getLeaseId(), accessType,
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                null));
    }

    private boolean validateLeaseOperationAccessConditions(HTTPAccessConditions httpAccessConditions) {
        return (httpAccessConditions.getIfMatch() == ETag.NONE &&
                httpAccessConditions.getIfNoneMatch() == ETag.NONE);
    }

    /**
     * Acquires a lease on the container for delete operations. The lease duration must be between 15 to
     * 60 seconds, or infinite (-1). For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerURL.acquireLease")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param proposedID
     *      A {@code String} in any valid GUID format.
     * @param duration
     *      The duration of the lease, in seconds, or negative one (-1) for a lease that never expires.
     *      A non-infinite lease can be between 15 and 60 seconds.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainerAcquireLeaseResponse> acquireLease(
            String proposedID, int duration, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;
        if (!this.validateLeaseOperationAccessConditions(httpAccessConditions)){
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException(
                    "ETag access conditions are not supported for this API.");
        }

        return addErrorWrappingToSingle(this.storageClient.generatedContainers().acquireLeaseWithRestResponseAsync(
                null,  duration, proposedID,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                null));
    }

    /**
     * Renews the container's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerURL.renewLease")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param leaseID
     *      The leaseId of the active lease on the container.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainerRenewLeaseResponse> renewLease(
            String leaseID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;
        if (!this.validateLeaseOperationAccessConditions(httpAccessConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException(
                    "ETag access conditions are not supported for this API.");
        }

        return addErrorWrappingToSingle(this.storageClient.generatedContainers().renewLeaseWithRestResponseAsync(
                leaseID, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                null));
    }

    /**
     * Releases the container's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerURL.releaseLease")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param leaseID
     *      The leaseId of the active lease on the container.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainerReleaseLeaseResponse> releaseLease(
            String leaseID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;
        if (!this.validateLeaseOperationAccessConditions(httpAccessConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException(
                    "ETag access conditions are not supported for this API.");
        }

        return addErrorWrappingToSingle(this.storageClient.generatedContainers().releaseLeaseWithRestResponseAsync(
                leaseID, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                null));
    }

    /**
     * Breaks the container's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerURL.breakLease")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param breakPeriodInSeconds
     *      An optional {@code Integer} representing the proposed duration of seconds that the lease should continue
     *      before it is broken, between 0 and 60 seconds. This break period is only used if it is shorter than the time
     *      remaining on the lease. If longer, the time remaining on the lease is used. A new lease will not be
     *      available before the break period has expired, but the lease may be held for longer than the break period.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainerBreakLeaseResponse> breakLease(
            Integer breakPeriodInSeconds, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;
        if (!this.validateLeaseOperationAccessConditions(httpAccessConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException(
                    "ETag access conditions are not supported for this API.");
        }

        return addErrorWrappingToSingle(this.storageClient.generatedContainers().breakLeaseWithRestResponseAsync(
                null, breakPeriodInSeconds,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                null));
    }

    /**
     * Changes the container's leaseID. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=container_lease "Sample code for ContainerURL.changeLease")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param leaseID
     *      The leaseId of the active lease on the container.
     * @param proposedID
     *      A {@code String} in any valid GUID format.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainerChangeLeaseResponse> changeLease(
            String leaseID, String proposedID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;
        if (!this.validateLeaseOperationAccessConditions(httpAccessConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException(
                    "ETag access conditions are not supported for this API.");
        }

        return addErrorWrappingToSingle(this.storageClient.generatedContainers().changeLeaseWithRestResponseAsync(
                leaseID, proposedID, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                null));
    }

    /**
     * Returns a single segment of blobs starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_flat "Sample code for ContainerURL.listBlobsFlatSegment")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_flat_helper "helper code for ContainerURL.listBlobsFlatSegment")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param marker
     *      Identifies the portion of the list to be returned with the next list operation.
     *      This value is returned in the response of a previous list operation as the
     *      ListBlobsFlatSegmentResponse.body().nextMarker(). Set to null to list the first segment.
     * @param options
     *      {@link ListBlobsOptions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainerListBlobFlatSegmentResponse> listBlobsFlatSegment(
            String marker, ListBlobsOptions options) {
        options = options == null ? ListBlobsOptions.DEFAULT : options;

        return addErrorWrappingToSingle(this.storageClient.generatedContainers()
                .listBlobFlatSegmentWithRestResponseAsync(
                options.getPrefix(), marker, options.getMaxResults(),
                options.getDetails().toList(), null, null));
    }

    /**
     * Returns a single segment of blobs and blob prefixes starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_hierarchy "Sample code for ContainerURL.listBlobsHierarchySegment")] \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=list_blobs_hierarchy_helper "helper code for ContainerURL.listBlobsHierarchySegment")] \n
     * For more samples, please see the [Samples file] (https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     *
     * @param marker
     *      Identifies the portion of the list to be returned with the next list operation.
     *      This value is returned in the response of a previous list operation as the
     *      ListBlobsHierarchySegmentResponse.body().nextMarker(). Set to null to list the first segment.
     * @param delimiter
     *      The operation returns a BlobPrefix element in the response body that acts as a placeholder for all blobs
     *      whose names begin with the same substring up to the appearance of the delimiter character. The delimiter may
     *      be a single character or a string.
     * @param options
     *      {@link ListBlobsOptions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainerListBlobHierarchySegmentResponse> listBlobsHierarchySegment(
            String marker, String delimiter, ListBlobsOptions options) {
        options = options == null ? ListBlobsOptions.DEFAULT : options;
        if (options.getDetails().getSnapshots()) {
            throw new IllegalArgumentException("Including snapshots in a hierarchical listing is not supported.");
        }

        return addErrorWrappingToSingle(this.storageClient.generatedContainers()
                .listBlobHierarchySegmentWithRestResponseAsync(
                delimiter, options.getPrefix(), marker, options.getMaxResults(),
                options.getDetails().toList(), null, null));
    }
}
