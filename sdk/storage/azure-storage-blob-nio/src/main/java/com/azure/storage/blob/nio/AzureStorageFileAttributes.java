// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.storage.blob.models.BlobProperties;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class AzureStorageFileAttributes implements BasicFileAttributes {

    private final BlobProperties properties;

    AzureStorageFileAttributes(BlobProperties properties) {
        this.properties = properties;
    }

    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(properties.getLastModified().toInstant());
    }

    @Override
    public FileTime lastAccessTime() {
        return null;
    }

    @Override
    public FileTime creationTime() {
        return null;
    }

    @Override
    public boolean isRegularFile() {
        return !this.isDirectory;
    }

    @Override
    public boolean isDirectory() {
        return this.isDirectory;
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public long size() {
        return properties.getBlobSize();
    }

    @Override
    public Object fileKey() {
        return null;
    }
}
