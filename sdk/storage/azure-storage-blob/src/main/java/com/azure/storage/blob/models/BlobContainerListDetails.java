// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.BlobServiceClient;

/**
 * This type allows users to specify additional information the service should return with each container when listing
 * containers in an account (via a {@link BlobServiceClient} object). This type is immutable to ensure thread-safety of
 * requests, so changing the details for a different listing operation requires construction of a new object. Null may
 * be passed if none of the options are desirable.
 */
@Fluent
public final class BlobContainerListDetails {
    private boolean retrieveMetadata;

    /**
     * Constructs an unpopulated {@link BlobContainerListDetails}.
     */
    public BlobContainerListDetails() {
    }

    /**
     * Whether metadata should be returned.
     *
     * @return a flag indicating whether metadata should be returned in the listing
     */
    public boolean getRetrieveMetadata() {
        return this.retrieveMetadata;
    }

    /**
     * Whether metadata should be returned.
     *
     * @param retrieveMetadata Flag indicating whether metadata should be returned
     * @return the updated ContainerListDetails object
     */
    public BlobContainerListDetails setRetrieveMetadata(boolean retrieveMetadata) {
        this.retrieveMetadata = retrieveMetadata;
        return this;
    }

    /**
     * @return the listing flags
     */
    public ListBlobContainersIncludeType toIncludeType() {
        if (this.retrieveMetadata) {
            return ListBlobContainersIncludeType.METADATA;
        }
        return null;
    }
}
