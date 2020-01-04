// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.storage.blob.BlobContainerClient;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

/**
 * {@inheritDoc}
 */
public class AzureFileStore extends FileStore {
    private final AzureFileSystem parentFileSystem;
    private final BlobContainerClient containerClient;

    AzureFileStore(AzureFileSystem parentFileSystem, String containerName) throws IOException {
        this.parentFileSystem = parentFileSystem;
        this.containerClient = this.parentFileSystem.getBlobServiceClient().getBlobContainerClient(containerName);

        try {
            if (!this.containerClient.exists()) {
                this.containerClient.create();
            }
        } catch (Exception e) {
            throw new IOException("There was an error in establishing the existence of container: " + containerName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.containerClient.getBlobContainerName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String type() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadOnly() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTotalSpace() throws IOException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getUsableSpace() throws IOException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getUnallocatedSpace() throws IOException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> aClass) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsFileAttributeView(String s) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> aClass) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(String s) throws IOException {
        return null;
    }
}
