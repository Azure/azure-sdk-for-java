// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;

import java.util.Objects;

/**
 * Extended options that may be passed when renaming a file system.
 */
@Fluent
public class FileSystemRenameOptions {

    private final String destinationFileSystemName;
    private final String sourceFileSystemName;
    private DataLakeRequestConditions requestConditions;

    /**
     * @param destinationFileSystemName The new name of the file system.
     * @param sourceFileSystemName The current name of the file system.
     */
    public FileSystemRenameOptions(String destinationFileSystemName, String sourceFileSystemName) {
        Objects.requireNonNull(destinationFileSystemName);
        Objects.requireNonNull(sourceFileSystemName);
        this.destinationFileSystemName = destinationFileSystemName;
        this.sourceFileSystemName = sourceFileSystemName;
    }

    /**
     * @return The new name of the file system.
     */
    public String getDestinationFileSystemName() {
        return destinationFileSystemName;
    }

    /**
     * @return The current name of the file system.
     */
    public String getSourceFileSystemName() {
        return sourceFileSystemName;
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
     */
    public FileSystemRenameOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
