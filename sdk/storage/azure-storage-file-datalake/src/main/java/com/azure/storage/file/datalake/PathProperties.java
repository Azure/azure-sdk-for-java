// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.blob.BlobProperties;
import com.azure.storage.file.datalake.models.AccessTier;
import com.azure.storage.file.datalake.models.ArchiveStatus;
import com.azure.storage.file.datalake.models.CopyStatusType;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;

import java.time.OffsetDateTime;
import java.util.Map;

public class PathProperties {

    private final OffsetDateTime creationTime;
    private final OffsetDateTime lastModified;
    private final String eTag;
    private final long fileSize;
    private final String contentType;
    private final byte[] contentMD5;
    private final String contentEncoding;
    private final String contentDisposition;
    private final String contentLanguage;
    private final String cacheControl;
    private final LeaseStatusType leaseStatus;
    private final LeaseStateType leaseState;
    private final LeaseDurationType leaseDuration;
    private final String copyId;
    private final CopyStatusType copyStatus;
    private final String copySource;
    private final String copyProgress;
    private final OffsetDateTime copyCompletionTime;
    private final String copyStatusDescription;
    private final Boolean isServerEncrypted;
    private final Boolean isIncrementalCopy;
    private final AccessTier accessTier;
    private final ArchiveStatus archiveStatus;
    private final String encryptionKeySha256;
    private final OffsetDateTime accessTierChangeTime;
    private final Map<String, String> metadata;

    public PathProperties(BlobProperties properties) {
        this.creationTime = properties.getCreationTime();
        this.lastModified = properties.getLastModified();
        this.eTag = properties.getETag();
        this.fileSize = properties.getBlobSize();
        this.contentType = properties.getContentType();
        this.contentMD5 = properties.getContentMD5();
        this.contentEncoding = properties.getContentEncoding();
        this.contentDisposition = properties.getContentDisposition();
        this.contentLanguage = properties.getContentLanguage();
        this.cacheControl = properties.getCacheControl();
        this.leaseStatus = LeaseStatusType.fromString(properties.getLeaseStatus().toString());
        this.leaseState = LeaseStateType.fromString(properties.getLeaseState().toString());
        this.leaseDuration = LeaseDurationType.fromString(properties.getLeaseDuration().toString());
        this.copyId = properties.getCopyId();
        this.copyStatus = CopyStatusType.fromString(properties.getCopyStatus().toString());
        this.copySource = properties.getCopySource();
        this.copyProgress = properties.getCopyProgress();
        this.copyCompletionTime = properties.getCopyCompletionTime();
        this.copyStatusDescription = properties.getCopyStatusDescription();
        this.isServerEncrypted = properties.isServerEncrypted();
        this.isIncrementalCopy = properties.isIncrementalCopy();
        /* TODO (gapra) : Make sure the toString for these two return what we want since they dont have an override
          toString */
        this.accessTier = AccessTier.fromString(properties.getAccessTier().toString());
        this.archiveStatus = ArchiveStatus.fromString(properties.getArchiveStatus().toString());
        this.encryptionKeySha256 = properties.getEncryptionKeySha256();
        this.accessTierChangeTime = properties.getAccessTierChangeTime();
        this.metadata = properties.getMetadata();
    }

    /**
     * @return the time when the file was created
     */
    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * @return the time when the file was last modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return the eTag of the file
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the size of the file in bytes
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * @return the content type of the file
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return the MD5 of the file's content
     */
    public byte[] getContentMD5() {
        return ImplUtils.clone(contentMD5);
    }

    /**
     * @return the content encoding of the file
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * @return the content disposition of the file
     */
    public String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * @return the content language of the file
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * @return the cache control of the file
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * @return the lease status of the file
     */
    public LeaseStatusType getLeaseStatus() {
        return leaseStatus;
    }

    /**
     * @return the lease state of the file
     */
    public LeaseStateType getLeaseState() {
        return leaseState;
    }

    /**
     * @return the lease duration if the file is leased
     */
    public LeaseDurationType getLeaseDuration() {
        return leaseDuration;
    }

    /**
     * @return the identifier of the last copy operation. If this file hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public String getCopyId() {
        return copyId;
    }

    /**
     * @return the status of the last copy operation. If this file hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public CopyStatusType getCopyStatus() {
        return copyStatus;
    }

    /**
     * @return the source file URL from the last copy operation. If this file hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public String getCopySource() {
        return copySource;
    }

    /**
     * @return the number of bytes copied and total bytes in the source from the last copy operation (bytes copied/total
     * bytes). If this file hasn't been the target of a copy operation or has been modified since this won't be set.
     */
    public String getCopyProgress() {
        return copyProgress;
    }

    /**
     * @return the completion time of the last copy operation. If this file hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public OffsetDateTime getCopyCompletionTime() {
        return copyCompletionTime;
    }

    /**
     * @return the description of the last copy failure, this is set when the {@link #getCopyStatus() getCopyStatus} is
     * {@link CopyStatusType#FAILED failed} or {@link CopyStatusType#ABORTED aborted}. If this file hasn't been the
     * target of a copy operation or has been modified since this won't be set.
     */
    public String getCopyStatusDescription() {
        return copyStatusDescription;
    }

    /**
     * @return the status of the file being encrypted on the server
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * @return the status of the file being an incremental copy file
     */
    public Boolean isIncrementalCopy() {
        return isIncrementalCopy;
    }

    /**
     * @return the tier of the file.
     */
    public AccessTier getAccessTier() {
        return accessTier;
    }

    /**
     * @return the archive status of the file.
     */
    public ArchiveStatus getArchiveStatus() {
        return archiveStatus;
    }

    /**
     * @return the key used to encrypt the file
     */
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * @return the time when the access tier for the file was last changed
     */
    public OffsetDateTime getAccessTierChangeTime() {
        return accessTierChangeTime;
    }

    /**
     * @return the metadata associated to this file
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
}
