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
public class ContainerRenameOptions {

    private final String destinationContainerName;
    private final String sourceContainerName;
    private BlobRequestConditions requestConditions;

    /**
     * @param destinationContainerName The new name of the container.
     * @param sourceContainerName The current name of the container.
     */
    public ContainerRenameOptions(String destinationContainerName, String sourceContainerName) {
        Objects.requireNonNull(destinationContainerName);
        Objects.requireNonNull(sourceContainerName);
        this.destinationContainerName = destinationContainerName;
        this.sourceContainerName = sourceContainerName;
    }

    /**
     * @return The new name of the container.
     */
    public String getDestinationContainerName() {
        return destinationContainerName;
    }

    /**
     * @return The current name of the container.
     */
    public String getSourceContainerName() {
        return sourceContainerName;
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
     */
    public ContainerRenameOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
