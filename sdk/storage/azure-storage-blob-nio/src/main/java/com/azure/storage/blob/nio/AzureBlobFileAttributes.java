// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.ArchiveStatus;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.CopyStatusType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Provides support for attributes associated with a file stored as a blob in Azure Storage.
 * <p>
 * Some of the attributes inherited from {@link BasicFileAttributes} are not supported. See the docs on each method for
 * more information.
 * <p>
 * If the target file is a virtual directory, most attributes will be set to null.
 */
public final class AzureBlobFileAttributes implements BasicFileAttributes {
    /*
    Some blob properties do not have getters as they do not make sense in the context of nio. These properties are:
        - incremental snapshot related properties (only for page blobs)
        - lease related properties (leases not currently supported)
        - sequence number (only for page blobs)
        - encryption key sha256 (cpk not supported)
        - committed block count (only for append blobs)
     */

    private static final ClientLogger LOGGER = new ClientLogger(AzureBlobFileAttributes.class);

    private final BlobProperties properties;
    private final AzureResource resource;
    private final boolean isVirtualDirectory;

    AzureBlobFileAttributes(Path path) throws IOException {
        this.resource = new AzureResource(path);
        BlobProperties props = null;
        try {
            props = resource.getBlobClient().getProperties();
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 404 && this.resource.checkVirtualDirectoryExists()) {
                this.isVirtualDirectory = true;
                this.properties = null;
                return;
            } else {
                throw LoggingUtility.logError(LOGGER, new IOException("Path: " + path.toString(), e));
            }
        }
        this.properties = props;
        this.isVirtualDirectory = false;
    }

    static Map<String, Supplier<Object>> getAttributeSuppliers(AzureBlobFileAttributes attributes) {
        Map<String, Supplier<Object>> map = new HashMap<>();
        map.put("creationTime", attributes::creationTime);
        map.put("lastModifiedTime", attributes::lastModifiedTime);
        map.put("eTag", attributes::eTag);
        map.put("blobHttpHeaders", attributes::blobHttpHeaders);
        map.put("blobType", attributes::blobType);
        map.put("copyId", attributes::copyId);
        map.put("copyStatus", attributes::copyStatus);
        map.put("copySource", attributes::copySource);
        map.put("copyProgress", attributes::copyProgress);
        map.put("copyCompletionTime", attributes::copyCompletionTime);
        map.put("copyStatusDescription", attributes::copyStatusDescription);
        map.put("isServerEncrypted", attributes::isServerEncrypted);
        map.put("accessTier", attributes::accessTier);
        map.put("isAccessTierInferred", attributes::isAccessTierInferred);
        map.put("archiveStatus", attributes::archiveStatus);
        map.put("accessTierChangeTime", attributes::accessTierChangeTime);
        map.put("metadata", attributes::metadata);
        map.put("isRegularFile", attributes::isRegularFile);
        map.put("isDirectory", attributes::isDirectory);
        map.put("isVirtualDirectory", attributes::isVirtualDirectory);
        map.put("isSymbolicLink", attributes::isSymbolicLink);
        map.put("isOther", attributes::isOther);
        map.put("size", attributes::size);
        return map;
    }

    /**
     * Returns the creation time. The creation time is the time that the file was created. Returns null if this is a
     * virtual directory.
     *
     * @return The creation time or null if this is a virtual directory
     */
    @Override
    public FileTime creationTime() {
        return !this.isVirtualDirectory ? FileTime.from(this.properties.getCreationTime().toInstant()) : null;
    }

    /**
     * Returns the time of last modification. Returns null if this is a virtual directory
     *
     * @return the time of last modification or null if this is a virtual directory
     */
    @Override
    public FileTime lastModifiedTime() {
        return !this.isVirtualDirectory ? FileTime.from(this.properties.getLastModified().toInstant()) : null;
    }

    /**
     * Returns the eTag of the blob or null if this is a virtual directory
     *
     * @return the eTag of the blob or null if this is a virtual directory
     */
    public String eTag() {
        return !this.isVirtualDirectory ? this.properties.getETag() : null;
    }

    /**
     * Returns the {@link BlobHttpHeaders} of the blob or null if this is a virtual directory.
     *
     * @return {@link BlobHttpHeaders} or null if this is a virtual directory
     */
    public BlobHttpHeaders blobHttpHeaders() {
        if (this.isVirtualDirectory) {
            return null;
        }
        /*
        We return these all as one value, so it's consistent with the way of setting, especially the setAttribute method
        that accepts a string argument for the name of the property. Returning them individually would mean we have to
        support setting them individually as well, which is not possible due to service constraints.
         */
        return new BlobHttpHeaders()
            .setContentType(this.properties.getContentType())
            .setContentLanguage(this.properties.getContentLanguage())
            .setContentMd5(this.properties.getContentMd5())
            .setContentDisposition(this.properties.getContentDisposition())
            .setContentEncoding(this.properties.getContentEncoding())
            .setCacheControl(this.properties.getCacheControl());
    }

    /**
     * Returns the type of the blob or null if this is a virtual directory
     *
     * @return the type of the blob or null if this is a virtual directory
     */
    public BlobType blobType() {
        return !this.isVirtualDirectory ? this.properties.getBlobType() : null;
    }

    /**
     * Returns the identifier of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set. Returns null if this is a virtual directory
     *
     * @return the identifier of the last copy operation or null if this is a virtual directory
     */
    public String copyId() {
        return !this.isVirtualDirectory ? this.properties.getCopyId() : null;
    }

    /**
     * Returns the status of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set. Returns null if this is a virtual directory
     *
     * @return the status of the last copy operation or null if this is a virtual directory
     */
    public CopyStatusType copyStatus() {
        return !this.isVirtualDirectory ? this.properties.getCopyStatus() : null;
    }

    /**
     * Returns the source blob URL from the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set. Returns null if this is a virtual directory
     *
     * @return the source blob URL from the last copy operation or null if this is a virtual directory
     */
    public String copySource() {
        return !this.isVirtualDirectory ? this.properties.getCopySource() : null;
    }

    /**
     * Returns the number of bytes copied and total bytes in the source from the last copy operation (bytes copied/total
     * bytes). If this blob hasn't been the target of a copy operation or has been modified since this won't be set.
     * Returns null if this is a virtual directory
     *
     * @return the number of bytes copied and total bytes in the source from the last copy operation null if this is a
     * virtual directory
     */
    public String copyProgress() {
        return !this.isVirtualDirectory ? this.properties.getCopyProgress() : null;
    }

    /**
     * Returns the completion time of the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set. Returns null if this is a virtual directory.
     *
     * @return the completion time of the last copy operation or null if this is a virtual directory
     */
    public OffsetDateTime copyCompletionTime() {
        return !this.isVirtualDirectory ? this.properties.getCopyCompletionTime() : null;
    }

    /**
     * Returns the description of the last copy failure, this is set when the {@link #copyStatus() getCopyStatus} is
     * {@link CopyStatusType#FAILED failed} or {@link CopyStatusType#ABORTED aborted}. If this blob hasn't been the
     * target of a copy operation or has been modified since this won't be set. Returns null if this is a virtual
     * directory.
     *
     * @return the description of the last copy failure or null if this is a virtual directory
     */
    public String copyStatusDescription() {
        return !this.isVirtualDirectory ? this.properties.getCopyStatusDescription() : null;
    }

    /**
     * Returns the status of the blob being encrypted on the server or null if this is a virtual directory.
     *
     * @return the status of the blob being encrypted on the server or null if this is a virtual directory
     */
    public Boolean isServerEncrypted() {
        return !this.isVirtualDirectory ? this.properties.isServerEncrypted() : null;
    }

    /**
     * Returns the tier of the blob. This is only set for Page blobs on a premium storage account or for Block blobs on
     * blob storage or general purpose V2 account. Returns null if this is a virtual directory.
     *
     * @return the tier of the blob or null if this is a virtual directory
     */
    public AccessTier accessTier() {
        return !this.isVirtualDirectory ? this.properties.getAccessTier() : null;
    }

    /**
     * Returns the status of the tier being inferred for the blob. This is only set for Page blobs on a premium storage
     * account or for Block blobs on blob storage or general purpose V2 account. Returns null if this is a virtual
     * directory.
     *
     * @return the status of the tier being inferred for the blob or null if this is a virtual directory
     */
    public Boolean isAccessTierInferred() {
        return !this.isVirtualDirectory ? this.properties.isAccessTierInferred() : null;
    }

    /**
     * Returns the archive status of the blob. This is only for blobs on a blob storage and general purpose v2 account.
     * Returns null if this is a virtual directory.
     *
     * @return the archive status of the blob or null if this is a virtual directory
     */
    public ArchiveStatus archiveStatus() {
        return !this.isVirtualDirectory ? this.properties.getArchiveStatus() : null;
    }

    /**
     * Returns the time when the access tier for the blob was last changed or null if this is a virtual directory.
     *
     * @return the time when the access tier for the blob was last changed or null if this is a virtual directory
     */
    public OffsetDateTime accessTierChangeTime() {
        return !this.isVirtualDirectory ? this.properties.getAccessTierChangeTime() : null;
    }

    /**
     * Returns the metadata associated with this blob or null if this is a virtual directory.
     *
     * @return the metadata associated with this blob or null if this is a virtual directory
     */
    public Map<String, String> metadata() {
        return !this.isVirtualDirectory ? Collections.unmodifiableMap(this.properties.getMetadata()) : null;
    }

    /**
     * Returns the time of last modification or null if this is a virtual directory.
     * <p>
     * Last access time is not supported by the blob service. In this case, it is typical for implementations to return
     * the {@link #lastModifiedTime()}.
     *
     * @return the time of last modification or null if this is a virtual directory
     */
    @Override
    public FileTime lastAccessTime() {
        return !this.isVirtualDirectory ? FileTime.from(this.properties.getLastAccessedTime().toInstant()) : null;
    }

    /**
     * Tells whether the file is a regular file with opaque content.
     *
     * @return whether the file is a regular file.
     */
    @Override
    public boolean isRegularFile() {
        return !this.isVirtualDirectory
            && !this.properties.getMetadata().getOrDefault(AzureResource.DIR_METADATA_MARKER, "false").equals("true");
    }

    /**
     * Tells whether the file is a directory.
     * <p>
     * Will return true if the directory is a concrete or virtual directory. See
     * {@link AzureFileSystemProvider#createDirectory(Path, FileAttribute[])} for more information on virtual and
     * concrete directories.
     *
     * @return whether the file is a directory
     */
    @Override
    public boolean isDirectory() {
        return !this.isRegularFile();
    }

    /**
     * Tells whether the file is a virtual directory.
     * <p>
     * See {@link AzureFileSystemProvider#createDirectory(Path, FileAttribute[])} for more information on virtual and
     * concrete directories.
     *
     * @return whether the file is a virtual directory
     */
    public boolean isVirtualDirectory() {
        return this.isVirtualDirectory;
    }

    /**
     * Tells whether the file is a symbolic link.
     *
     * @return false. Symbolic links are not supported.
     */
    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    /**
     * Tells whether the file is something other than a regular file, directory, or symbolic link.
     *
     * @return false. No other object types are supported.
     */
    @Override
    public boolean isOther() {
        return false;
    }

    /**
     * Returns the size of the file (in bytes).
     *
     * @return the size of the file
     */
    @Override
    public long size() {
        return !this.isVirtualDirectory ? properties.getBlobSize() : 0;
    }

    /**
     * Returns the url of the resource.
     *
     * @return The file key, which is the url.
     */
    @Override
    public Object fileKey() {
        return resource.getBlobClient().getBlobUrl();
    }
}
