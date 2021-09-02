// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage.resource;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.models.ShareStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Implements {@link WritableResource} for reading and writing objects in Azure
 * StorageAccount file. An instance of this class represents a handle to a file.
 *
 * @author Warren Zhu
 */
public class FileStorageResource extends AzureStorageResource {
    private static final Logger LOG = LoggerFactory.getLogger(FileStorageResource.class);
    private static final String MSG_FAIL_GET = "Failed to get file or container";
    private static final String MSG_FAIL_OPEN_OUTPUT = "Failed to open output stream of file";
    private static final String MSG_FAIL_CHECK_EXIST = "Failed to check existence of file or container";
    private static final String MSG_FAIL_OPEN_INPUT = "Failed to open input stream of file";
    private final ShareServiceClient shareServiceClient;
    private final ShareClient shareClient;
    private final ShareFileClient shareFileClient;
    private final String location;
    private final boolean autoCreateFiles;

    public FileStorageResource(ShareServiceClient shareServiceClient, String location) {
        this(shareServiceClient, location, false);
    }

    FileStorageResource(ShareServiceClient shareServiceClient, String location, boolean autoCreateFiles) {
        assertIsAzureStorageLocation(location);
        this.autoCreateFiles = autoCreateFiles;
        this.location = location;
        this.shareServiceClient = shareServiceClient;

        this.shareClient = shareServiceClient.getShareClient(getContainerName(location));
        this.shareFileClient = shareClient.getFileClient(getFilename(location));
    }

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
            LOG.error(MSG_FAIL_OPEN_OUTPUT, e);
            throw new IOException(MSG_FAIL_OPEN_OUTPUT, e);
        }
    }

    @Override
    public boolean exists() {
        return this.shareClient.exists() && shareFileClient.exists();
    }

    @Override
    public URL getURL() throws IOException {
        return new URL(this.shareFileClient.getFileUrl());
    }

    @Override
    public File getFile() {
        throw new UnsupportedOperationException(getDescription() + " cannot be resolved to absolute file path");
    }

    @Override
    public long contentLength() {
        return this.shareFileClient.getProperties().getContentLength();
    }

    @Override
    public long lastModified() {
        return this.shareFileClient.getProperties().getLastModified().toEpochSecond() * 1000;
    }

    @Override
    public Resource createRelative(String relativePath) {
        String newLocation = this.location + "/" + relativePath;
        return new FileStorageResource(this.shareServiceClient, newLocation, autoCreateFiles);
    }

    @Override
    public String getFilename() {
        final String[] split = this.shareFileClient.getFilePath().split("/");
        return split[split.length - 1];
    }

    @Override
    public String getDescription() {
        return String.format("Azure storage account file resource [container='%s', file='%s']",
            this.shareFileClient.getShareName(), this.getFilename());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            assertExisted();
            return this.shareFileClient.openInputStream();
        } catch (ShareStorageException e) {
            LOG.error("Failed to open input stream of cloud file", e);
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
            //TODO: create method must provide file length, but we don't know actual
            this.shareFileClient.create(1024);
        }
    }
}
