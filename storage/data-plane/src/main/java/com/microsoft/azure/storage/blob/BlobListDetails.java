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
     */
    public boolean copy() {
        return copy;
    }

    /**
     * Whether blob metadata related to any current or previous Copy Blob operation should be included in the
     * response.
     */
    public BlobListDetails withCopy(boolean copy) {
        this.copy = copy;
        return this;
    }

    /**
     * Whether blob metadata should be returned.
     */
    public boolean metadata() {
        return metadata;
    }

    /**
     * Whether blob metadata should be returned.
     */
    public BlobListDetails withMetadata(boolean metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Whether snapshots should be returned. Snapshots are listed from oldest to newest.
     */
    public boolean snapshots() {
        return snapshots;
    }

    /**
     * Whether snapshots should be returned. Snapshots are listed from oldest to newest.
     */
    public BlobListDetails withSnapshots(boolean snapshots) {
        this.snapshots = snapshots;
        return this;
    }

    /**
     * Whether blobs for which blocks have been uploaded, but which have not been committed using Put Block List,
     * should be included in the response.
     */
    public boolean uncommittedBlobs() {
        return uncommittedBlobs;
    }

    /**
     * Whether blobs for which blocks have been uploaded, but which have not been committed using Put Block List,
     * should be included in the response.
     */
    public BlobListDetails withUncommittedBlobs(boolean uncommittedBlobs) {
        this.uncommittedBlobs = uncommittedBlobs;
        return this;
    }

    /**
     * Whether blobs which have been soft deleted should be returned.
     */
    public boolean deletedBlobs() {
        return deletedBlobs;
    }

    /**
     * Whether blobs which have been soft deleted should be returned.
     */
    public BlobListDetails withDeletedBlobs(boolean deletedBlobs) {
        this.deletedBlobs = deletedBlobs;
        return this;
    }

    /*
    This is used internally to convert the details structure into a list to pass to the protocol layer. The customer
    should never have need for this.
     */
    ArrayList<ListBlobsIncludeItem> toList() {
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
