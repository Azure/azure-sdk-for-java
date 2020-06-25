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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AzureBasicFileAttributes implements BasicFileAttributes {
    private final ClientLogger logger = new ClientLogger(AzureBasicFileAttributes.class);

    // For verifying parameters on FileSystemProvider.readAttributes
    static final Set<String> attributeStrings;
    static {
        Set<String> set = new HashSet<>();
        set.add("lastModifiedTime");
        set.add("isRegularFile");
        set.add("isDirectory");
        set.add("isSymbolicLink");
        set.add("isOther");
        set.add("size");
        set.add("creationTime");
        attributeStrings = Collections.unmodifiableSet(set);
    }

    private final BlobProperties properties;

    /*
    There are some work-arounds we could do to try to accommodate virtual directories such as making a checkDirStatus
    call before or after getProperties to throw an appropriate error or adding an isVirtualDirectory method. However,
    the former wastes network time only to throw a slightly more specific error when we will throw on 404 anyway. The
    latter introduces virtual directories into the actual code path/api surface. While we are clear in our docs about
    the possible pitfalls of virtual directories, and customers should be aware of it, they shouldn't have to code
    against it. Therefore, we fall back to documenting that reading attributes on a virtual directory will throw. And
    who reads attributes on a directory anyway?
     */
    AzureBasicFileAttributes(Path path) throws IOException {
        try {
            this.properties = new AzureResource(path).getBlobClient().getProperties();
        } catch (BlobStorageException e) {
            throw LoggingUtility.logError(logger, new IOException(e));
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
        return FileTime.from(properties.getCreationTime().toInstant());
    }

    @Override
    public boolean isRegularFile() {
        return !this.properties.getMetadata().getOrDefault(AzureResource.DIR_METADATA_MARKER, "false").equals("true");
    }

    /**
     * Will only return true if a marker blob exists.
     * @return
     */
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
