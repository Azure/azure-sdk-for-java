// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.resource;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Implements {@link WritableResource} for reading and writing objects in Azure StorageAccount blob. An instance of this
 * class represents a handle to a blob.
 *
 * @author Warren Zhu
 */
public class StorageBlobResource extends AzureStorageResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageBlobResource.class);
    private static final String MSG_FAIL_GET = "Failed to get blob or container";
    private static final String MSG_FAIL_OPEN_OUTPUT = "Failed to open output stream of cloud blob";
    private static final String MSG_FAIL_CHECK_EXIST = "Failed to check existence of blob or container";
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

    public StorageBlobResource(BlobServiceClient blobServiceClient, String location) {
        this(blobServiceClient, location, true);
    }

    public StorageBlobResource(BlobServiceClient blobServiceClient, String location, Boolean autoCreateFiles) {
        this(blobServiceClient, location, autoCreateFiles, null, null, null);
    }

    public StorageBlobResource(BlobServiceClient blobServiceClient, String location, Boolean autoCreateFiles,
                               String snapshot, String versionId, String contentType) {
        assertIsAzureStorageLocation(location);
        this.autoCreateFiles = autoCreateFiles == null ? getAutoCreateFiles(location) : autoCreateFiles;
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

    private boolean getAutoCreateFiles(String location) {
        return true;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        try {
            if (!exists()) {
                if (autoCreateFiles) {
                    create();
                } else {
                    throw new FileNotFoundException("The blob was not found: " + this.location);
                }
            }
            BlockBlobOutputStreamOptions options = new BlockBlobOutputStreamOptions();
            if (StringUtils.hasText(contentType)) {
                BlobHttpHeaders blobHttpHeaders = new BlobHttpHeaders();
                blobHttpHeaders.setContentType(contentType);
                options.setHeaders(blobHttpHeaders);
            }
            return this.blockBlobClient.getBlobOutputStream(options);
        } catch (BlobStorageException e) {
            LOGGER.error(MSG_FAIL_OPEN_OUTPUT, e);
            throw new IOException(MSG_FAIL_OPEN_OUTPUT, e);
        }
    }

    @Override
    public boolean exists() {
        return this.blobContainerClient.exists() && blockBlobClient.exists();
    }

    @Override
    public URL getURL() throws IOException {
        return new URL(this.blockBlobClient.getBlobUrl());
    }

    @Override
    public File getFile() throws IOException {
        throw new UnsupportedOperationException(getDescription() + " cannot be resolved to absolute file path");
    }

    private BlobProperties getBlobProperties() {
        if (blobProperties == null) {
            blobProperties = blockBlobClient.getProperties();
        }
        return blobProperties;
    }

    @Override
    public long contentLength() throws IOException {
        return getBlobProperties().getBlobSize();
    }

    @Override
    public long lastModified() throws IOException {
        return getBlobProperties().getLastModified().toEpochSecond();
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        String newLocation = this.location + "/" + relativePath;
        return new StorageBlobResource(this.blobServiceClient, newLocation, autoCreateFiles);
    }

    @Override
    public String getFilename() {
        return this.blockBlobClient.getBlobName();
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Azure storage account blob resource [container='")
          .append(this.blockBlobClient.getContainerName())
          .append("', blob='")
          .append(blockBlobClient.getBlobName())
          .append("'");
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
            assertExisted();
            return this.blockBlobClient.openInputStream();
        } catch (BlobStorageException e) {
            LOGGER.error(MSG_FAIL_OPEN_INPUT, e);
            throw new IOException(MSG_FAIL_OPEN_INPUT);
        }
    }

    @Override
    StorageType getStorageType() {
        return StorageType.BLOB;
    }

    private void assertExisted() throws FileNotFoundException {
        if (!exists()) {
            throw new FileNotFoundException("Blob or container not existed.");
        }
    }

    private void create() {
        if (!this.blobContainerClient.exists()) {
            LOGGER.debug("Blob container {} doesn't exist, now creating it",
                blobContainerClient.getBlobContainerName());
            this.blobContainerClient.create();
        }
    }
}
