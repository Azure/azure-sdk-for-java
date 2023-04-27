// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/** Used to specify Blob container url to recording storage. */
@Fluent
public final class BlobStorage extends ExternalStorage {

    /*
     * Url of a container or a location within a container
     */
    private final String containerUrl;

    /**
     * Constructor
     *
     * @param containerUrl Url of a container or a location within a container.
     */

    public BlobStorage(String containerUrl) {
        super(RecordingStorageType.BLOB_STORAGE);
        this.containerUrl = containerUrl;
    }


    /**
     * Get the containerUrl property: Url of a container or a location within a container.
     *
     * @return the containerUrl value.
     */
    public String getContainerUrl() {
        return this.containerUrl;
    }
}
