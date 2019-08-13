// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.blob.BlobServiceClient;

/**
 * This type allows users to specify additional information the service should return with each container when listing
 * containers in an account (via a {@link BlobServiceClient} object). This type is immutable to ensure thread-safety of
 * requests, so changing the details for a different listing operation requires construction of a new object. Null may
 * be passed if none of the options are desirable.
 */
public final class ContainerListDetails {

    private boolean metadata;

    public ContainerListDetails() {

    }

    /**
     * Whether metadata should be returned.
     *
     * @return a flag indicating whether metadata should be returned in the listing
     */
    public boolean metadata() {
        return this.metadata;
    }

    /**
     * Whether metadata should be returned.
     *
     * @param metadata Flag indicating whether metadata should be returned
     * @return the updated ContainerListDetails object
     */
    public ContainerListDetails metadata(boolean metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @return the listing flags
     */
    public ListContainersIncludeType toIncludeType() {
        if (this.metadata) {
            return ListContainersIncludeType.METADATA;
        }
        return null;
    }
}
