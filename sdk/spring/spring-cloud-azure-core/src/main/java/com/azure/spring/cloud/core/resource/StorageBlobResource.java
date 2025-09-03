// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Implements {@link WritableResource} for reading and writing objects in Azure StorageAccount blob. An instance of this
 * class represents a handle to a blob.
 */
public final class StorageBlobResource extends AzureStorageResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageBlobResource.class);
    private static final String MSG_FAIL_OPEN_OUTPUT = "Failed to open output stream of cloud blob";
    private static final String MSG_FAIL_OPEN_INPUT = "Failed to open input stream of blob";
    private final BlobServiceClient blobServiceClient;
    private final String location;
    private final BlobContainerClient blobContainerClient;
    private final BlockBlobClient blockBlobClient;
    private final boolean autoCreateFiles;
    private BlobProperties blobProperties;
    private final String snapshot;
    private final String versionId;
    private final String contentType;

    /**
     * Creates a new instance of {@link StorageBlobResource}.
     *
     * @param blobServiceClient the BlobServiceClient
     * @param location the location
     */
    public StorageBlobResource(BlobServiceClient blobServiceClient, String location) {
        this(blobServiceClient, location, true);
    }

    /**
     * Creates a new instance of {@link StorageBlobResource}.
     *
     * @param blobServiceClient the BlobServiceClient
     * @param location the location
     * @param autoCreateFiles whether to automatically create files
     */
    public StorageBlobResource(BlobServiceClient blobServiceClient, String location, Boolean autoCreateFiles) {
        this(blobServiceClient, location, autoCreateFiles, null, null, null);
    }

    /**
     * Creates a new instance of {@link StorageBlobResource}.
     *
     * @param blobServiceClient the BlobServiceClient
     * @param location the location
     * @param autoCreateFiles whether to automatically create files
     * @param snapshot the snapshot name
     * @param versionId the version id
     * @param contentType the content type
     */
    public StorageBlobResource(BlobServiceClient blobServiceClient, String location, Boolean autoCreateFiles,
                               String snapshot, String versionId, String contentType) {
        assertIsAzureStorageLocation(location);
        this.autoCreateFiles = autoCreateFiles == null ? isAutoCreateFiles(location) : autoCreateFiles;
        this.blobServiceClient = blobServiceClient;
        this.location = location;
        this.snapshot = snapshot;
        this.versionId = versionId;
        this.contentType = StringUtils.hasText(contentType) ? contentType : getContentType(location);
        Assert.isTrue(!(StringUtils.hasText(versionId) && StringUtils.hasText(snapshot)),
            "'versionId' and 'snapshot' can not be both set");
        this.blobContainerClient = blobServiceClient.getBlobContainerClient(getContainerName(location));
        BlobClient blobClient = blobContainerClient.getBlobClient(getFilename(location));
        if (StringUtils.hasText(versionId)) {
            blobClient = blobClient.getVersionClient(versionId);
        }
        if (StringUtils.hasText(snapshot)) {
            blobClient = blobClient.getSnapshotClient(snapshot);
        }
        this.blockBlobClient = blobClient.getBlockBlobClient();
    }

    private boolean isAutoCreateFiles(String location) {
        return true;
    }

    /**
     * Creates and opens an output stream to write data to the block blob. If the blob already exists on the service, it
     * will be overwritten.
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws IOException If a storage service error occurred or blob not found.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        try {
            if (this.autoCreateFiles) {
                this.blobContainerClient.createIfNotExists();
            }
            BlockBlobOutputStreamOptions options = new BlockBlobOutputStreamOptions();
            if (StringUtils.hasText(contentType)) {
                BlobHttpHeaders blobHttpHeaders = new BlobHttpHeaders();
                blobHttpHeaders.setContentType(contentType);
                options.setHeaders(blobHttpHeaders);
            }
            return this.blockBlobClient.getBlobOutputStream(options);
        } catch (BlobStorageException e) {
            throw new IOException(MSG_FAIL_OPEN_OUTPUT, e);
        }
    }

    /**
     * Gets if the blob this client represents exists in the cloud.
     *
     * @return true if the blob exists, false if it doesn't
     */
    @Override
    public boolean exists() {
        return blockBlobClient.exists();
    }

    /**
     * Gets the URL of the blob represented by this client.
     *
     * @return the URL.
     */
    @SuppressWarnings("deprecation")
    @Override
    public URL getURL() throws IOException {
        return new URL(this.blockBlobClient.getBlobUrl());
    }

    /**
     * This implementation throws a FileNotFoundException, assuming
     * that the resource cannot be resolved to an absolute file path.
     */
    @Override
    public File getFile() {
        throw new UnsupportedOperationException(getDescription() + " cannot be resolved to absolute file path");
    }

    private BlobProperties getBlobProperties() {
        if (blobProperties == null) {
            blobProperties = blockBlobClient.getProperties();
        }
        return blobProperties;
    }

    /**
     * @return the size of the blob in bytes
     */
    @Override
    public long contentLength() {
        return getBlobProperties().getBlobSize();
    }

    /**
     * @return the time when the blob was last modified
     */
    @Override
    public long lastModified() {
        return getBlobProperties().getLastModified().toEpochSecond();
    }

    @Override
    public Resource createRelative(String relativePath) {
        String newLocation = this.location + "/" + relativePath;
        return new StorageBlobResource(this.blobServiceClient, newLocation, autoCreateFiles);
    }

    /**
     * @return The decoded name of the blob.
     */
    @Override
    public String getFilename() {
        return this.blockBlobClient.getBlobName();
    }

    /**
     * @return a description for this resource,
     * to be used for error output when working with the resource.
     */
    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Azure storage account blob resource [container='");
        sb.append(this.blockBlobClient.getContainerName());
        sb.append("', blob='");
        sb.append(blockBlobClient.getBlobName());
        sb.append("'");
        if (versionId != null) {
            sb.append(", versionId='").append(versionId).append("'");
        }
        if (snapshot != null) {
            sb.append(", snapshot='").append(snapshot).append("'");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return this.blockBlobClient.openInputStream();
        } catch (BlobStorageException e) {
            if (e.getErrorCode() == BlobErrorCode.CONTAINER_NOT_FOUND
                || e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND) {
                throw new FileNotFoundException("Blob or container does not exist.");
            } else {
                throw new IOException(MSG_FAIL_OPEN_INPUT, e);
            }
        }
    }

    @Override
    StorageType getStorageType() {
        return StorageType.BLOB;
    }
}
