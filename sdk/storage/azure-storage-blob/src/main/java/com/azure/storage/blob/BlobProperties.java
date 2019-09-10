// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.ArchiveStatus;
import com.azure.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.LeaseDurationType;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;
import com.azure.storage.blob.models.Metadata;

import java.time.OffsetDateTime;

public final class BlobProperties {
    private final OffsetDateTime creationTime;
    private final OffsetDateTime lastModified;
    private final String eTag;
    private final long blobSize;
    private final String contentType;
    private final byte[] contentMD5;
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
    private final Metadata metadata;
    private final Integer committedBlockCount;

    BlobProperties(BlobGetPropertiesHeaders generatedHeaders) {
        this.creationTime = generatedHeaders.creationTime();
        this.lastModified = generatedHeaders.lastModified();
        this.eTag = generatedHeaders.eTag();
        this.blobSize = generatedHeaders.contentLength() == null ? 0 : generatedHeaders.contentLength();
        this.contentType = generatedHeaders.contentType();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.contentEncoding = generatedHeaders.contentEncoding();
        this.contentDisposition = generatedHeaders.contentDisposition();
        this.contentLanguage = generatedHeaders.contentLanguage();
        this.cacheControl = generatedHeaders.cacheControl();
        this.blobSequenceNumber = generatedHeaders.blobSequenceNumber();
        this.blobType = generatedHeaders.blobType();
        this.leaseStatus = generatedHeaders.leaseStatus();
        this.leaseState = generatedHeaders.leaseState();
        this.leaseDuration = generatedHeaders.leaseDuration();
        this.copyId = generatedHeaders.copyId();
        this.copyStatus = generatedHeaders.copyStatus();
        this.copySource = generatedHeaders.copySource();
        this.copyProgress = generatedHeaders.copyProgress();
        this.copyCompletionTime = generatedHeaders.copyCompletionTime();
        this.copyStatusDescription = generatedHeaders.copyStatusDescription();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.isIncrementalCopy = generatedHeaders.isIncrementalCopy();
        this.copyDestinationSnapshot = generatedHeaders.destinationSnapshot();
        this.accessTier = AccessTier.fromString(generatedHeaders.accessTier());
        this.isAccessTierInferred = generatedHeaders.accessTierInferred();
        this.archiveStatus = ArchiveStatus.fromString(generatedHeaders.archiveStatus());
        this.encryptionKeySha256 = generatedHeaders.encryptionKeySha256();
        this.accessTierChangeTime = generatedHeaders.accessTierChangeTime();
        this.metadata = new Metadata(generatedHeaders.metadata());
        this.committedBlockCount = generatedHeaders.blobCommittedBlockCount();
    }

    /**
     * @return the time when the blob was created
     */
    public OffsetDateTime creationTime() {
        return creationTime;
    }

    /**
     * @return the time when the blob was last modified
     */
    public OffsetDateTime lastModified() {
        return lastModified;
    }

    /**
     * @return the eTag of the blob
     */
    public String eTag() {
        return eTag;
    }

    /**
     * @return the size of the blob in bytes
     */
    public long blobSize() {
        return blobSize;
    }

    /**
     * @return the content type of the blob
     */
    public String contentType() {
        return contentType;
    }

    /**
     * @return the MD5 of the blob's content
     */
    public byte[] contentMD5() {
        return ImplUtils.clone(contentMD5);
    }

    /**
     * @return the content encoding of the blob
     */
    public String contentEncoding() {
        return contentEncoding;
    }

    /**
     * @return the content disposition of the blob
     */
    public String contentDisposition() {
        return contentDisposition;
    }

    /**
     * @return the content language of the blob
     */
    public String contentLanguage() {
        return contentLanguage;
    }

    /**
     * @return the cache control of the blob
     */
    public String cacheControl() {
        return cacheControl;
    }

    /**
     * @return the current sequence number of the page blob. This is only returned for page blobs.
     */
    public Long blobSequenceNumber() {
        return blobSequenceNumber;
    }

    /**
     * @return the type of the blob
     */
    public BlobType blobType() {
        return blobType;
    }

    /**
     * @return the lease status of the blob
     */
    public LeaseStatusType leaseStatus() {
        return leaseStatus;
    }

    /**
     * @return the lease state of the blob
     */
    public LeaseStateType leaseState() {
        return leaseState;
    }

    /**
     * @return the lease duration if the blob is leased
     */
    public LeaseDurationType leaseDuration() {
        return leaseDuration;
    }

    /**
     * @return the identifier of the last copy operation. If this blob hasn't been the target of a copy operation or
     * has been modified since this won't be set.
     */
    public String copyId() {
        return copyId;
    }

    /**
     * @return the status of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public CopyStatusType copyStatus() {
        return copyStatus;
    }

    /**
     * @return the source blob URL from the last copy operation. If this blob hasn't been the target of a copy
     * operation or has been modified since this won't be set.
     */
    public String copySource() {
        return copySource;
    }

    /**
     * @return the number of bytes copied and total bytes in the source from the last copy operation
     * (bytes copied/total bytes). If this blob hasn't been the target of a copy operation or has been modified since
     * this won't be set.
     */
    public String copyProgress() {
        return copyProgress;
    }

    /**
     * @return the completion time of the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public OffsetDateTime copyCompletionTime() {
        return copyCompletionTime;
    }

    /**
     * @return the description of the last copy failure, this is set when the {@link #copyStatus() copyStatus} is
     * {@link CopyStatusType#FAILED failed} or {@link CopyStatusType#ABORTED aborted}. If this blob hasn't been the
     * target of a copy operation or has been modified since this won't be set.
     */
    public String copyStatusDescription() {
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
     * incremental copy blob or incremental copy snapshot or {@link #copyStatus() copyStatus} isn't
     * {@link CopyStatusType#SUCCESS success} this won't be set.
     */
    public String copyDestinationSnapshot() {
        return copyDestinationSnapshot;
    }

    /**
     * @return the tier of the blob. This is only set for Page blobs on a premium storage account or for Block blobs on
     * blob storage or general purpose V2 account.
     */
    public AccessTier accessTier() {
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
    public ArchiveStatus archiveStatus() {
        return archiveStatus;
    }

    /**
     * @return the key used to encrypt the blob
     */
    public String encryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * @return the time when the access tier for the blob was last changed
     */
    public OffsetDateTime accessTierChangeTime() {
        return accessTierChangeTime;
    }

    /**
     * @return the metadata associated to this blob
     */
    public Metadata metadata() {
        return metadata;
    }

    /**
     * @return the number of committed blocks in the blob. This is only returned for Append blobs.
     */
    public Integer committedBlockCount() {
        return committedBlockCount;
    }
}
