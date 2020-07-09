// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;

/**
 * Provides support for basic file attributes.
 *
 * The operations supported by this view and the attributes it reads are a strict subset of
 * {@link AzureBlobFileAttributeView} and has the same network behavior. Therefore, while this type is offered for
 * compliance with the NIO spec, {@link AzureBlobFileAttributeView} is generally preferred.
 *
 * {@link #setTimes(FileTime, FileTime, FileTime)} is not supported.
 *
 * {@inheritDoc}
 */
public final class AzureBasicFileAttributeView implements BasicFileAttributeView {

    private final Path path;

    AzureBasicFileAttributeView(Path path) {
        this.path = path;
    }

    /**
     * Returns {@code "azureBasic"}
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "azureBasic";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureBasicFileAttributes readAttributes() throws IOException {
        return new AzureBasicFileAttributes(path);
    }

    /**
     * Unsupported.
     *
     * @throws UnsupportedOperationException Operation not supported.
     * {@inheritDoc}
     */
    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
        throw new UnsupportedOperationException();
    }
}
