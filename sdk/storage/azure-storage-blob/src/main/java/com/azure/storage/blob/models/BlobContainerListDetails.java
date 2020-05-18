// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.BlobServiceClient;

/**
 * This type allows users to specify additional information the service should return with each container when listing
 * containers in an account (via a {@link BlobServiceClient} object). Null may
 * be passed if none of the options are desirable.
 */
@Fluent
public final class BlobContainerListDetails {
    private boolean retrieveMetadata;
    private boolean retrieveDeleted;

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
     * Whether deleted containers should be returned.
     *
     * @return a flag indicating whether deleted containers should be returned
     */
    public boolean getRetrieveDeleted() {
        return this.retrieveDeleted;
    }

    /**
     * Whether deleted containers should be returned.
     *
     * @param retrieveDeleted Flag indicating whether deleted containers should be returned.
     * @return the updated ContainerListDetails object
     */
    public BlobContainerListDetails setRetrieveDeleted(boolean retrieveDeleted) {
        this.retrieveDeleted = retrieveDeleted;
        return this;
    }

    /**
     * @return the listing flags
     * @deprecated {@link BlobContainerListDetails} now contains multiple options.
     * This will only convert result of {{@link #setRetrieveMetadata(boolean)}} for backwards compatibility.
     */
    @Deprecated
    public ListBlobContainersIncludeType toIncludeType() {
        if (this.retrieveMetadata) {
            return ListBlobContainersIncludeType.METADATA;
        }
        return null;
    }
}
