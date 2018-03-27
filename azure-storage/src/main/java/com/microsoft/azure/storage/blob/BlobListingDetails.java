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

import com.microsoft.azure.storage.blob.models.ListBlobsIncludeItem;

import java.util.ArrayList;

/**
 * This type allows users to specify additional information the service should return with each blob when listing blobs
 * in a container (via a {@link ContainerURL} object). This type is immutable to ensure thread-safety of requests, so
 * changing the details for a different listing operation requires construction of a new object.
 */
public final class BlobListingDetails {

    /**
     * An object representing no listing details.
     */
    public static final BlobListingDetails NONE = new BlobListingDetails(false, false, false,
            false);

    private final boolean copy;

    private final boolean metadata;

    private final boolean snapshots;

    private final boolean uncommittedBlobs;

    /**
     * A {@link BlobListingDetails} object.
     *
     * @param copy
     *      Whether blob metadata related to any current or previous Copy Blob operation should be included in the
     *      response.
     * @param metadata
     *      Whether blob metadata should be returned.
     * @param snapshots
     *      Whether snapshots should be returned. Snapshots are listed from oldest to newest.
     * @param uncommittedBlobs
     *      Whether blobs for which blocks have been uploaded, but which have not been committed using Put Block List,
     *      should be included in the response.
     */
    public BlobListingDetails(boolean copy, boolean metadata, boolean snapshots, boolean uncommittedBlobs) {
        this.copy = copy;
        this.metadata = metadata;
        this.snapshots = snapshots;
        this.uncommittedBlobs = uncommittedBlobs;
    }

    /**
     * @return
     *      Whether blob copies should be returned.
     */
    public boolean getCopy() {
        return copy;
    }

    /**
     * @return
     *      Whether metadata should be returned.
     */
    public boolean getMetadata() {
        return metadata;
    }

    /**
     * @return
     *      Whether snapshots should be returned.
     */
    public boolean getSnapshots() {
        return snapshots;
    }

    /**
     * @return
     *      Whether uncommitted blobs should be returned.
     */
    public boolean getUncommittedBlobs() {
        return uncommittedBlobs;
    }

    /*
     This is used internally to convert the details structure into a list to pass to the protocol layer. The customer
     should never have need for this.
     */
    ArrayList<ListBlobsIncludeItem> toList() {
        ArrayList<ListBlobsIncludeItem> details = new ArrayList<ListBlobsIncludeItem>();
        if(this.copy) {
            details.add(ListBlobsIncludeItem.COPY);
        }
        if(this.metadata) {
            details.add(ListBlobsIncludeItem.METADATA);
        }
        if(this.snapshots) {
            details.add(ListBlobsIncludeItem.SNAPSHOTS);
        }
        if(this.uncommittedBlobs) {
            details.add(ListBlobsIncludeItem.UNCOMMITTEDBLOBS);
        }
        return details;
    }
}
