// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the response information returned from the service when getting file layout.
 */
@Immutable
public final class DataLakeFileLayoutInfo {
    private final List<DataLakeFileLayoutRange> ranges;
    private final OffsetDateTime creationTime;
    private final OffsetDateTime lastModified;
    private final String eTag;
    private final long fileSize;
    private final String contentType;
    private final byte[] contentMd5;
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
    private final AccessTier accessTier;
    private final ArchiveStatus archiveStatus;
    private final String encryptionKeySha256;
    private final OffsetDateTime accessTierChangeTime;
    private final Map<String, String> metadata;
    private final OffsetDateTime expiresOn;

    /**
     * Constructs a {@link DataLakeFileLayoutInfo}.
     *
     * @param ranges The ranges in the file layout.
     * @param creationTime Creation time of the file.
     * @param lastModified Datetime when the file was last modified.
     * @param eTag ETag of the file.
     * @param fileSize Size of the file.
     * @param contentType Content type specified for the file.
     * @param contentMd5 Content MD5 specified for the file.
     * @param contentEncoding Content encoding specified for the file.
     * @param contentDisposition Content disposition specified for the file.
     * @param contentLanguage Content language specified for the file.
     * @param cacheControl Cache control specified for the file.
     * @param leaseStatus Status of the lease on the file.
     * @param leaseState State of the lease on the file.
     * @param leaseDuration Type of lease on the file.
     * @param copyId Identifier of the last copy operation performed on the file.
     * @param copyStatus Status of the last copy operation performed on the file.
     * @param copySource Source of the last copy operation performed on the file.
     * @param copyProgress Progress of the last copy operation performed on the file.
     * @param copyCompletionTime Datetime when the last copy operation on the file completed.
     * @param copyStatusDescription Description of the last copy operation on the file.
     * @param isServerEncrypted Flag indicating if the file's content is encrypted on the server.
     * @param accessTier Access tier of the file.
     * @param archiveStatus Archive status of the file.
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the file on the server.
     * @param accessTierChangeTime Datetime when the access tier of the file last changed.
     * @param metadata Metadata associated with the file.
     * @param expiresOn The time when the file is going to expire.
     */
    public DataLakeFileLayoutInfo(List<DataLakeFileLayoutRange> ranges, OffsetDateTime creationTime,
        OffsetDateTime lastModified, String eTag, long fileSize, String contentType, byte[] contentMd5,
        String contentEncoding, String contentDisposition, String contentLanguage, String cacheControl,
        LeaseStatusType leaseStatus, LeaseStateType leaseState, LeaseDurationType leaseDuration, String copyId,
        CopyStatusType copyStatus, String copySource, String copyProgress, OffsetDateTime copyCompletionTime,
        String copyStatusDescription, Boolean isServerEncrypted, AccessTier accessTier, ArchiveStatus archiveStatus,
        String encryptionKeySha256, OffsetDateTime accessTierChangeTime, Map<String, String> metadata,
        OffsetDateTime expiresOn) {
        this.ranges = ranges == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(ranges));
        this.creationTime = creationTime;
        this.lastModified = lastModified;
        this.eTag = eTag;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.contentMd5 = CoreUtils.clone(contentMd5);
        this.contentEncoding = contentEncoding;
        this.contentDisposition = contentDisposition;
        this.contentLanguage = contentLanguage;
        this.cacheControl = cacheControl;
        this.leaseStatus = leaseStatus;
        this.leaseState = leaseState;
        this.leaseDuration = leaseDuration;
        this.copyId = copyId;
        this.copyStatus = copyStatus;
        this.copySource = copySource;
        this.copyProgress = copyProgress;
        this.copyCompletionTime = copyCompletionTime;
        this.copyStatusDescription = copyStatusDescription;
        this.isServerEncrypted = isServerEncrypted;
        this.accessTier = accessTier;
        this.archiveStatus = archiveStatus;
        this.encryptionKeySha256 = encryptionKeySha256;
        this.accessTierChangeTime = accessTierChangeTime;
        this.metadata
            = metadata == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(metadata));
        this.expiresOn = expiresOn;
    }

    /**
     * Gets the ranges property.
     *
     * @return The ranges property.
     */
    public List<DataLakeFileLayoutRange> getRanges() {
        return ranges;
    }

    /**
     * Gets the time when the file was created.
     *
     * @return the time when the file was created
     */
    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * Gets the time when the file was last modified.
     *
     * @return the time when the file was last modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Gets the eTag of the file.
     *
     * @return the eTag of the file
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the size of the file in bytes.
     *
     * @return the size of the file in bytes
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Gets the content type of the file.
     *
     * @return the content type of the file
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the MD5 of the file's content.
     *
     * @return the MD5 of the file's content
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * Gets the content encoding of the file.
     *
     * @return the content encoding of the file
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Gets the content disposition of the file.
     *
     * @return the content disposition of the file
     */
    public String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * Gets the content language of the file.
     *
     * @return the content language of the file
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * Gets the cache control of the file.
     *
     * @return the cache control of the file
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * Gets the lease status of the file.
     *
     * @return the lease status of the file
     */
    public LeaseStatusType getLeaseStatus() {
        return leaseStatus;
    }

    /**
     * Gets the lease state of the file.
     *
     * @return the lease state of the file
     */
    public LeaseStateType getLeaseState() {
        return leaseState;
    }

    /**
     * Gets the lease duration if the file is leased.
     *
     * @return the lease duration if the file is leased
     */
    public LeaseDurationType getLeaseDuration() {
        return leaseDuration;
    }

    /**
     * Gets the identifier of the last copy operation.
     *
     * @return the identifier of the last copy operation. If this file hasn't been the target of a copy operation or
     * has been modified since this won't be set.
     */
    public String getCopyId() {
        return copyId;
    }

    /**
     * Gets the status of the last copy operation.
     *
     * @return the status of the last copy operation. If this file hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public CopyStatusType getCopyStatus() {
        return copyStatus;
    }

    /**
     * Gets the source file URL from the last copy operation.
     *
     * @return the source file URL from the last copy operation. If this file hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public String getCopySource() {
        return copySource;
    }

    /**
     * Gets the number of bytes copied and total bytes in the source from the last copy operation.
     *
     * @return the number of bytes copied and total bytes in the source from the last copy operation
     * (bytes copied/total bytes). If this file hasn't been the target of a copy operation or has been modified since
     * this won't be set.
     */
    public String getCopyProgress() {
        return copyProgress;
    }

    /**
     * Gets the completion time of the last copy operation.
     *
     * @return the completion time of the last copy operation. If this file hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public OffsetDateTime getCopyCompletionTime() {
        return copyCompletionTime;
    }

    /**
     * Gets the description of the last copy failure.
     *
     * @return the description of the last copy failure, this is set when the {@link #getCopyStatus() getCopyStatus} is
     * {@link CopyStatusType#FAILED failed} or {@link CopyStatusType#ABORTED aborted}. If this file hasn't been the
     * target of a copy operation or has been modified since this won't be set.
     */
    public String getCopyStatusDescription() {
        return copyStatusDescription;
    }

    /**
     * Gets the status of the file being encrypted on the server.
     *
     * @return the status of the file being encrypted on the server
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * Gets the tier of the file.
     *
     * @return the tier of the file.
     */
    public AccessTier getAccessTier() {
        return accessTier;
    }

    /**
     * Gets the archive status of the file.
     *
     * @return the archive status of the file.
     */
    public ArchiveStatus getArchiveStatus() {
        return archiveStatus;
    }

    /**
     * Gets the SHA256 of the encryption key used to encrypt the file.
     *
     * @return the key used to encrypt the file
     */
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * Gets the time when the access tier for the file was last changed.
     *
     * @return the time when the access tier for the file was last changed
     */
    public OffsetDateTime getAccessTierChangeTime() {
        return accessTierChangeTime;
    }

    /**
     * Gets the metadata associated to this file.
     *
     * @return the metadata associated to this file
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Gets the time when the file is going to expire.
     *
     * @return the time when the file is going to expire.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }
}
