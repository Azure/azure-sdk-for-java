// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlobClientBase;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A file attribute view that provides a view of attributes specific to files stored as blobs in Azure Storage.
 * <p>
 * All attributes are retrieved from the file system as a bulk operation.
 * <p>
 * {@link #setTimes(FileTime, FileTime, FileTime)} is not supported.
 */
public final class AzureBlobFileAttributeView implements BasicFileAttributeView {
    private final ClientLogger logger = new ClientLogger(AzureBlobFileAttributeView.class);

    static final String ATTR_CONSUMER_ERROR = "Exception thrown by attribute consumer";
    static final String NAME = "azureBlob";

    private final Path path;

    AzureBlobFileAttributeView(Path path) {
        this.path = path;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Consumer<Object>> setAttributeConsumers(AzureBlobFileAttributeView view) {
        Map<String, Consumer<Object>> map = new HashMap<>();
        map.put("blobHttpHeaders", obj -> {
            try {
                view.setBlobHttpHeaders((BlobHttpHeaders) obj);
            } catch (IOException e) {
                throw LoggingUtility.logError(view.logger, new UncheckedIOException(ATTR_CONSUMER_ERROR, e));
            }
        });
        map.put("metadata", obj -> {
            try {
                Map<String, String> m = (Map<String, String>) obj;
                if (m == null) {
                    throw LoggingUtility.logError(view.logger, new ClassCastException());
                }
                view.setMetadata(m);
            } catch (IOException e) {
                throw LoggingUtility.logError(view.logger, new UncheckedIOException(ATTR_CONSUMER_ERROR, e));
            }
        });
        map.put("tier", obj -> {
            try {
                view.setTier((AccessTier) obj);
            } catch (IOException e) {
                throw LoggingUtility.logError(view.logger, new UncheckedIOException(ATTR_CONSUMER_ERROR, e));
            }
        });

        return map;
    }

    /**
     * Returns "azureBlob".
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return NAME;
    }

    /**
     * Reads the file attributes as a bulk operation.
     *
     * Gets a fresh copy every time it is called.
     * @return {@link AzureBlobFileAttributes}
     * @throws IOException if an IOException occurs.
     */
    @Override
    public AzureBlobFileAttributes readAttributes() throws IOException {
        return new AzureBlobFileAttributes(path);
    }

    /**
     * Sets the {@link BlobHttpHeaders} as an atomic operation.
     * <p>
     * See {@link BlobClientBase#setHttpHeaders(BlobHttpHeaders)} for more information.
     * @param headers {@link BlobHttpHeaders}
     * @throws IOException if an IOException occurs.
     */
    public void setBlobHttpHeaders(BlobHttpHeaders headers) throws IOException {
        try {
            new AzureResource(this.path).getBlobClient().setHttpHeaders(headers);
        } catch (BlobStorageException e) {
            throw LoggingUtility.logError(logger, new IOException(e));
        }
    }

    /**
     * Sets the metadata as an atomic operation.
     * <p>
     * See {@link BlobClientBase#setMetadata(Map)} for more information.
     * @param metadata The metadata to associate with the blob
     * @throws IOException if an IOException occurs.
     */
    public void setMetadata(Map<String, String> metadata) throws IOException {
        try {
            new AzureResource(this.path).getBlobClient().setMetadata(metadata);
        } catch (BlobStorageException e) {
            throw LoggingUtility.logError(logger, new IOException(e));
        }
    }

    /**
     * Sets the {@link AccessTier} on the file.
     *
     * See {@link BlobClientBase#setAccessTier(AccessTier)} for more information.
     * @param tier {@link AccessTier}
     * @throws IOException if an IOException occurs.
     */
    public void setTier(AccessTier tier) throws IOException {
        try {
            new AzureResource(this.path).getBlobClient().setAccessTier(tier);
        } catch (BlobStorageException e) {
            throw LoggingUtility.logError(logger, new IOException(e));
        }
    }

    /**
     * Unsupported.
     *
     * @throws UnsupportedOperationException Operation not supported.
     * {@inheritDoc}
     */
    @Override
    public void setTimes(FileTime fileTime, FileTime fileTime1, FileTime fileTime2) throws IOException {
        throw new UnsupportedOperationException();
    }
}
