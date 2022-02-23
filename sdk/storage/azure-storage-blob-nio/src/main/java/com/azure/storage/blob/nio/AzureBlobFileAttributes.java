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

    private final ClientLogger logger = new ClientLogger(AzureBlobFileAttributes.class);

    private final BlobProperties properties;
    private final AzureResource resource;
    private final boolean isVirtualDirectory;

    AzureBlobFileAttributes(Path path) throws IOException {
        this.resource = new AzureResource(path);
        BlobProperties props = null;
        try {
            props = resource.getBlobClient().getProperties();
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 404 && this.resource.checkDirectoryExists()) {
                this.isVirtualDirectory = true;
                this.properties = null;
                return;
            } else {
                throw LoggingUtility.logError(logger, new IOException("Path: " + path.toString(), e));
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
     * @return The creation time or
     */
    @Override
    public FileTime creationTime() {
        if (!this.isVirtualDirectory) {
            return FileTime.from(this.properties.getCreationTime().toInstant());
        } else {
            return null;
        }
    }

    /**
     * Returns the time of last modification. Returns null if this is a virtual directory
     *
     * @return the time of last modification or null if this is a virtual directory
     */
    @Override
    public FileTime lastModifiedTime() {
        if (!this.isVirtualDirectory) {
            return FileTime.from(this.properties.getLastModified().toInstant());
        } else {
            return null;
        }
    }

    /**
     * Returns the eTag of the blob or null if this is a virtual directory
     *
     * @return the eTag of the blob or null if this is a virtual directory
     */
    public String eTag() {
        if (!this.isVirtualDirectory) {
            return this.properties.getETag();
        } else {
            return null;
        }
    }

    /**
     * Returns the {@link BlobHttpHeaders} of the blob or null if this is a virtual directory.
     *
     * @return {@link BlobHttpHeaders} or null if this is a virtual directory
     */
    public BlobHttpHeaders blobHttpHeaders() {
        if (!this.isVirtualDirectory) {
        /*
        We return these all as one value so it's consistent with the way of setting, especially the setAttribute method
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
        } else {
            return null;
        }
    }

    /**
     * Returns the type of the blob or null if this is a virtual directory
     *
     * @return the type of the blob or null if this is a virtual directory
     */
    public BlobType blobType() {
        if (!this.isVirtualDirectory) {
            return this.properties.getBlobType();
        } else {
            return null;
        }
    }

    /**
     * Returns the identifier of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set. Returns null if this is a virtual directory
     *
     * @return the identifier of the last copy operation or null if this is a virtual directory
     */
    public String copyId() {
        if (!this.isVirtualDirectory) {
            return this.properties.getCopyId();
        } else {
            return null;
        }
    }

    /**
     * Returns the status of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set. Returns null if this is a virtual directory
     *
     * @return the status of the last copy operation or null if this is a virtual directory
     */
    public CopyStatusType copyStatus() {
        if (!this.isVirtualDirectory) {
            return this.properties.getCopyStatus();
        } else {
            return null;
        }
    }

    /**
     * Returns the source blob URL from the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set. Returns null if this is a virtual directory
     *
     * @return the source blob URL from the last copy operation or null if this is a virtual directory
     */
    public String copySource() {
        if (!this.isVirtualDirectory) {
            return this.properties.getCopySource();
        } else {
            return null;
        }
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
        if (!this.isVirtualDirectory) {
            return this.properties.getCopyProgress();
        } else {
            return null;
        }
    }

    /**
     * Returns the completion time of the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set. Returns null if this is a virtual directory.
     *
     * @return the completion time of the last copy operation or null if this is a virtual directory
     */
    public OffsetDateTime copyCompletionTime() {
        if (!this.isVirtualDirectory) {
            return this.properties.getCopyCompletionTime();
        } else {
            return null;
        }
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
        if (!this.isVirtualDirectory) {
            return this.properties.getCopyStatusDescription();
        } else {
            return null;
        }
    }

    /**
     * Returns the status of the blob being encrypted on the server or null if this is a virtual directory.
     *
     * @return the status of the blob being encrypted on the server or null if this is a virtual directory
     */
    public Boolean isServerEncrypted() {
        if (!this.isVirtualDirectory) {
            return this.properties.isServerEncrypted();
        } else {
            return null;
        }
    }

    /**
     * Returns the tier of the blob. This is only set for Page blobs on a premium storage account or for Block blobs on
     * blob storage or general purpose V2 account. Returns null if this is a virtual directory.
     *
     * @return the tier of the blob or null if this is a virtual directory
     */
    public AccessTier accessTier() {
        if (!this.isVirtualDirectory) {
            return this.properties.getAccessTier();
        } else {
            return null;
        }
    }

    /**
     * Returns the status of the tier being inferred for the blob. This is only set for Page blobs on a premium storage
     * account or for Block blobs on blob storage or general purpose V2 account. Returns null if this is a virtual
     * directory.
     *
     * @return the status of the tier being inferred for the blob or null if this is a virtual directory
     */
    public Boolean isAccessTierInferred() {
        if (!this.isVirtualDirectory) {
            return this.properties.isAccessTierInferred();
        } else {
            return null;
        }
    }

    /**
     * Returns the archive status of the blob. This is only for blobs on a blob storage and general purpose v2 account.
     * Returns null if this is a virtual directory.
     *
     * @return the archive status of the blob or null if this is a virtual directory
     */
    public ArchiveStatus archiveStatus() {
        if (!this.isVirtualDirectory) {
            return this.properties.getArchiveStatus();
        } else {
            return null;
        }
    }

    /**
     * Returns the time when the access tier for the blob was last changed or null if this is a virtual directory.
     *
     * @return the time when the access tier for the blob was last changed or null if this is a virtual directory
     */
    public OffsetDateTime accessTierChangeTime() {
        if (!this.isVirtualDirectory) {
            return this.properties.getAccessTierChangeTime();
        } else {
            return null;
        }
    }

    /**
     * Returns the metadata associated with this blob or null if this is a virtual directory.
     *
     * @return the metadata associated with this blob or null if this is a virtual directory
     */
    public Map<String, String> metadata() {
        if (!this.isVirtualDirectory) {
            return Collections.unmodifiableMap(this.properties.getMetadata());
        } else {
            return null;
        }
    }

    /**
     * Returns the time of last modification or null if this is a virtual directory.
     * <p>
     * Last access time is not supported by the blob service. In this case, it is typical for implementations to return
     * the {@link #lastModifiedTime()}.
     *
     * @return the time of last modification null if this is a virtual directory
     */
    @Override
    public FileTime lastAccessTime() {
        if (!this.isVirtualDirectory) {
            return this.lastModifiedTime();
        } else {
            return null;
        }
    }

    /**
     * Tells whether the file is a regular file with opaque content.
     *
     * @return whether the file is a regular file.
     */
    @Override
    public boolean isRegularFile() {
        if (!this.isVirtualDirectory) {
            return !this.properties.getMetadata().getOrDefault(AzureResource.DIR_METADATA_MARKER, "false").equals("true");
        } else {
            return false;
        }
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
     * Returns the size of the file (in bytes) or null if this is a virtual directory.
     *
     * @return the size of the file or null if this is a virtual directory
     */
    @Override
    public long size() {
        if (!this.isVirtualDirectory) {
            return properties.getBlobSize();
        } else {
            return -1;
        }
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
