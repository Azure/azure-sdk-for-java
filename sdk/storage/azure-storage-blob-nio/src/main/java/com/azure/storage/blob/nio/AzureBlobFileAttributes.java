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
 * File attributes associated with a file stored as a blob in Azure Storage.
 * <p>
 * Some of the attributes inherited from {@link BasicFileAttributes} are not supported. See the docs on each method for
 * more information.
 */
public final class AzureBlobFileAttributes implements BasicFileAttributes {
    /*
    Some blob properties do not have getters as they do not make sense in the context of nio. These properties are:
        - incremental snapshot related properties
        - lease related properties
        - sequence number
        - encryption key sha256
     */

    private final ClientLogger logger = new ClientLogger(AzureBlobFileAttributes.class);

    private final BlobProperties properties;

    AzureBlobFileAttributes(Path path) throws IOException {
        try {
            this.properties = new AzureResource(path).getBlobClient().getProperties();
        } catch (BlobStorageException e) {
            throw LoggingUtility.logError(logger, new IOException("Path: " + path.toString(), e));
        }
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
        map.put("committedBlockCount", attributes::committedBlockCount);
        map.put("isRegularFile", attributes::isRegularFile);
        map.put("isDirectory", attributes::isDirectory);
        map.put("isSymbolicLink", attributes::isSymbolicLink);
        map.put("isOther", attributes::isOther);
        map.put("size", attributes::size);
        return map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileTime creationTime() {
        return FileTime.from(this.properties.getCreationTime().toInstant());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(this.properties.getLastModified().toInstant());
    }

    /**
     * @return the eTag of the blob
     */
    public String eTag() {
        return this.properties.getETag();
    }

    /**
     * @return {@link BlobHttpHeaders}
     */
    public BlobHttpHeaders blobHttpHeaders() {
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
    }

    /**
     * @return the type of the blob
     */
    public BlobType blobType() {
        return this.properties.getBlobType();
    }

    /**
     * @return the identifier of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public String copyId() {
        return this.properties.getCopyId();
    }

    /**
     * @return the status of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public CopyStatusType copyStatus() {
        return this.properties.getCopyStatus();
    }

    /**
     * @return the source blob URL from the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public String copySource() {
        return this.properties.getCopySource();
    }

    /**
     * @return the number of bytes copied and total bytes in the source from the last copy operation (bytes copied/total
     * bytes). If this blob hasn't been the target of a copy operation or has been modified since this won't be set.
     */
    public String copyProgress() {
        return this.properties.getCopyProgress();
    }

    /**
     * @return the completion time of the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public OffsetDateTime copyCompletionTime() {
        return this.properties.getCopyCompletionTime();
    }

    /**
     * @return the description of the last copy failure, this is set when the {@link #copyStatus() getCopyStatus} is
     * {@link CopyStatusType#FAILED failed} or {@link CopyStatusType#ABORTED aborted}. If this blob hasn't been the
     * target of a copy operation or has been modified since this won't be set.
     */
    public String copyStatusDescription() {
        return this.properties.getCopyStatusDescription();
    }

    /**
     * @return the status of the blob being encrypted on the server
     */
    public Boolean isServerEncrypted() {
        return this.properties.isServerEncrypted();
    }

    /**
     * @return the tier of the blob. This is only set for Page blobs on a premium storage account or for Block blobs on
     * blob storage or general purpose V2 account.
     */
    public AccessTier accessTier() {
        return this.properties.getAccessTier();
    }

    /**
     * @return the status of the tier being inferred for the blob. This is only set for Page blobs on a premium storage
     * account or for Block blobs on blob storage or general purpose V2 account.
     */
    public Boolean isAccessTierInferred() {
        return this.properties.isAccessTierInferred();
    }

    /**
     * @return the archive status of the blob. This is only for blobs on a blob storage and general purpose v2 account.
     */
    public ArchiveStatus archiveStatus() {
        return this.properties.getArchiveStatus();
    }

    /**
     * @return the time when the access tier for the blob was last changed
     */
    public OffsetDateTime accessTierChangeTime() {
        return this.properties.getAccessTierChangeTime();
    }

    /**
     * @return the metadata associated with this blob
     */
    public Map<String, String> metadata() {
        return Collections.unmodifiableMap(this.properties.getMetadata());
    }

    /**
     * @return the number of committed blocks in the blob. This is only returned for Append blobs.
     */
    public Integer committedBlockCount() {
        return this.properties.getCommittedBlockCount();
    }

    /**
     * Unsupported.
     * {@inheritDoc}
     */
    @Override
    public FileTime lastAccessTime() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRegularFile() {
        return !this.properties.getMetadata().getOrDefault(AzureResource.DIR_METADATA_MARKER, "false").equals("true");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Will only return true if the directory is a concrete directory. See
     * {@link AzureFileSystemProvider#createDirectory(Path, FileAttribute[])} for more information on virtual and
     * concrete directories.
     */
    @Override
    public boolean isDirectory() {
        return !this.isRegularFile();
    }

    /**
     * @return false. Symbolic links are not supported.
     */
    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    /**
     * @return false
     */
    @Override
    public boolean isOther() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long size() {
        return properties.getBlobSize();
    }

    /**
     * Unsupported.
     * {@inheritDoc}
     */
    @Override
    public Object fileKey() {
        throw new UnsupportedOperationException();
    }
}
