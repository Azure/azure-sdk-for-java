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
    private static final String AZURE_FILE_STORE_TYPE = "AzureBlobContainer";

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
     * Returns the {@code String "AzureBlobContainer"} to indicate that the file store is backed by a remote blob
     * container in Azure Storage.
     *
     * {@inheritDoc}
     */
    @Override
    public String type() {
        return AZURE_FILE_STORE_TYPE;
    }

    /**
     * Always returns false. It may be the case that the authentication method provided to this file system only
     * supports read operations and hence the file store is implicitly read only in this view, but that does not
     * imply the underlying container/file store is inherently read only. Creating/specifying read only file stores
     * is not currently supported.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean isReadOnly() {
        return false;
    }

    /**
     * Containers do not limit the amount of data stored. This method will always return max long.
     *
     * {@inheritDoc}
     */
    @Override
    public long getTotalSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    /**
     * Containers do not limit the amount of data stored. This method will always return max long.
     *
     * {@inheritDoc}
     */
    @Override
    public long getUsableSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    /**
     * Containers do not limit the amount of data stored. This method will always return max long.
     *
     * {@inheritDoc}
     */
    @Override
    public long getUnallocatedSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    /**
     * All file stores in this file system support the following views:
     * <ul>
     *     <li>{@link java.nio.file.attribute.BasicFileAttributeView}</li>
     *     <li>{@link java.nio.file.attribute.UserDefinedFileAttributeView}</li>
     *     <li>{@link AzureStorageFileAttributeView}</li>
     * </ul>
     *
     * {@inheritDoc}
     */
    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> aClass) {
        return AzureFileSystem.SUPPORTED_ATTRIBUTE_VIEWS.containsKey(aClass);
    }

    /**
     * All file stores in this file system support the following views:
     * <ul>
     *     <li>{@link java.nio.file.attribute.BasicFileAttributeView}</li>
     *     <li>{@link java.nio.file.attribute.UserDefinedFileAttributeView}</li>
     *     <li>{@link AzureStorageFileAttributeView}</li>
     * </ul>
     *
     * {@inheritDoc}
     */
    @Override
    public boolean supportsFileAttributeView(String s) {
        return AzureFileSystem.SUPPORTED_ATTRIBUTE_VIEWS.containsValue(s);
    }

    /**
     * This method always returns null as no {@link FileStoreAttributeView} is currently supported.
     *
     * {@inheritDoc}
     */
    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> aClass) {
        return null;
    }

    /**
     * This method always throws an {@code UnsupportedOperationException} as no {@link FileStoreAttributeView} is
     * currently supported.
     *
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(String s) throws IOException {
        throw new UnsupportedOperationException("FileStoreAttributeViews aren't supported.");
    }

    BlobContainerClient getContainerClient() {
        return this.containerClient;
    }
}
