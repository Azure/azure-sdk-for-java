// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * This class contains the response information returned from the service when getting blob properties.
 */
@Immutable
public final class BlobProperties {
    private final OffsetDateTime creationTime;
    private final OffsetDateTime lastModified;
    private final String eTag;
    private final long blobSize;
    private final String contentType;
    private final byte[] contentMd5;
    private final String contentEncoding;
    private final String contentDisposition;
    private final String contentLanguage;
    private final String cacheControl;
    private final Long blobSequenceNumber;
    private final BlobType blobType;
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
    private final String copyDestinationSnapshot;
    private final AccessTier accessTier;
    private final Boolean isAccessTierInferred;
    private final ArchiveStatus archiveStatus;
    private final String encryptionKeySha256;
    private final OffsetDateTime accessTierChangeTime;
    private final Map<String, String> metadata;
    private final Integer committedBlockCount;

    /**
     * Constructs a {@link BlobProperties}.
     *
     * @param creationTime Creation time of the blob.
     * @param lastModified Datetime when the blob was last modified.
     * @param eTag ETag of the blob.
     * @param blobSize Size of the blob.
     * @param contentType Content type specified for the blob.
     * @param contentMd5 Content MD5 specified for the blob.
     * @param contentEncoding Content encoding specified for the blob.
     * @param contentDisposition Content disposition specified for the blob.
     * @param contentLanguage Content language specified for the blob.
     * @param cacheControl Cache control specified for the blob.
     * @param blobSequenceNumber The current sequence number for a page blob, if the blob is an append or block blob
     * pass {@code null}.
     * @param blobType Type of the blob.
     * @param leaseStatus Status of the lease on the blob.
     * @param leaseState State of the lease on the blob.
     * @param leaseDuration Type of lease on the blob.
     * @param copyId Identifier of the last copy operation performed on the blob.
     * @param copyStatus Status of the last copy operation performed on the blob.
     * @param copySource Source of the last copy operation performed on the blob.
     * @param copyProgress Progress of the last copy operation performed on the blob.
     * @param copyCompletionTime Datetime when the last copy operation on the blob completed.
     * @param copyStatusDescription Description of the last copy operation on the blob.
     * @param isServerEncrypted Flag indicating if the blob's content is encrypted on the server.
     * @param isIncrementalCopy Flag indicating if the blob was incrementally copied.
     * @param copyDestinationSnapshot Snapshot identifier of the last incremental copy snapshot for the blob.
     * @param accessTier Access tier of the blob.
     * @param isAccessTierInferred Flag indicating if the access tier of the blob was inferred from properties of the
     * blob.
     * @param archiveStatus Archive status of the blob.
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the blob on the server.
     * @param accessTierChangeTime Datetime when the access tier of the blob last changed.
     * @param metadata Metadata associated with the blob.
     * @param committedBlockCount Number of blocks committed to an append blob, if the blob is a block or page blob
     * pass {@code null}.
     */
    public BlobProperties(final OffsetDateTime creationTime, final OffsetDateTime lastModified, final String eTag,
        final long blobSize, final String contentType, final byte[] contentMd5, final String contentEncoding,
        final String contentDisposition, final String contentLanguage, final String cacheControl,
        final Long blobSequenceNumber, final BlobType blobType, final LeaseStatusType leaseStatus,
        final LeaseStateType leaseState, final LeaseDurationType leaseDuration, final String copyId,
        final CopyStatusType copyStatus, final String copySource, final String copyProgress,
        final OffsetDateTime copyCompletionTime, final String copyStatusDescription, final Boolean isServerEncrypted,
        final Boolean isIncrementalCopy, final String copyDestinationSnapshot, final AccessTier accessTier,
        final Boolean isAccessTierInferred, final ArchiveStatus archiveStatus, final String encryptionKeySha256,
        final OffsetDateTime accessTierChangeTime, final Map<String, String> metadata,
        final Integer committedBlockCount) {
        this.creationTime = creationTime;
        this.lastModified = lastModified;
        this.eTag = eTag;
        this.blobSize = blobSize;
        this.contentType = contentType;
        this.contentMd5 = CoreUtils.clone(contentMd5);
        this.contentEncoding = contentEncoding;
        this.contentDisposition = contentDisposition;
        this.contentLanguage = contentLanguage;
        this.cacheControl = cacheControl;
        this.blobSequenceNumber = blobSequenceNumber;
        this.blobType = blobType;
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
        this.copyDestinationSnapshot = copyDestinationSnapshot;
        this.accessTier = accessTier;
        this.isAccessTierInferred = isAccessTierInferred;
        this.archiveStatus = archiveStatus;
        this.encryptionKeySha256 = encryptionKeySha256;
        this.accessTierChangeTime = accessTierChangeTime;
        this.metadata = metadata;
        this.committedBlockCount = committedBlockCount;
    }

    /**
     * @return the time when the blob was created
     */
    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * @return the time when the blob was last modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return the eTag of the blob
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the size of the blob in bytes
     */
    public long getBlobSize() {
        return blobSize;
    }

    /**
     * @return the content type of the blob
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return the MD5 of the blob's content
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * @return the content encoding of the blob
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * @return the content disposition of the blob
     */
    public String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * @return the content language of the blob
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * @return the cache control of the blob
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * @return the current sequence number of the page blob. This is only returned for page blobs.
     */
    public Long getBlobSequenceNumber() {
        return blobSequenceNumber;
    }

    /**
     * @return the type of the blob
     */
    public BlobType getBlobType() {
        return blobType;
    }

    /**
     * @return the lease status of the blob
     */
    public LeaseStatusType getLeaseStatus() {
        return leaseStatus;
    }

    /**
     * @return the lease state of the blob
     */
    public LeaseStateType getLeaseState() {
        return leaseState;
    }

    /**
     * @return the lease duration if the blob is leased
     */
    public LeaseDurationType getLeaseDuration() {
        return leaseDuration;
    }

    /**
     * @return the identifier of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public String getCopyId() {
        return copyId;
    }

    /**
     * @return the status of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public CopyStatusType getCopyStatus() {
        return copyStatus;
    }

    /**
     * @return the source blob URL from the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public String getCopySource() {
        return copySource;
    }

    /**
     * @return the number of bytes copied and total bytes in the source from the last copy operation (bytes copied/total
     * bytes). If this blob hasn't been the target of a copy operation or has been modified since this won't be set.
     */
    public String getCopyProgress() {
        return copyProgress;
    }

    /**
     * @return the completion time of the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public OffsetDateTime getCopyCompletionTime() {
        return copyCompletionTime;
    }

    /**
     * @return the description of the last copy failure, this is set when the {@link #getCopyStatus() getCopyStatus} is
     * {@link CopyStatusType#FAILED failed} or {@link CopyStatusType#ABORTED aborted}. If this blob hasn't been the
     * target of a copy operation or has been modified since this won't be set.
     */
    public String getCopyStatusDescription() {
        return copyStatusDescription;
    }

    /**
     * @return the status of the blob being encrypted on the server
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * @return the status of the blob being an incremental copy blob
     */
    public Boolean isIncrementalCopy() {
        return isIncrementalCopy;
    }

    /**
     * @return the snapshot time of the last successful incremental copy snapshot for this blob. If this blob isn't an
     * incremental copy blob or incremental copy snapshot or {@link #getCopyStatus() getCopyStatus} isn't {@link
     * CopyStatusType#SUCCESS success} this won't be set.
     */
    public String getCopyDestinationSnapshot() {
        return copyDestinationSnapshot;
    }

    /**
     * @return the tier of the blob. This is only set for Page blobs on a premium storage account or for Block blobs on
     * blob storage or general purpose V2 account.
     */
    public AccessTier getAccessTier() {
        return accessTier;
    }

    /**
     * @return the status of the tier being inferred for the blob. This is only set for Page blobs on a premium storage
     * account or for Block blobs on blob storage or general purpose V2 account.
     */
    public Boolean isAccessTierInferred() {
        return isAccessTierInferred;
    }

    /**
     * @return the archive status of the blob. This is only for blobs on a blob storage and general purpose v2 account.
     */
    public ArchiveStatus getArchiveStatus() {
        return archiveStatus;
    }

    /**
     * @return the key used to encrypt the blob
     */
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * @return the time when the access tier for the blob was last changed
     */
    public OffsetDateTime getAccessTierChangeTime() {
        return accessTierChangeTime;
    }

    /**
     * @return the metadata associated with this blob
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @return the number of committed blocks in the blob. This is only returned for Append blobs.
     */
    public Integer getCommittedBlockCount() {
        return committedBlockCount;
    }
}
