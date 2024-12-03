// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options that may be passed when restoring a file system.
 */
@Fluent
public final class FileSystemUndeleteOptions {
    private final String deletedFileSystemName;
    private final String deletedFileSystemVersion;
    private String destinationFileSystemName;

    /**
     * Constructs a {@link FileSystemUndeleteOptions}.
     *
     * @param deletedFileSystemName The name of the previously deleted file system.
     * @param deletedFileSystemVersion The version of the previously deleted file system.
     */
    public FileSystemUndeleteOptions(String deletedFileSystemName, String deletedFileSystemVersion) {
        StorageImplUtils.assertNotNull("deletedFileSystemName", deletedFileSystemName);
        StorageImplUtils.assertNotNull("deletedFileSystemVersion", deletedFileSystemVersion);
        this.deletedFileSystemName = deletedFileSystemName;
        this.deletedFileSystemVersion = deletedFileSystemVersion;
    }

    /**
     * Gets the deleted file system name.
     *
     * @return The name of the previously deleted file system.
     */
    public String getDeletedFileSystemName() {
        return deletedFileSystemName;
    }

    /**
     * Gets the deleted file system version.
     *
     * @return The version of the previously deleted file system.
     */
    public String getDeletedFileSystemVersion() {
        return deletedFileSystemVersion;
    }

    /**
     * Gets the destination file system name.
     * The restored file system
     * will be renamed to the <code>destinationFileSystemName</code>.
     * If the file system associated with provided <code>destinationFileSystemName</code>
     * already exists, the undelete operation will result in a 409 (conflict).
     *
     * @return The destination file system name.
     * @deprecated Destination file system name must match deleted file system name
     */
    @Deprecated
    public String getDestinationFileSystemName() {
        return destinationFileSystemName;
    }

    /**
     * Sets the destination file system name.
     * The restored file system
     * will be renamed to the <code>destinationFileSystemName</code>.
     * If the file system associated with provided <code>destinationFileSystemName</code>
     * already exists, the undelete operation will result in a 409 (conflict).
     *
     * @param destinationFileSystemName The destination file system name.
     * @return The updated options.
     * @deprecated Destination file system name must match deleted file system name
     */
    @Deprecated
    public FileSystemUndeleteOptions setDestinationFileSystemName(String destinationFileSystemName) {
        this.destinationFileSystemName = destinationFileSystemName;
        return this;
    }
}
