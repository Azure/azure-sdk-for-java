// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.nio.implementation.util.Utility;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.Objects;

/**
 * An {@code AzureFileStore} is backed by an Azure Blob Storage container.
 *
 * {@inheritDoc}
 */
public final class AzureFileStore extends FileStore {
    private final ClientLogger logger = new ClientLogger(AzureFileStore.class);
    private final AzureFileSystem parentFileSystem;
    private final BlobContainerClient containerClient;

    AzureFileStore(AzureFileSystem parentFileSystem, String containerName) throws IOException {
        // A FileStore should only ever be created by a FileSystem.
        if (Objects.isNull(parentFileSystem)) {
            throw Utility.logError(logger, new IllegalStateException("AzureFileStore cannot be instantiated without "
                + "a parent FileSystem"));
        }
        this.parentFileSystem = parentFileSystem;
        this.containerClient = this.parentFileSystem.getBlobServiceClient().getBlobContainerClient(containerName);

        try {
            // This also serves as our connection check.
            if (!this.containerClient.exists()) {
                this.containerClient.create();
            }
        } catch (Exception e) {
            throw Utility.logError(logger, new IOException("There was an error in establishing the existence of "
                + "container: " + containerName, e));
        }
    }

    /**
     * Returns the name of the container that underlies this file store.
     *
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
