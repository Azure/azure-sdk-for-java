// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Used to specify Blob container url to recording storage. */
@Fluent
public final class BlobStorage extends ExternalStorage {

    /*
     * Url of a container or a location within a container
     */
    @JsonProperty(value = "containerUri", required = true)
    private final String containerUri;

    /**
     * Constructor
     *
     * @param containerUri Url of a container or a location within a container.
     */

    public BlobStorage(String containerUri) {
        super(RecordingStorageType.BLOB_STORAGE);
        this.containerUri = containerUri;
    }


    /**
     * Get the containerUri property: Url of a container or a location within a container.
     *
     * @return the containerUri value.
     */
    public String getContainerUri() {
        return this.containerUri;
    }
}
