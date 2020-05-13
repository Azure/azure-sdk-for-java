// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;

/**
 * Provides support for properties specific to Azure Blob Storage such as tier.
 *
 * {@inheritDoc}
 */
public class AzureStorageFileAttributeView implements BasicFileAttributeView {

    /**
     * Returns {@code "azureStorage"}
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "azureStorage";
    }

    @Override
    public BasicFileAttributes readAttributes() throws IOException {
        return null;
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {

    }
}
