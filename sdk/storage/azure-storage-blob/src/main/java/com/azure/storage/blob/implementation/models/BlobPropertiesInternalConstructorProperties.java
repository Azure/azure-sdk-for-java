// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.models;

import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.ArchiveStatus;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.LeaseDurationType;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;
import com.azure.storage.blob.models.ObjectReplicationPolicy;
import com.azure.storage.blob.models.RehydratePriority;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link BlobPropertiesInternal} that represents the last constructor overload of
 * {@link BlobProperties}.
 */
public final class BlobPropertiesInternalConstructorProperties implements BlobPropertiesInternal {
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
    private final String encryptionScope;
    private final OffsetDateTime accessTierChangeTime;
    private final Map<String, String> metadata;
    private final Integer committedBlockCount;
    private final Long tagCount;
    private final String versionId;
    private final Boolean isCurrentVersion;
    private final List<ObjectReplicationPolicy> objectReplicationSourcePolicies;
    private final String objectReplicationDestinationPolicyId;
    private final RehydratePriority rehydratePriority;
    private final Boolean isSealed;
    private final OffsetDateTime lastAccessedTime;
    private final OffsetDateTime expiresOn;
    private final BlobImmutabilityPolicy immutabilityPolicy;
    private final Boolean hasLegalHold;

    /**
     * Constructs a {@link BlobPropertiesInternalConstructorProperties}.
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
     * @param encryptionScope The name of the encryption scope under which the blob is encrypted.
     * @param accessTierChangeTime Datetime when the access tier of the blob last changed.
     * @param metadata Metadata associated with the blob.
     * @param committedBlockCount Number of blocks committed to an append blob, if the blob is a block or page blob
     * pass {@code null}.
     * @param versionId The version identifier of the blob.
     * @param isCurrentVersion Flag indicating if version identifier points to current version of the blob.
     * @param tagCount Number of tags associated with the blob.
     * @param objectReplicationSourcePolicies The already parsed object replication policies.
     * @param objectReplicationDestinationPolicyId The policy id on the destination blob.
     * @param rehydratePriority The rehydrate priority
     * @param isSealed Whether the blob is sealed.
     * @param lastAccessedTime The date and time the blob was read or written to.
     * @param expiresOn The time when the blob is going to expire.
     * @param immutabilityPolicy the immutability policy of the blob.
     * @param hasLegalHold whether the blob has a legal hold.
     */
    public BlobPropertiesInternalConstructorProperties(final OffsetDateTime creationTime,
        final OffsetDateTime lastModified, final String eTag, final long blobSize, final String contentType,
        final byte[] contentMd5, final String contentEncoding, final String contentDisposition,
        final String contentLanguage, final String cacheControl, final Long blobSequenceNumber, final BlobType blobType,
        final LeaseStatusType leaseStatus, final LeaseStateType leaseState, final LeaseDurationType leaseDuration,
        final String copyId, final CopyStatusType copyStatus, final String copySource, final String copyProgress,
        final OffsetDateTime copyCompletionTime, final String copyStatusDescription, final Boolean isServerEncrypted,
        final Boolean isIncrementalCopy, final String copyDestinationSnapshot, final AccessTier accessTier,
        final Boolean isAccessTierInferred, final ArchiveStatus archiveStatus, final String encryptionKeySha256,
        final String encryptionScope, final OffsetDateTime accessTierChangeTime, final Map<String, String> metadata,
        final Integer committedBlockCount, final Long tagCount, final String versionId, final Boolean isCurrentVersion,
        final List<ObjectReplicationPolicy> objectReplicationSourcePolicies,
        final String objectReplicationDestinationPolicyId, final RehydratePriority rehydratePriority,
        final Boolean isSealed, final OffsetDateTime lastAccessedTime, final OffsetDateTime expiresOn,
        final BlobImmutabilityPolicy immutabilityPolicy, final Boolean hasLegalHold) {
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
        this.encryptionScope = encryptionScope;
        this.accessTierChangeTime = accessTierChangeTime;
        this.metadata = metadata;
        this.committedBlockCount = committedBlockCount;
        this.tagCount = tagCount;
        this.versionId = versionId;
        this.isCurrentVersion = isCurrentVersion;
        this.objectReplicationSourcePolicies = objectReplicationSourcePolicies;
        this.objectReplicationDestinationPolicyId = objectReplicationDestinationPolicyId;
        this.rehydratePriority = rehydratePriority;
        this.isSealed = isSealed;
        this.lastAccessedTime = lastAccessedTime;
        this.expiresOn = expiresOn;
        this.immutabilityPolicy = immutabilityPolicy;
        this.hasLegalHold = hasLegalHold;
    }

    @Override
    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

    @Override
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    @Override
    public String getETag() {
        return eTag;
    }

    @Override
    public long getBlobSize() {
        return blobSize;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    @Override
    public String getContentEncoding() {
        return contentEncoding;
    }

    @Override
    public String getContentDisposition() {
        return contentDisposition;
    }

    @Override
    public String getContentLanguage() {
        return contentLanguage;
    }

    @Override
    public String getCacheControl() {
        return cacheControl;
    }

    @Override
    public Long getBlobSequenceNumber() {
        return blobSequenceNumber;
    }

    @Override
    public BlobType getBlobType() {
        return blobType;
    }

    @Override
    public LeaseStatusType getLeaseStatus() {
        return leaseStatus;
    }

    @Override
    public LeaseStateType getLeaseState() {
        return leaseState;
    }

    @Override
    public LeaseDurationType getLeaseDuration() {
        return leaseDuration;
    }

    @Override
    public String getCopyId() {
        return copyId;
    }

    @Override
    public CopyStatusType getCopyStatus() {
        return copyStatus;
    }

    @Override
    public String getCopySource() {
        return copySource;
    }

    @Override
    public String getCopyProgress() {
        return copyProgress;
    }

    @Override
    public OffsetDateTime getCopyCompletionTime() {
        return copyCompletionTime;
    }

    @Override
    public String getCopyStatusDescription() {
        return copyStatusDescription;
    }

    @Override
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    @Override
    public Boolean isIncrementalCopy() {
        return isIncrementalCopy;
    }

    @Override
    public String getCopyDestinationSnapshot() {
        return copyDestinationSnapshot;
    }

    @Override
    public AccessTier getAccessTier() {
        return accessTier;
    }

    @Override
    public Boolean isAccessTierInferred() {
        return isAccessTierInferred;
    }

    @Override
    public ArchiveStatus getArchiveStatus() {
        return archiveStatus;
    }

    @Override
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }

    @Override
    public String getEncryptionScope() {
        return encryptionScope;
    }

    @Override
    public OffsetDateTime getAccessTierChangeTime() {
        return accessTierChangeTime;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public Integer getCommittedBlockCount() {
        return committedBlockCount;
    }

    @Override
    public Long getTagCount() {
        return tagCount;
    }

    @Override
    public String getVersionId() {
        return versionId;
    }

    @Override
    public Boolean isCurrentVersion() {
        return isCurrentVersion;
    }

    @Override
    public List<ObjectReplicationPolicy> getObjectReplicationSourcePolicies() {
        return Collections.unmodifiableList(this.objectReplicationSourcePolicies);
    }

    @Override
    public String getObjectReplicationDestinationPolicyId() {
        return this.objectReplicationDestinationPolicyId;
    }

    @Override
    public RehydratePriority getRehydratePriority() {
        return this.rehydratePriority;
    }

    @Override
    public Boolean isSealed() {
        return isSealed;
    }

    @Override
    public OffsetDateTime getLastAccessedTime() {
        return lastAccessedTime;
    }

    @Override
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    @Override
    public BlobImmutabilityPolicy getImmutabilityPolicy() {
        return immutabilityPolicy;
    }

    @Override
    public Boolean hasLegalHold() {
        return hasLegalHold;
    }
}
