// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

public final class FileRef {
    private String name;
    private boolean isDirectory;
    private FileProperty fileProperty;

    public FileRef(final String name, final boolean isDirectory, final FileProperty fileProperty) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.fileProperty = fileProperty;
    }

    public String name() {
        return name;
    }

    public void name(final String name) {
        this.name = name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void isDirectory(final boolean directory) {
        isDirectory = directory;
    }

    public FileProperty fileProperties() {
        return fileProperty;
    }

    public void fileProperty(final FileProperty fileProperty) {
        this.fileProperty = fileProperty;
    }

}
