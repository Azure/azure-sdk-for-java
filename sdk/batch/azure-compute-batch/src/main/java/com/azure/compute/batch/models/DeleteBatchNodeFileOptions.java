// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

/**
 * Optional parameters for deleting a certain file from a certain Compute Node.
 */
public class DeleteBatchNodeFileOptions extends BatchBaseOptions {
    private Boolean recursive;

    /**
     * Gets a value indicating whether to delete children of a directory. If the filePath parameter of deleteNodeFile represents a directory
     * instead of a file, you can set recursive to true to delete the directory and all of the files and
     * subdirectories in it. If recursive is false then the directory must be empty or deletion will fail.
     *
     * @return A value indicating whether to delete children of a directory.
     */
    public Boolean isRecursive() {
        return recursive;
    }

    /**
     * Sets a value indicating whether to delete children of a directory. If the filePath parameter of deleteNodeFile represents a directory
     * instead of a file, you can set recursive to true to delete the directory and all of the files and
     * subdirectories in it. If recursive is false then the directory must be empty or deletion will fail.
     *
     * @param recursive A value indicating whether to delete children of a directory.
     * @return The {@link DeleteBatchNodeFileOptions} object itself, allowing for method chaining.
     */
    public DeleteBatchNodeFileOptions setRecursive(Boolean recursive) {
        this.recursive = recursive;
        return this;
    }


}
