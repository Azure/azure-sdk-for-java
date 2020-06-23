// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;

/**
 * Provides support for properties specific to Azure Blob Storage such as tier.
 *
 * {@inheritDoc}
 */
public class AzureBasicFileAttributeView implements BasicFileAttributeView {

    private final Path path;

    AzureBasicFileAttributeView(Path path) {
        this.path = path;
    }

    /**
     * Returns {@code "azureStorage"}
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "azureBasic";
    }

    @Override
    public AzureBasicFileAttributes readAttributes() throws IOException {
        return new AzureBasicFileAttributes(path);
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
        throw new UnsupportedOperationException();
    }
}
