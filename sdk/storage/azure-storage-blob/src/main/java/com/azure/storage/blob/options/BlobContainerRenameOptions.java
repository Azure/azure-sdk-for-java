// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRequestConditions;

import java.util.Objects;

/**
 * Extended options that may be passed when renaming a blob container.
 */
@Fluent
class BlobContainerRenameOptions {

    private final String destinationContainerName;
    private BlobRequestConditions requestConditions;

    /**
     * @param destinationContainerName The new name of the container.
     */
    BlobContainerRenameOptions(String destinationContainerName) {
        Objects.requireNonNull(destinationContainerName);
        this.destinationContainerName = destinationContainerName;
    }

    /**
     * @return The new name of the container.
     */
    public String getDestinationContainerName() {
        return destinationContainerName;
    }

    /**
     * @return {@link BlobRequestConditions}
     */
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link BlobRequestConditions}
     * @return The updated options.
     * @throws UnsupportedOperationException if a condition other than lease id is set.
     */
    public BlobContainerRenameOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
