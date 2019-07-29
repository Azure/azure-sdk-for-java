// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

/**
 * Contains file or directory reference information in the storage File service.
 */
public final class FileRef {
    private final String name;
    private final boolean isDirectory;
    private final FileProperty fileProperty;

    /**
     * Creates an instance of file or directory reference information about a specific Share.
     *
     * @param name Name of the file or the directory.
     * @param isDirectory A boolean set to true if the reference is a directory, false if the reference is a file.
     * @param fileProperty Property of a file. Pass {@code null} if the reference is a directory.
     */
    public FileRef(final String name, final boolean isDirectory, final FileProperty fileProperty) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.fileProperty = fileProperty;
    }


    /**
     * @return Name of the file or the directory.
     */
    public String name() {
        return name;
    }

    /**
     * @return True if the reference is a directory, or false if the reference is a file.
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * @return Property of a file. Return {@code null} if the reference is a directory.
     */
    public FileProperty fileProperties() {
        return fileProperty;
    }

}
