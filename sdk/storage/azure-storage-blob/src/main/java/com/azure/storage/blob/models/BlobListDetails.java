// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.BlobContainerClient;

import java.util.ArrayList;

/**
 * This type allows users to specify additional information the service should return with each blob when listing blobs
 * in a container (via a {@link BlobContainerClient} object). This type is immutable to ensure thread-safety of
 * requests, so changing the details for a different listing operation requires construction of a new object. Null may
 * be passed if none of the options are desirable.
 */
@Fluent
public final class BlobListDetails {
    private boolean retrieveCopy;
    private boolean retrieveMetadata;
    private boolean retrieveTags;
    private boolean retrieveSnapshots;
    private boolean retrieveUncommittedBlobs;
    private boolean retrieveDeletedBlobs;
    private boolean retrieveVersions;
    private boolean retrieveDeletedWithVersions;
    private boolean retrieveImmutabilityPolicy;
    private boolean retrieveLegalHold;

    /**
     * Constructs an unpopulated {@link BlobListDetails}.
     */
    public BlobListDetails() {
    }

    /**
     * Whether blob metadata related to any current or previous Copy Blob operation should be included in the response.
     *
     * @return a flag indicating if copy information will be returned in the listing
     */
    public boolean getRetrieveCopy() {
        return retrieveCopy;
    }

    /**
     * Whether blob metadata related to any current or previous Copy Blob operation should be included in the response.
     *
     * @param retrieveCopy Flag indicating whether copy information should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails setRetrieveCopy(boolean retrieveCopy) {
        this.retrieveCopy = retrieveCopy;
        return this;
    }

    /**
     * Whether blob metadata should be returned.
     *
     * @return a flag indicating if metadata will be returned in the listing
     */
    public boolean getRetrieveMetadata() {
        return retrieveMetadata;
    }

    /**
     * Whether blob metadata should be returned.
     *
     * @param retrieveMetadata Flag indicating whether metadata should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails setRetrieveMetadata(boolean retrieveMetadata) {
        this.retrieveMetadata = retrieveMetadata;
        return this;
    }

    /**
     * Whether blob tags should be returned.
     *
     * @return a flag indicating if tags will be returned in the listing
     */
    public boolean getRetrieveTags() {
        return retrieveTags;
    }

    /**
     * Whether blob tags should be returned.
     *
     * @param retrieveTags Flag indicating whether tags should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails setRetrieveTags(boolean retrieveTags) {
        this.retrieveTags = retrieveTags;
        return this;
    }

    /**
     * Whether snapshots should be returned. Snapshots are listed from oldest to newest.
     *
     * @return a flag indicating if snapshots will be returned in the listing
     */
    public boolean getRetrieveSnapshots() {
        return retrieveSnapshots;
    }

    /**
     * Whether snapshots should be returned. Snapshots are listed from oldest to newest.
     *
     * @param retrieveSnapshots Flag indicating whether snapshots should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails setRetrieveSnapshots(boolean retrieveSnapshots) {
        this.retrieveSnapshots = retrieveSnapshots;
        return this;
    }

    /**
     * Whether versions should be returned. Versions are listed from oldest to newest.
     *
     * @return a flag indicating if versions will be returned in the listing
     */
    public boolean getRetrieveVersions() {
        return retrieveVersions;
    }

    /**
     * Whether versions should be returned. Versions are listed from oldest to newest.
     *
     * @param retrieveVersions Flag indicating whether versions should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails setRetrieveVersions(boolean retrieveVersions) {
        this.retrieveVersions = retrieveVersions;
        return this;
    }

    /**
     * Whether blobs for which blocks have been uploaded, but which have not been committed using Put Block List, should
     * be included in the response.
     *
     * @return a flag indicating if uncommitted blobs will be returned in the listing
     */
    public boolean getRetrieveUncommittedBlobs() {
        return retrieveUncommittedBlobs;
    }

    /**
     * Whether blobs for which blocks have been uploaded, but which have not been committed using Put Block List, should
     * be included in the response.
     *
     * @param retrieveUncommittedBlobs Flag indicating whether uncommitted blobs should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails setRetrieveUncommittedBlobs(boolean retrieveUncommittedBlobs) {
        this.retrieveUncommittedBlobs = retrieveUncommittedBlobs;
        return this;
    }

    /**
     * Whether blobs which have been soft deleted should be returned.
     *
     * @return a flag indicating if deleted blobs will be returned in the listing
     */
    public boolean getRetrieveDeletedBlobs() {
        return retrieveDeletedBlobs;
    }

    /**
     * Whether blobs which have been soft deleted should be returned.
     *
     * @param retrieveDeletedBlobs Flag indicating whether deleted blobs should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails setRetrieveDeletedBlobs(boolean retrieveDeletedBlobs) {
        this.retrieveDeletedBlobs = retrieveDeletedBlobs;
        return this;
    }

    /**
     * Whether blobs which have been deleted with versioning.
     *
     * @return a flag indicating if deleted blobs with versioning will be returned in the listing
     */
    public boolean getRetrieveDeletedBlobsWithVersions() {
        return retrieveDeletedWithVersions;
    }

    /**
     * Whether blobs which have been deleted with versioning should be returned.
     *
     * @param retrieveDeletedWithVersions Flag indicating whether deleted blobs with versioning should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails setRetrieveDeletedBlobsWithVersions(boolean retrieveDeletedWithVersions) {
        this.retrieveDeletedWithVersions = retrieveDeletedWithVersions;
        return this;
    }

    /**
     * Whether immutability policy for the blob should be returned.
     *
     * @return a flag indicating if immutability policy for the blob will be returned in the listing
     */
    public boolean getRetrieveImmutabilityPolicy() {
        return retrieveImmutabilityPolicy;
    }

    /**
     * Whether immutability policy for the blob should be returned.
     *
     * @param retrieveImmutabilityPolicy Flag indicating whether immutability policy for the blob should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails setRetrieveImmutabilityPolicy(boolean retrieveImmutabilityPolicy) {
        this.retrieveImmutabilityPolicy = retrieveImmutabilityPolicy;
        return this;
    }

    /**
     * Whether legal hold for the blob should be returned.
     *
     * @return a flag indicating if legal hold for the blob will be returned in the listing
     */
    public boolean getRetrieveLegalHold() {
        return retrieveLegalHold;
    }

    /**
     * Whether legal hold for the blob should be returned.
     *
     * @param retrieveLegalHold Flag indicating whetherlegal hold for the blob  should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails setRetrieveLegalHold(boolean retrieveLegalHold) {
        this.retrieveLegalHold = retrieveLegalHold;
        return this;
    }

    /**
     * @return a list of the flag set to true
     */
    public ArrayList<ListBlobsIncludeItem> toList() {
        ArrayList<ListBlobsIncludeItem> details = new ArrayList<>();
        if (this.retrieveCopy) {
            details.add(ListBlobsIncludeItem.COPY);
        }
        if (this.retrieveDeletedBlobs) {
            details.add(ListBlobsIncludeItem.DELETED);
        }
        if (this.retrieveMetadata) {
            details.add(ListBlobsIncludeItem.METADATA);
        }
        if (this.retrieveTags) {
            details.add(ListBlobsIncludeItem.TAGS);
        }
        if (this.retrieveSnapshots) {
            details.add(ListBlobsIncludeItem.SNAPSHOTS);
        }
        if (this.retrieveUncommittedBlobs) {
            details.add(ListBlobsIncludeItem.UNCOMMITTEDBLOBS);
        }
        if (this.retrieveVersions) {
            details.add(ListBlobsIncludeItem.VERSIONS);
        }
        if (this.retrieveDeletedWithVersions) {
            details.add(ListBlobsIncludeItem.DELETED_WITH_VERSIONS);
        }
        if (this.retrieveImmutabilityPolicy) {
            details.add(ListBlobsIncludeItem.IMMUTABILITY_POLICY);
        }
        if (this.retrieveLegalHold) {
            details.add(ListBlobsIncludeItem.LEGAL_HOLD);
        }
        return details;
    }
}
