// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;

/**
 * Extended options that may be passed when restoring a blob container.
 */
@Fluent
public class UndeleteBlobContainerOptions {
    private String destinationContainerName;

    /**
     * Gets the destination blob container name.
     * The restored container
     * will be renamed to the <code>destinationContainerName</code>.
     * If the container associated with provided <code>destinationContainerName</code>
     * already exists, the undelete operation will result in a 409 (conflict).
     *
     * @return The destination blob container name.
     */
    public String getDestinationContainerName() {
        return destinationContainerName;
    }

    /**
     * Sets the destination blob container name.
     * The restored container
     * will be renamed to the <code>destinationContainerName</code>.
     * If the container associated with provided <code>destinationContainerName</code>
     * already exists, the undelete operation will result in a 409 (conflict).
     *
     * @param destinationContainerName The destination blob container name.
     * @return The updated options.
     */
    public UndeleteBlobContainerOptions setDestinationContainerName(String destinationContainerName) {
        this.destinationContainerName = destinationContainerName;
        return this;
    }
}
