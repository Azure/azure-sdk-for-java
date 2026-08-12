// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the response information returned from the service when getting blob layout.
 */
@Immutable
public final class BlobLayoutInfo {
    private final List<BlobLayoutRange> ranges;
    private final OffsetDateTime lastModified;
    private final OffsetDateTime createdOn;
    private final Map<String, String> metadata;
    private final String objectReplicationDestinationPolicyId;
    private final List<ObjectReplicationPolicy> objectReplicationSourcePolicies;
    private final BlobType blobType;
    private final OffsetDateTime copyCompletionTime;
    private final String copyStatusDescription;
    private final String copyId;
    private final String copyProgress;
    private final String copySource;
    private final CopyStatusType copyStatus;
    private final LeaseDurationType leaseDuration;
    private final LeaseStateType leaseState;
    private final LeaseStatusType leaseStatus;
    private final Long contentLength;
    private final String contentType;
    private final String eTag;
    private final byte[] contentMd5;
    private final String contentEncoding;
    private final String contentDisposition;
    private final String contentLanguage;
    private final String cacheControl;
    private final Long blobSequenceNumber;
    private final String acceptRanges;
    private final Integer blobCommittedBlockCount;
    private final Boolean isServerEncrypted;
    private final String encryptionKeySha256;
    private final String encryptionScope;
    private final String accessTier;
    private final Boolean accessTierInferred;
    private final String smartAccessTier;
    private final String archiveStatus;
    private final OffsetDateTime accessTierChangeTime;
    private final String versionId;
    private final Boolean isCurrentVersion;
    private final Long tagCount;
    private final OffsetDateTime expiresOn;
    private final Boolean isSealed;
    private final String rehydratePriority;
    private final OffsetDateTime lastAccessedTime;
    private final BlobImmutabilityPolicy immutabilityPolicy;
    private final Boolean hasLegalHold;
    private final Long blobContentLength;
    private final String blobContentType;
    private final String blobContentEncoding;
    private final byte[] blobContentMd5;
    private final OffsetDateTime blobCreatedOn;

    /**
     * Constructs a {@link BlobLayoutInfo}.
     *
     * @param ranges The ranges in the blob layout.
     * @param lastModified Datetime when the blob was last modified.
     * @param createdOn Creation time of the blob.
     * @param metadata Metadata associated with the blob.
     * @param objectReplicationDestinationPolicyId The policy id on the destination blob.
     * @param objectReplicationSourcePolicies The object replication policy and rules on the source blob.
     * @param blobType Type of the blob.
     * @param copyCompletionTime Datetime when the last copy operation on the blob completed.
     * @param copyStatusDescription Description of the last copy operation on the blob.
     * @param copyId Identifier of the last copy operation performed on the blob.
     * @param copyProgress Progress of the last copy operation performed on the blob.
     * @param copySource Source of the last copy operation performed on the blob.
     * @param copyStatus Status of the last copy operation performed on the blob.
     * @param leaseDuration Type of lease on the blob.
     * @param leaseState State of the lease on the blob.
     * @param leaseStatus Status of the lease on the blob.
     * @param contentLength Size of the blob.
     * @param contentType Content type specified for the blob.
     * @param eTag ETag of the blob.
     * @param contentMd5 Content MD5 specified for the blob.
     * @param contentEncoding Content encoding specified for the blob.
     * @param contentDisposition Content disposition specified for the blob.
     * @param contentLanguage Content language specified for the blob.
     * @param cacheControl Cache control specified for the blob.
     * @param blobSequenceNumber The current sequence number for a page blob.
     * @param acceptRanges The range units accepted by the service.
     * @param blobCommittedBlockCount Number of blocks committed to an append blob.
     * @param isServerEncrypted Flag indicating if the blob's content is encrypted on the server.
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the blob.
     * @param encryptionScope The name of the encryption scope under which the blob is encrypted.
     * @param accessTier Access tier of the blob.
     * @param accessTierInferred Flag indicating if the access tier of the blob was inferred.
     * @param smartAccessTier Smart access tier of the blob.
     * @param archiveStatus Archive status of the blob.
     * @param accessTierChangeTime Datetime when the access tier of the blob last changed.
     * @param versionId The version identifier of the blob.
     * @param isCurrentVersion Flag indicating if version identifier points to current version of the blob.
     * @param tagCount Number of tags associated with the blob.
     * @param expiresOn The time when the blob is going to expire.
     * @param isSealed Whether the blob is sealed.
     * @param rehydratePriority The rehydrate priority.
     * @param lastAccessedTime The date and time the blob was read or written to.
     * @param immutabilityPolicy The immutability policy of the blob.
     * @param hasLegalHold Whether the blob has a legal hold.
     * @param blobContentLength The content length of the blob (distinct from the response body length).
     * @param blobContentType The content type specified for the blob.
     * @param blobContentEncoding The content encoding specified for the blob.
     * @param blobContentMd5 The content MD5 of the blob.
     * @param blobCreatedOn The creation time of the blob.
     */
    public BlobLayoutInfo(List<BlobLayoutRange> ranges, OffsetDateTime lastModified, OffsetDateTime createdOn,
        Map<String, String> metadata, String objectReplicationDestinationPolicyId,
        List<ObjectReplicationPolicy> objectReplicationSourcePolicies, BlobType blobType,
        OffsetDateTime copyCompletionTime, String copyStatusDescription, String copyId, String copyProgress,
        String copySource, CopyStatusType copyStatus, LeaseDurationType leaseDuration, LeaseStateType leaseState,
        LeaseStatusType leaseStatus, Long contentLength, String contentType, String eTag, byte[] contentMd5,
        String contentEncoding, String contentDisposition, String contentLanguage, String cacheControl,
        Long blobSequenceNumber, String acceptRanges, Integer blobCommittedBlockCount, Boolean isServerEncrypted,
        String encryptionKeySha256, String encryptionScope, String accessTier, Boolean accessTierInferred,
        String smartAccessTier, String archiveStatus, OffsetDateTime accessTierChangeTime, String versionId,
        Boolean isCurrentVersion, Long tagCount, OffsetDateTime expiresOn, Boolean isSealed, String rehydratePriority,
        OffsetDateTime lastAccessedTime, BlobImmutabilityPolicy immutabilityPolicy, Boolean hasLegalHold,
        Long blobContentLength, String blobContentType, String blobContentEncoding, byte[] blobContentMd5,
        OffsetDateTime blobCreatedOn) {
        this.ranges = ranges == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(ranges));
        this.lastModified = lastModified;
        this.createdOn = createdOn;
        this.metadata
            = metadata == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(metadata));
        this.objectReplicationDestinationPolicyId = objectReplicationDestinationPolicyId;
        this.objectReplicationSourcePolicies = objectReplicationSourcePolicies == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(objectReplicationSourcePolicies));
        this.blobType = blobType;
        this.copyCompletionTime = copyCompletionTime;
        this.copyStatusDescription = copyStatusDescription;
        this.copyId = copyId;
        this.copyProgress = copyProgress;
        this.copySource = copySource;
        this.copyStatus = copyStatus;
        this.leaseDuration = leaseDuration;
        this.leaseState = leaseState;
        this.leaseStatus = leaseStatus;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.eTag = eTag;
        this.contentMd5 = CoreUtils.clone(contentMd5);
        this.contentEncoding = contentEncoding;
        this.contentDisposition = contentDisposition;
        this.contentLanguage = contentLanguage;
        this.cacheControl = cacheControl;
        this.blobSequenceNumber = blobSequenceNumber;
        this.acceptRanges = acceptRanges;
        this.blobCommittedBlockCount = blobCommittedBlockCount;
        this.isServerEncrypted = isServerEncrypted;
        this.encryptionKeySha256 = encryptionKeySha256;
        this.encryptionScope = encryptionScope;
        this.accessTier = accessTier;
        this.accessTierInferred = accessTierInferred;
        this.smartAccessTier = smartAccessTier;
        this.archiveStatus = archiveStatus;
        this.accessTierChangeTime = accessTierChangeTime;
        this.versionId = versionId;
        this.isCurrentVersion = isCurrentVersion;
        this.tagCount = tagCount;
        this.expiresOn = expiresOn;
        this.isSealed = isSealed;
        this.rehydratePriority = rehydratePriority;
        this.lastAccessedTime = lastAccessedTime;
        this.immutabilityPolicy = immutabilityPolicy;
        this.hasLegalHold = hasLegalHold;
        this.blobContentLength = blobContentLength;
        this.blobContentType = blobContentType;
        this.blobContentEncoding = blobContentEncoding;
        this.blobContentMd5 = CoreUtils.clone(blobContentMd5);
        this.blobCreatedOn = blobCreatedOn;
    }

    /**
     * Gets the ranges property.
     *
     * @return The ranges property.
     */
    public List<BlobLayoutRange> getRanges() {
        return ranges;
    }

    /**
     * Gets the lastModified property.
     *
     * @return The lastModified property.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Gets the createdOn property.
     *
     * @return The createdOn property.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    /**
     * Gets the metadata property.
     *
     * @return The metadata property.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Gets the objectReplicationDestinationPolicyId property.
     *
     * @return The objectReplicationDestinationPolicyId property.
     */
    public String getObjectReplicationDestinationPolicyId() {
        return objectReplicationDestinationPolicyId;
    }

    /**
     * Gets the object replication policy and rules on the source blob.
     *
     * @return The object replication policy and rules on the source blob.
     */
    public List<ObjectReplicationPolicy> getObjectReplicationSourcePolicies() {
        return objectReplicationSourcePolicies;
    }

    /**
     * Gets the blobType property.
     *
     * @return The blobType property.
     */
    public BlobType getBlobType() {
        return blobType;
    }

    /**
     * Gets the copyCompletionTime property.
     *
     * @return The copyCompletionTime property.
     */
    public OffsetDateTime getCopyCompletionTime() {
        return copyCompletionTime;
    }

    /**
     * Gets the copyStatusDescription property.
     *
     * @return The copyStatusDescription property.
     */
    public String getCopyStatusDescription() {
        return copyStatusDescription;
    }

    /**
     * Gets the copyId property.
     *
     * @return The copyId property.
     */
    public String getCopyId() {
        return copyId;
    }

    /**
     * Gets the copyProgress property.
     *
     * @return The copyProgress property.
     */
    public String getCopyProgress() {
        return copyProgress;
    }

    /**
     * Gets the copySource property.
     *
     * @return The copySource property.
     */
    public String getCopySource() {
        return copySource;
    }

    /**
     * Gets the copyStatus property.
     *
     * @return The copyStatus property.
     */
    public CopyStatusType getCopyStatus() {
        return copyStatus;
    }

    /**
     * Gets the leaseDuration property.
     *
     * @return The leaseDuration property.
     */
    public LeaseDurationType getLeaseDuration() {
        return leaseDuration;
    }

    /**
     * Gets the leaseState property.
     *
     * @return The leaseState property.
     */
    public LeaseStateType getLeaseState() {
        return leaseState;
    }

    /**
     * Gets the leaseStatus property.
     *
     * @return The leaseStatus property.
     */
    public LeaseStatusType getLeaseStatus() {
        return leaseStatus;
    }

    /**
     * Gets the contentLength property.
     *
     * @return The contentLength property.
     */
    public Long getContentLength() {
        return contentLength;
    }

    /**
     * Gets the contentType property.
     *
     * @return The contentType property.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the eTag property.
     *
     * @return The eTag property.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the contentMd5 property.
     *
     * @return The contentMd5 property.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * Gets the contentEncoding property.
     *
     * @return The contentEncoding property.
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Gets the contentDisposition property.
     *
     * @return The contentDisposition property.
     */
    public String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * Gets the contentLanguage property.
     *
     * @return The contentLanguage property.
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * Gets the cacheControl property.
     *
     * @return The cacheControl property.
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * Gets the blobSequenceNumber property.
     *
     * @return The blobSequenceNumber property.
     */
    public Long getBlobSequenceNumber() {
        return blobSequenceNumber;
    }

    /**
     * Gets the acceptRanges property.
     *
     * @return The acceptRanges property.
     */
    public String getAcceptRanges() {
        return acceptRanges;
    }

    /**
     * Gets the blobCommittedBlockCount property.
     *
     * @return The blobCommittedBlockCount property.
     */
    public Integer getBlobCommittedBlockCount() {
        return blobCommittedBlockCount;
    }

    /**
     * Gets the flag indicating if the blob's content is encrypted on the server.
     *
     * @return The flag indicating if the blob's content is encrypted on the server.
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * Gets the encryptionKeySha256 property.
     *
     * @return The encryptionKeySha256 property.
     */
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * Gets the encryptionScope property.
     *
     * @return The encryptionScope property.
     */
    public String getEncryptionScope() {
        return encryptionScope;
    }

    /**
     * Gets the accessTier property.
     *
     * @return The accessTier property.
     */
    public String getAccessTier() {
        return accessTier;
    }

    /**
     * Gets the flag indicating if the access tier of the blob was inferred.
     *
     * @return The flag indicating if the access tier of the blob was inferred.
     */
    public Boolean isAccessTierInferred() {
        return accessTierInferred;
    }

    /**
     * Gets the smartAccessTier property.
     *
     * @return The smartAccessTier property.
     */
    public String getSmartAccessTier() {
        return smartAccessTier;
    }

    /**
     * Gets the archiveStatus property.
     *
     * @return The archiveStatus property.
     */
    public String getArchiveStatus() {
        return archiveStatus;
    }

    /**
     * Gets the accessTierChangeTime property.
     *
     * @return The accessTierChangeTime property.
     */
    public OffsetDateTime getAccessTierChangeTime() {
        return accessTierChangeTime;
    }

    /**
     * Gets the versionId property.
     *
     * @return The versionId property.
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * Gets the flag indicating whether version identifier points to current version of the blob.
     *
     * @return The flag indicating whether version identifier points to current version of the blob.
     */
    public Boolean isCurrentVersion() {
        return isCurrentVersion;
    }

    /**
     * Gets the tagCount property.
     *
     * @return The tagCount property.
     */
    public Long getTagCount() {
        return tagCount;
    }

    /**
     * Gets the expiresOn property.
     *
     * @return The expiresOn property.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    /**
     * Gets the flag indicating whether this blob has been sealed.
     *
     * @return The flag indicating whether this blob has been sealed.
     */
    public Boolean isSealed() {
        return isSealed;
    }

    /**
     * Gets the rehydratePriority property.
     *
     * @return The rehydratePriority property.
     */
    public String getRehydratePriority() {
        return rehydratePriority;
    }

    /**
     * Gets the lastAccessedTime property.
     *
     * @return The lastAccessedTime property.
     */
    public OffsetDateTime getLastAccessedTime() {
        return lastAccessedTime;
    }

    /**
     * Gets the immutabilityPolicy property.
     *
     * @return The immutabilityPolicy property.
     */
    public BlobImmutabilityPolicy getImmutabilityPolicy() {
        return immutabilityPolicy;
    }

    /**
     * Gets the legal hold status of the blob.
     *
     * @return whether the blob has a legal hold.
     */
    public Boolean hasLegalHold() {
        return hasLegalHold;
    }

    /**
     * Gets the content length of the blob. Distinct from {@link #getContentLength()}, which reflects the length of
     * the layout response body rather than the blob's actual content length.
     *
     * @return The content length of the blob.
     */
    public Long getBlobContentLength() {
        return blobContentLength;
    }

    /**
     * Gets the content type specified for the blob.
     *
     * @return The content type specified for the blob.
     */
    public String getBlobContentType() {
        return blobContentType;
    }

    /**
     * Gets the content encoding specified for the blob.
     *
     * @return The content encoding specified for the blob.
     */
    public String getBlobContentEncoding() {
        return blobContentEncoding;
    }

    /**
     * Gets the content MD5 of the blob.
     *
     * @return The content MD5 of the blob.
     */
    public byte[] getBlobContentMd5() {
        return CoreUtils.clone(blobContentMd5);
    }

    /**
     * Gets the creation time of the blob.
     *
     * @return The creation time of the blob.
     */
    public OffsetDateTime getBlobCreatedOn() {
        return blobCreatedOn;
    }
}
