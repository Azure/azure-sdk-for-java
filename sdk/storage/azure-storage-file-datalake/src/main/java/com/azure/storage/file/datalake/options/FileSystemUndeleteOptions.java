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
}
