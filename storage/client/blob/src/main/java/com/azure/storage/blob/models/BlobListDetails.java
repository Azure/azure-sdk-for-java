// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.blob.ContainerClient;

import java.util.ArrayList;

/**
 * This type allows users to specify additional information the service should return with each blob when listing blobs
 * in a container (via a {@link ContainerClient} object). This type is immutable to ensure thread-safety of requests, so
 * changing the details for a different listing operation requires construction of a new object. Null may be passed if
 * none of the options are desirable.
 */
public final class BlobListDetails {

    private boolean copy;

    private boolean metadata;

    private boolean snapshots;

    private boolean uncommittedBlobs;

    private boolean deletedBlobs;

    public BlobListDetails() {
    }

    /**
     * Whether blob metadata related to any current or previous Copy Blob operation should be included in the
     * response.
     *
     * @return a flag indicating if copy information will be returned in the listing
     */
    public boolean copy() {
        return copy;
    }

    /**
     * Whether blob metadata related to any current or previous Copy Blob operation should be included in the
     * response.
     *
     * @param copy Flag indicating whether copy information should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails copy(boolean copy) {
        this.copy = copy;
        return this;
    }

    /**
     * Whether blob metadata should be returned.
     *
     * @return a flag indicating if metadata will be returned in the listing
     */
    public boolean metadata() {
        return metadata;
    }

    /**
     * Whether blob metadata should be returned.
     *
     * @param metadata Flag indicating whether metadata should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails metadata(boolean metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Whether snapshots should be returned. Snapshots are listed from oldest to newest.
     *
     * @return a flag indicating if snapshots will be returned in the listing
     */
    public boolean snapshots() {
        return snapshots;
    }

    /**
     * Whether snapshots should be returned. Snapshots are listed from oldest to newest.
     *
     * @param snapshots Flag indicating whether snapshots should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails snapshots(boolean snapshots) {
        this.snapshots = snapshots;
        return this;
    }

    /**
     * Whether blobs for which blocks have been uploaded, but which have not been committed using Put Block List,
     * should be included in the response.
     *
     * @return a flag indicating if uncommitted blobs will be returned in the listing
     */
    public boolean uncommittedBlobs() {
        return uncommittedBlobs;
    }

    /**
     * Whether blobs for which blocks have been uploaded, but which have not been committed using Put Block List,
     * should be included in the response.
     *
     * @param uncommittedBlobs Flag indicating whether uncommitted blobs should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails uncommittedBlobs(boolean uncommittedBlobs) {
        this.uncommittedBlobs = uncommittedBlobs;
        return this;
    }

    /**
     * Whether blobs which have been soft deleted should be returned.
     *
     * @return a flag indicating if deleted blobs will be returned in the listing
     */
    public boolean deletedBlobs() {
        return deletedBlobs;
    }

    /**
     * Whether blobs which have been soft deleted should be returned.
     *
     * @param deletedBlobs Flag indicating whether deleted blobs should be returned
     * @return the updated BlobListDetails object
     */
    public BlobListDetails deletedBlobs(boolean deletedBlobs) {
        this.deletedBlobs = deletedBlobs;
        return this;
    }

    /**
     * @return a list of the flag set to true
     */
    public ArrayList<ListBlobsIncludeItem> toList() {
        ArrayList<ListBlobsIncludeItem> details = new ArrayList<ListBlobsIncludeItem>();
        if (this.copy) {
            details.add(ListBlobsIncludeItem.COPY);
        }
        if (this.deletedBlobs) {
            details.add(ListBlobsIncludeItem.DELETED);
        }
        if (this.metadata) {
            details.add(ListBlobsIncludeItem.METADATA);
        }
        if (this.snapshots) {
            details.add(ListBlobsIncludeItem.SNAPSHOTS);
        }
        if (this.uncommittedBlobs) {
            details.add(ListBlobsIncludeItem.UNCOMMITTEDBLOBS);
        }
        return details;
    }
}
