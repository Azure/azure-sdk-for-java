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
    private boolean retrieveSnapshots;
    private boolean retrieveUncommittedBlobs;
    private boolean retrieveDeletedBlobs;

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
        if (this.retrieveSnapshots) {
            details.add(ListBlobsIncludeItem.SNAPSHOTS);
        }
        if (this.retrieveUncommittedBlobs) {
            details.add(ListBlobsIncludeItem.UNCOMMITTEDBLOBS);
        }
        return details;
    }
}
