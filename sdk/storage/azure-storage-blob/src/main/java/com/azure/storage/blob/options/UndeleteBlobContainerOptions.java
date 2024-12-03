// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options that may be passed when restoring a blob container.
 */
@Fluent
public class UndeleteBlobContainerOptions {
    private final String deletedContainerName;
    private final String deletedContainerVersion;
    private String destinationContainerName;

    /**
     * Constructs a {@link UndeleteBlobContainerOptions}.
     *
     * @param deletedContainerName The name of the previously deleted container.
     * @param deletedContainerVersion The version of the previously deleted container.
     */
    public UndeleteBlobContainerOptions(String deletedContainerName, String deletedContainerVersion) {
        StorageImplUtils.assertNotNull("deletedContainerName", deletedContainerName);
        StorageImplUtils.assertNotNull("deletedContainerVersion", deletedContainerVersion);
        this.deletedContainerName = deletedContainerName;
        this.deletedContainerVersion = deletedContainerVersion;
    }

    /**
     * Gets the deleted blob container name.
     *
     * @return The name of the previously deleted container.
     */
    public String getDeletedContainerName() {
        return deletedContainerName;
    }

    /**
     * Gets the deleted blob container version.
     *
     * @return The version of the previously deleted container.
     */
    public String getDeletedContainerVersion() {
        return deletedContainerVersion;
    }

    /**
     * Gets the destination blob container name.
     * The restored container
     * will be renamed to the <code>destinationContainerName</code>.
     * If the container associated with provided <code>destinationContainerName</code>
     * already exists, the undelete operation will result in a 409 (conflict).
     *
     * @return The destination blob container name.
     * @deprecated Destination container name must match deleted container name
     */
    @Deprecated
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
     * @deprecated Destination container name must match deleted container name
     */
    @Deprecated
    public UndeleteBlobContainerOptions setDestinationContainerName(String destinationContainerName) {
        this.destinationContainerName = destinationContainerName;
        return this;
    }
}
