// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;

import java.util.Objects;

/**
 * Extended options that may be passed when renaming a file system.
 */
@Fluent
class FileSystemRenameOptions {

    private final String destinationFileSystemName;
    private DataLakeRequestConditions requestConditions;

    /**
     * @param destinationFileSystemName The new name of the file system.
     */
    FileSystemRenameOptions(String destinationFileSystemName) {
        Objects.requireNonNull(destinationFileSystemName);
        this.destinationFileSystemName = destinationFileSystemName;
    }

    /**
     * @return The new name of the file system.
     */
    public String getDestinationFileSystemName() {
        return destinationFileSystemName;
    }

    /**
     * @return {@link DataLakeRequestConditions}
     */
    public DataLakeRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return The updated options.
     * @throws UnsupportedOperationException if a condition other than lease id is set.
     */
    public FileSystemRenameOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
