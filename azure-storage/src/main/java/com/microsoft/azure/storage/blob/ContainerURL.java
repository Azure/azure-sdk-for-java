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
import java.util.List;

/**
 * Represents a URL to a container. It may be obtained by direct construction or via the create method on a
 * {@link ServiceURL} object. This class does not hold any state about a particular blob but is instead a convenient way
 * of sending off appropriate requests to the resource on the service. It may also be used to construct URLs to blobs.
 * Please refer to the following for more information on containers:
 * https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction
 */
public final class ContainerURL extends StorageURL {

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
     * @param metadata
     *      {@link Metadata}
     * @param accessType
     *      Specifies how the data in this container is available to the public. See the x-ms-blob-public-access header
     *      in the Azure Docs for more information. Pass null for no public access.
     * @return
     *      Emits the successful response.
     */
    public Single<ContainersCreateResponse> create(
            Metadata metadata, PublicAccessType accessType) {
        metadata = metadata == null ? Metadata.NONE : metadata;
        return this.storageClient.generatedContainers().createWithRestResponseAsync(
                null, metadata, accessType, null);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later
     * deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @param accessConditions
     *      {@link ContainerAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainersDeleteResponse> delete(
            ContainerAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? ContainerAccessConditions.NONE : accessConditions;

        if (!accessConditions.getHttpAccessConditions().getIfMatch().equals(ETag.NONE) ||
                !accessConditions.getHttpAccessConditions().getIfNoneMatch().equals(ETag.NONE)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException("ETag access conditions are not supported for this API.");
        }

        return this.storageClient.generatedContainers().deleteWithRestResponseAsync(null,
                accessConditions.getLeaseID().getLeaseId(),
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                null);
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @param leaseAccessConditions
     *      {@link LeaseAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainersGetPropertiesResponse> getProperties(
            LeaseAccessConditions leaseAccessConditions) {
        leaseAccessConditions = leaseAccessConditions == null ? LeaseAccessConditions.NONE : leaseAccessConditions;

        return this.storageClient.generatedContainers().getPropertiesWithRestResponseAsync(null,
                leaseAccessConditions.getLeaseId(), null);
    }

    /**
     * Sets the container's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * @param metadata
     *      {@link Metadata}
     * @param accessConditions
     *      {@link ContainerAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainersSetMetadataResponse> setMetadata(
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


        return this.storageClient.generatedContainers().setMetadataWithRestResponseAsync(null,
                accessConditions.getLeaseID().getLeaseId(), metadata,
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),null);
    }

    /**
     * Returns the container's permissions. The permissions indicate whether container's blobs may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * @param leaseAccessConditions
     *      {@link LeaseAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainersGetAccessPolicyResponse> getAccessPolicy(
            LeaseAccessConditions leaseAccessConditions) {
        leaseAccessConditions = leaseAccessConditions == null ? LeaseAccessConditions.NONE : leaseAccessConditions;

        return this.storageClient.generatedContainers().getAccessPolicyWithRestResponseAsync(
                null, leaseAccessConditions.getLeaseId(), null);
    }

    /**
     * Sets the container's permissions. The permissions indicate whether blobs in a container may be accessed publicly.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * @param accessType
     *      Specifies how the data in this container is available to the public. See the x-ms-blob-public-access header
     *      in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers
     *      A list of {@link SignedIdentifier} objects that specify the permissions for the container. Please see
     *      <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     *      for more information.
     * @param accessConditions
     *      {@link ContainerAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainersSetAccessPolicyResponse> setAccessPolicy(
            PublicAccessType accessType, List<SignedIdentifier> identifiers,
            ContainerAccessConditions accessConditions) {
        accessConditions = accessConditions == null ? ContainerAccessConditions.NONE : accessConditions;

        // TODO: validate that empty list clears permissions and null list does not change list. Document behavior.
        return this.storageClient.generatedContainers().setAccessPolicyWithRestResponseAsync(identifiers, null,
                accessConditions.getLeaseID().getLeaseId(), accessType,
                accessConditions.getHttpAccessConditions().getIfModifiedSince(),
                accessConditions.getHttpAccessConditions().getIfUnmodifiedSince(),
                null);
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
    public Single<ContainersAcquireLeaseResponse> acquireLease(
            String proposedID, int duration, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;
        if (!this.validateLeaseOperationAccessConditions(httpAccessConditions)){
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException(
                    "ETag access conditions are not supported for this API.");
        }

        return this.storageClient.generatedContainers().acquireLeaseWithRestResponseAsync(
                null, duration, proposedID,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                null);
    }

    /**
     * Renews the container's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @param leaseID
     *      The leaseId of the active lease on the container.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainersRenewLeaseResponse> renewLease(
            String leaseID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;
        if (!this.validateLeaseOperationAccessConditions(httpAccessConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException(
                    "ETag access conditions are not supported for this API.");
        }

        return this.storageClient.generatedContainers().renewLeaseWithRestResponseAsync(
                leaseID, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                null);
    }

    /**
     * Releases the container's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
     *
     * @param leaseID
     *      The leaseId of the active lease on the container.
     * @param httpAccessConditions
     *      {@link HTTPAccessConditions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainersReleaseLeaseResponse> releaseLease(
            String leaseID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;
        if (!this.validateLeaseOperationAccessConditions(httpAccessConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException(
                    "ETag access conditions are not supported for this API.");
        }

        return this.storageClient.generatedContainers().releaseLeaseWithRestResponseAsync(
                leaseID, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                null);
    }

    /**
     * Breaks the container's previously-acquired lease. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
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
    public Single<ContainersBreakLeaseResponse> breakLease(
            Integer breakPeriodInSeconds, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;
        if (!this.validateLeaseOperationAccessConditions(httpAccessConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException(
                    "ETag access conditions are not supported for this API.");
        }

        return this.storageClient.generatedContainers().breakLeaseWithRestResponseAsync(
                null, breakPeriodInSeconds,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                null);
    }

    /**
     * Changes the container's leaseID. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">Azure Docs</a>.
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
    public Single<ContainersChangeLeaseResponse> changeLease(
            String leaseID, String proposedID, HTTPAccessConditions httpAccessConditions) {
        httpAccessConditions = httpAccessConditions == null ? HTTPAccessConditions.NONE : httpAccessConditions;
        if (!this.validateLeaseOperationAccessConditions(httpAccessConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new IllegalArgumentException(
                    "ETag access conditions are not supported for this API.");
        }

        return this.storageClient.generatedContainers().changeLeaseWithRestResponseAsync(
                leaseID, proposedID, null,
                httpAccessConditions.getIfModifiedSince(),
                httpAccessConditions.getIfUnmodifiedSince(),
                null);
    }

    /**
     * Returns a single segment of blobs starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @param marker
     *      Identifies the portion of the list to be returned with the next list operation.
     *      This value is returned in the response of a previous list operation. Set to null if this is the first
     *      segment.
     * @param options
     *      {@link ListBlobsOptions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainersListBlobFlatSegmentResponse> listBlobsFlatSegment(
            String marker, ListBlobsOptions options) {
        options = options == null ? ListBlobsOptions.DEFAULT : options;

        return this.storageClient.generatedContainers().listBlobFlatSegmentWithRestResponseAsync(
                options.getPrefix(), marker, options.getMaxResults(),
                options.getDetails().toList(), null, null);
    }

    /**
     * Returns a single segment of blobs and blob prefixes starting from the specified Marker. Use an empty
     * marker to start enumeration from the beginning. Blob names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListBlobs again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * @param marker
     *      Identifies the portion of the list to be returned with the next list operation. This value is returned in
     *      the response of a previous list operation. Set to null if this is the first segment.
     * @param delimiter
     *      The operation returns a BlobPrefix element in the response body that acts as a placeholder for all blobs
     *      whose names begin with the same substring up to the appearance of the delimiter character. The delimiter may
     *      be a single character or a string.
     * @param options
     *      {@link ListBlobsOptions}
     * @return
     *      Emits the successful response.
     */
    public Single<ContainersListBlobHierarchySegmentResponse> listBlobsHierarchySegment(
            String marker, String delimiter, ListBlobsOptions options) {
        options = options == null ? ListBlobsOptions.DEFAULT : options;

        return this.storageClient.generatedContainers().listBlobHierarchySegmentWithRestResponseAsync(
                delimiter, options.getPrefix(), marker, options.getMaxResults(),
                options.getDetails().toList(), null, null);
    }
}
