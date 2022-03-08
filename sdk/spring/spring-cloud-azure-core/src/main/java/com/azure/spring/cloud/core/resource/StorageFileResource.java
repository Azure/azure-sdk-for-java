// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import com.azure.core.util.Context;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.StorageFileOutputStream;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Implements {@link WritableResource} for reading and writing objects in Azure StorageAccount file. An instance of this
 * class represents a handle to a file.
 */
public final class StorageFileResource extends AzureStorageResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageFileResource.class);
    private static final String MSG_FAIL_OPEN_OUTPUT = "Failed to open output stream of file";
    private final ShareServiceClient shareServiceClient;
    private final ShareClient shareClient;
    private final ShareFileClient shareFileClient;
    private final String location;
    private final boolean autoCreateFiles;
    private final String contentType;

    /**
     * Creates a new instance of {@link StorageFileResource}.
     *
     * @param shareServiceClient the ShareServiceClient
     * @param location the location
     */
    public StorageFileResource(ShareServiceClient shareServiceClient, String location) {
        this(shareServiceClient, location, false);
    }

    /**
     * Creates a new instance of {@link StorageFileResource}.
     *
     * @param shareServiceClient the ShareServiceClient
     * @param location the location
     * @param autoCreateFiles whether to automatically create files
     */
    public StorageFileResource(ShareServiceClient shareServiceClient, String location, boolean autoCreateFiles) {
        this(shareServiceClient, location, autoCreateFiles, null);
    }

    /**
     * Creates a new instance of {@link StorageFileResource}.
     *
     * @param shareServiceClient the ShareServiceClient
     * @param location the location
     * @param autoCreateFiles whether to automatically create files
     * @param contentType the content type
     */
    public StorageFileResource(ShareServiceClient shareServiceClient, String location, boolean autoCreateFiles,
                               String contentType) {
        assertIsAzureStorageLocation(location);
        this.autoCreateFiles = autoCreateFiles;
        this.location = location;
        this.shareServiceClient = shareServiceClient;

        this.shareClient = shareServiceClient.getShareClient(getContainerName(location));
        this.shareFileClient = shareClient.getFileClient(getFilename(location));
        this.contentType = StringUtils.hasText(contentType) ? contentType : getContentType(location);
    }

    /**
     * Checks whether an Azure Storage File can be opened,
     * if the file is not existed, and autoCreateFiles==true,
     * it will create the file on Azure Storage.
     * @return A {@link StorageFileOutputStream} object used to write data to the file.
     * @throws IOException when fail to open the output stream.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        try {
            if (!exists()) {
                if (autoCreateFiles) {
                    create();
                } else {
                    throw new FileNotFoundException("The file was not found: " + this.location);
                }
            }
            return this.shareFileClient.getFileOutputStream();
        } catch (ShareStorageException e) {
            LOGGER.error(MSG_FAIL_OPEN_OUTPUT, e);
            throw new IOException(MSG_FAIL_OPEN_OUTPUT, e);
        }
    }

    /**
     * Determines if the file this client represents exists in the cloud.
     *
     * @return Flag indicating existence of the file.
     */
    @Override
    public boolean exists() {
        return this.shareClient.exists() && shareFileClient.exists();
    }

    /**
     * Get the url of the storage file client.
     *
     * @return the URL of the storage file client.
     */
    @Override
    public URL getURL() throws IOException {
        return new URL(this.shareFileClient.getFileUrl());
    }

    /**
     * This implementation throws a FileNotFoundException, assuming
     * that the resource cannot be resolved to an absolute file path.
     */
    @Override
    public File getFile() {
        throw new UnsupportedOperationException(getDescription() + " cannot be resolved to absolute file path");
    }

    /**
     * @return The number of bytes present in the response body.
     */
    @Override
    public long contentLength() {
        return this.shareFileClient.getProperties().getContentLength();
    }

    /**
     *
     * @return Last time the directory was modified.
     */
    @Override
    public long lastModified() {
        return this.shareFileClient.getProperties().getLastModified().toEpochSecond() * 1000;
    }

    /**
     * Create relative resource from current location.
     *
     * @param relativePath the relative path.
     * @return StorageFileResource with relative path from current location.
     */
    @Override
    public Resource createRelative(String relativePath) {
        String newLocation = this.location + "/" + relativePath;
        return new StorageFileResource(this.shareServiceClient, newLocation, autoCreateFiles);
    }

    /**
     * @return The name of the file.
     */
    @Override
    public String getFilename() {
        final String[] split = this.shareFileClient.getFilePath().split("/");
        return split[split.length - 1];
    }

    /**
     * @return a description for this resource,
     * to be used for error output when working with the resource.
     */
    @Override
    public String getDescription() {
        return String.format("Azure storage account file resource [container='%s', file='%s']",
            this.shareFileClient.getShareName(), this.getFilename());
    }

    /**
     * Opens a file input stream to download the file.
     *
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the file.
     * @throws IOException If a storage service error occurred or not existed.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        try {
            assertExisted();
            return this.shareFileClient.openInputStream();
        } catch (ShareStorageException e) {
            LOGGER.error("Failed to open input stream of cloud file", e);
            throw new IOException("Failed to open input stream of cloud file");
        }
    }

    @Override
    StorageType getStorageType() {
        return StorageType.FILE;
    }

    private void assertExisted() throws FileNotFoundException {
        if (!exists()) {
            throw new FileNotFoundException("File or container not existed.");
        }
    }

    private void create() throws ShareStorageException {
        if (!shareClient.exists()) {
            this.shareClient.create();
        }
        if (!shareFileClient.exists()) {
            ShareFileHttpHeaders header = null;
            if (StringUtils.hasText(contentType)) {
                header = new ShareFileHttpHeaders();
                header.setContentType(contentType);
            }
            //TODO: create method must provide file length, but we don't know actual
            this.shareFileClient.createWithResponse(1024, header, null, null, null, null, Context.NONE).getValue();
        }
    }
}
