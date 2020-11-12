// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.annotation.Immutable;

import com.azure.core.util.CoreUtils;
import com.azure.storage.common.implementation.Constants;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * This class contains the response information returned from the service when getting path properties.
 */
@Immutable
public class PathProperties {
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
    private final Boolean isIncrementalCopy;
    private final AccessTier accessTier;
    private final ArchiveStatus archiveStatus;
    private final String encryptionKeySha256;
    private final OffsetDateTime accessTierChangeTime;
    private final Map<String, String> metadata;
    private final Boolean isDirectory;
    private final OffsetDateTime expiresOn;

    /**
     * Constructs a {@link PathProperties}.
     *
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
     * @param isIncrementalCopy Flag indicating if the file was incrementally copied.
     * @param accessTier Access tier of the file.
     * @param archiveStatus Archive status of the file.
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the file on the server.
     * @param accessTierChangeTime Datetime when the access tier of the file last changed.
     * @param metadata Metadata associated with the file.
     * pass {@code null}.
     */
    public PathProperties(final OffsetDateTime creationTime, final OffsetDateTime lastModified, final String eTag,
        final long fileSize, final String contentType, final byte[] contentMd5, final String contentEncoding,
        final String contentDisposition, final String contentLanguage, final String cacheControl,
        final LeaseStatusType leaseStatus, final LeaseStateType leaseState, final LeaseDurationType leaseDuration,
        final String copyId, final CopyStatusType copyStatus, final String copySource, final String copyProgress,
        final OffsetDateTime copyCompletionTime, final String copyStatusDescription, final Boolean isServerEncrypted,
        final Boolean isIncrementalCopy, final AccessTier accessTier, final ArchiveStatus archiveStatus,
        final String encryptionKeySha256, final OffsetDateTime accessTierChangeTime,
        final Map<String, String> metadata) {
        this(creationTime, lastModified, eTag, fileSize, contentType, contentMd5, contentEncoding, contentDisposition,
            contentLanguage, cacheControl, leaseStatus, leaseState, leaseDuration, copyId, copyStatus, copySource,
            copyProgress, copyCompletionTime, copyStatusDescription, isServerEncrypted, isIncrementalCopy, accessTier,
            archiveStatus, encryptionKeySha256, accessTierChangeTime, metadata, null);
    }

    /**
     * Constructs a {@link PathProperties}.
     *
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
     * @param isIncrementalCopy Flag indicating if the file was incrementally copied.
     * @param accessTier Access tier of the file.
     * @param archiveStatus Archive status of the file.
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the file on the server.
     * @param accessTierChangeTime Datetime when the access tier of the file last changed.
     * @param metadata Metadata associated with the file.
     * pass {@code null}.
     * @param expiresOn the time when the path is going to expire.
     */
    public PathProperties(final OffsetDateTime creationTime, final OffsetDateTime lastModified, final String eTag,
        final long fileSize, final String contentType, final byte[] contentMd5, final String contentEncoding,
        final String contentDisposition, final String contentLanguage, final String cacheControl,
        final LeaseStatusType leaseStatus, final LeaseStateType leaseState, final LeaseDurationType leaseDuration,
        final String copyId, final CopyStatusType copyStatus, final String copySource, final String copyProgress,
        final OffsetDateTime copyCompletionTime, final String copyStatusDescription, final Boolean isServerEncrypted,
        final Boolean isIncrementalCopy, final AccessTier accessTier, final ArchiveStatus archiveStatus,
        final String encryptionKeySha256, final OffsetDateTime accessTierChangeTime,
        final Map<String, String> metadata, final OffsetDateTime expiresOn) {
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
        this.isIncrementalCopy = isIncrementalCopy;
        this.accessTier = accessTier;
        this.archiveStatus = archiveStatus;
        this.encryptionKeySha256 = encryptionKeySha256;
        this.accessTierChangeTime = accessTierChangeTime;
        this.metadata = metadata;
        /* Default isDirectory to false. */
        if (this.metadata == null) {
            this.isDirectory = false;
        } else {
            this.isDirectory = Boolean.parseBoolean(metadata.get(Constants.HeaderConstants.DIRECTORY_METADATA_KEY));
        }
        this.expiresOn = expiresOn;
    }
    /**
     * @return the time when the path was created
     */
    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * @return the time when the path was last modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return the eTag of the path
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the size of the path in bytes
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * @return the content type of the path
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return the MD5 of the path's content
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * @return the content encoding of the path
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * @return the content disposition of the path
     */
    public String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * @return the content language of the path
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * @return the cache control of the path
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * @return the lease status of the path
     */
    public LeaseStatusType getLeaseStatus() {
        return leaseStatus;
    }

    /**
     * @return the lease state of the path
     */
    public LeaseStateType getLeaseState() {
        return leaseState;
    }

    /**
     * @return the lease duration if the path is leased
     */
    public LeaseDurationType getLeaseDuration() {
        return leaseDuration;
    }

    /**
     * @return the identifier of the last copy operation. If this path hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public String getCopyId() {
        return copyId;
    }

    /**
     * @return the status of the last copy operation. If this path hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public CopyStatusType getCopyStatus() {
        return copyStatus;
    }

    /**
     * @return the source path URL from the last copy operation. If this path hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public String getCopySource() {
        return copySource;
    }

    /**
     * @return the number of bytes copied and total bytes in the source from the last copy operation (bytes copied/total
     * bytes). If this path hasn't been the target of a copy operation or has been modified since this won't be set.
     */
    public String getCopyProgress() {
        return copyProgress;
    }

    /**
     * @return the completion time of the last copy operation. If this path hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public OffsetDateTime getCopyCompletionTime() {
        return copyCompletionTime;
    }

    /**
     * @return the description of the last copy failure, this is set when the {@link #getCopyStatus() getCopyStatus} is
     * {@link CopyStatusType#FAILED failed} or {@link CopyStatusType#ABORTED aborted}. If this path hasn't been the
     * target of a copy operation or has been modified since this won't be set.
     */
    public String getCopyStatusDescription() {
        return copyStatusDescription;
    }

    /**
     * @return the status of the path being encrypted on the server
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * @return the status of the path being an incremental copy file
     */
    public Boolean isIncrementalCopy() {
        return isIncrementalCopy;
    }

    /**
     * @return the tier of the path.
     */
    public AccessTier getAccessTier() {
        return accessTier;
    }

    /**
     * @return the archive status of the path.
     */
    public ArchiveStatus getArchiveStatus() {
        return archiveStatus;
    }

    /**
     * @return the key used to encrypt the path
     */
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * @return the time when the access tier for the path was last changed
     */
    public OffsetDateTime getAccessTierChangeTime() {
        return accessTierChangeTime;
    }

    /**
     * @return the metadata associated to this path
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @return whether or not this path represents a directory
     */
    public Boolean isDirectory() {
        return isDirectory;
    }

    /**
     * @return the time when the path is going to expire.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }
}
