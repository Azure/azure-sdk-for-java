// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.blob.StorageClient;

/**
 * This type allows users to specify additional information the service should return with each container when listing
 * containers in an account (via a {@link StorageClient} object). This type is immutable to ensure thread-safety of
 * requests, so changing the details for a different listing operation requires construction of a new object. Null may
 * be passed if none of the options are desirable.
 */
public final class ContainerListDetails {

    private boolean metadata;

    public ContainerListDetails() {

    }

    /**
     * Whether metadata should be returned.
     */
    public boolean metadata() {
        return this.metadata;
    }

    /**
     * Whether metadata should be returned.
     */
    public ContainerListDetails metadata(boolean metadata) {
        this.metadata = metadata;
        return this;
    }

    /*
     This is used internally to convert the details structure into the appropriate type to pass to the protocol layer.
     It is intended to mirror the BlobListDetails.toList() method, but is slightly different since there is only one
     possible value here currently. The customer should never have need for this.
     */
    public ListContainersIncludeType toIncludeType() {
        if (this.metadata) {
            return ListContainersIncludeType.METADATA;
        }
        return null;
    }
}
