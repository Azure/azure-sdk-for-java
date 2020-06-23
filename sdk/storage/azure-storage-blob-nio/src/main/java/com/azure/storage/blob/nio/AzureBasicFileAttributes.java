// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class AzureBasicFileAttributes implements BasicFileAttributes {
    private final ClientLogger logger = new ClientLogger(AzureBasicFileAttributes.class);

    // TODO: Add is virtual directory method to the AzureStorage ones
    // TODO: Should also implement UserDefined
    // Should have a way for people to know if it's a virtual or concrete directory. Shouldn't require an extra network call
    // Could leverage the checkDirectoryStatus list call to get properties out, but that requires refactoring all of that and
    // ensuring that the first element in the list for a directory is always the one we want.
    // I think for now we can just do a get properties and if that throws a 404, we can check the directory status. Directories
    // Don't really have very many properties anyway, so making two calls for virtual directories isn't terrible I suppose.
    // And we can always optimize later.

    private final BlobProperties properties;

    AzureBasicFileAttributes(Path path) throws IOException {
        try {
            this.properties = new AzureResource(path).getBlobClient().getProperties();
        } catch (BlobStorageException e) {
            throw LoggingUtility.logError(logger, new IOException("The specified file was not found. This could be "
                + "because the file does not exist or because it is a virtual directory. Path: " + path.toString(), e));
        }
    }

    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(properties.getLastModified().toInstant());
    }

    @Override
    public FileTime lastAccessTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileTime creationTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRegularFile() {
        return !this.properties.getMetadata().getOrDefault(AzureResource.DIR_METADATA_MARKER, "false").equals("true");
    }

    @Override
    public boolean isDirectory() {
        return !this.isRegularFile();
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
