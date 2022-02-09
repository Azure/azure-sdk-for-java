// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.implementation.accesshelpers.BlobPropertiesConstructorProxy;
import com.azure.storage.blob.implementation.models.BlobPropertiesInternal;
import com.azure.storage.blob.implementation.models.BlobPropertiesInternalConstructorProperties;
import com.azure.storage.blob.implementation.util.ModelHelper;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class contains the response information returned from the service when getting blob properties.
 */
@Immutable
public final class BlobProperties {
    private final BlobPropertiesInternal internalProperties;

    static {
        BlobPropertiesConstructorProxy.setAccessor(
            new BlobPropertiesConstructorProxy.BlobPropertiesConstructorAccessor() {
                @Override
                public BlobProperties create(BlobPropertiesInternal internalProperties) {
                    return new BlobProperties(internalProperties);
                }
            });
    }

    private BlobProperties(BlobPropertiesInternal internalProperties) {
        this.internalProperties = internalProperties;
    }

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
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the blob on the
     * server.
     * @param accessTierChangeTime Datetime when the access tier of the blob last changed.
     * @param metadata Metadata associated with the blob.
     * @param committedBlockCount Number of blocks committed to an append blob, if the blob is a block or page blob pass
     * {@code null}.
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
        this(creationTime, lastModified, eTag, blobSize, contentType, contentMd5, contentEncoding, contentDisposition,
            contentLanguage, cacheControl, blobSequenceNumber, blobType, leaseStatus, leaseState, leaseDuration,
            copyId, copyStatus, copySource, copyProgress, copyCompletionTime, copyStatusDescription, isServerEncrypted,
            isIncrementalCopy, copyDestinationSnapshot, accessTier, isAccessTierInferred, archiveStatus,
            encryptionKeySha256, null, accessTierChangeTime, metadata, committedBlockCount, null, null, null, null,
            null);
    }

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
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the blob on the
     * server.
     * @param encryptionScope The name of the encryption scope under which the blob is encrypted.
     * @param accessTierChangeTime Datetime when the access tier of the blob last changed.
     * @param metadata Metadata associated with the blob.
     * @param committedBlockCount Number of blocks committed to an append blob, if the blob is a block or page blob pass
     * {@code null}.
     * @param versionId The version identifier of the blob.
     * @param isCurrentVersion Flag indicating if version identifier points to current version of the blob.
     * @param tagCount Number of tags associated with the blob.
     * @param objectReplicationStatus The object replication status map to parse.
     * @param rehydratePriority The rehydrate priority
     * @param isSealed Whether the blob is sealed.
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
        final String encryptionScope, final OffsetDateTime accessTierChangeTime, final Map<String, String> metadata,
        final Integer committedBlockCount, final String versionId, final Boolean isCurrentVersion,
        final Long tagCount, Map<String, String> objectReplicationStatus, final String rehydratePriority,
        final Boolean isSealed) {
        this(creationTime, lastModified, eTag, blobSize, contentType, contentMd5, contentEncoding, contentDisposition,
            contentLanguage, cacheControl, blobSequenceNumber, blobType, leaseStatus, leaseState, leaseDuration,
            copyId, copyStatus, copySource, copyProgress, copyCompletionTime, copyStatusDescription, isServerEncrypted,
            isIncrementalCopy, copyDestinationSnapshot, accessTier, isAccessTierInferred, archiveStatus,
            encryptionKeySha256, encryptionScope, accessTierChangeTime, metadata, committedBlockCount, tagCount,
            versionId, isCurrentVersion, ModelHelper.getObjectReplicationSourcePolicies(objectReplicationStatus),
            ModelHelper.getObjectReplicationDestinationPolicyId(objectReplicationStatus),
            RehydratePriority.fromString(rehydratePriority), isSealed, null, null);
    }

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
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the blob on the
     * server.
     * @param encryptionScope The name of the encryption scope under which the blob is encrypted.
     * @param accessTierChangeTime Datetime when the access tier of the blob last changed.
     * @param metadata Metadata associated with the blob.
     * @param committedBlockCount Number of blocks committed to an append blob, if the blob is a block or page blob pass
     * {@code null}.
     * @param versionId The version identifier of the blob.
     * @param isCurrentVersion Flag indicating if version identifier points to current version of the blob.
     * @param tagCount Number of tags associated with the blob.
     * @param objectReplicationSourcePolicies The already parsed object replication policies.
     * @param objectReplicationDestinationPolicyId The policy id on the destination blob.
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
        String encryptionScope, final OffsetDateTime accessTierChangeTime, final Map<String, String> metadata,
        final Integer committedBlockCount, final Long tagCount, final String versionId,
        final Boolean isCurrentVersion, List<ObjectReplicationPolicy> objectReplicationSourcePolicies,
        String objectReplicationDestinationPolicyId) {
        this(creationTime, lastModified, eTag, blobSize, contentType, contentMd5, contentEncoding, contentDisposition,
            contentLanguage, cacheControl, blobSequenceNumber, blobType, leaseStatus, leaseState, leaseDuration,
            copyId, copyStatus, copySource, copyProgress, copyCompletionTime, copyStatusDescription, isServerEncrypted,
            isIncrementalCopy, copyDestinationSnapshot, accessTier, isAccessTierInferred, archiveStatus,
            encryptionKeySha256, encryptionScope, accessTierChangeTime, metadata, committedBlockCount, tagCount,
            versionId, isCurrentVersion, objectReplicationSourcePolicies, objectReplicationDestinationPolicyId,
            null, null, null, null);
    }

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
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the blob on the
     * server.
     * @param encryptionScope The name of the encryption scope under which the blob is encrypted.
     * @param accessTierChangeTime Datetime when the access tier of the blob last changed.
     * @param metadata Metadata associated with the blob.
     * @param committedBlockCount Number of blocks committed to an append blob, if the blob is a block or page blob pass
     * {@code null}.
     * @param versionId The version identifier of the blob.
     * @param isCurrentVersion Flag indicating if version identifier points to current version of the blob.
     * @param tagCount Number of tags associated with the blob.
     * @param objectReplicationSourcePolicies The already parsed object replication policies.
     * @param objectReplicationDestinationPolicyId The policy id on the destination blob.
     * @param rehydratePriority The rehydrate priority
     * @param isSealed Whether the blob is sealed.
     * @param lastAccessedTime The date and time the blob was read or written to.
     * @param expiresOn The time when the blob is going to expire.
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
        final String encryptionScope, final OffsetDateTime accessTierChangeTime, final Map<String, String> metadata,
        final Integer committedBlockCount, final Long tagCount, final String versionId, final Boolean isCurrentVersion,
        final List<ObjectReplicationPolicy> objectReplicationSourcePolicies,
        final String objectReplicationDestinationPolicyId, final RehydratePriority rehydratePriority,
        final Boolean isSealed, final OffsetDateTime lastAccessedTime, final OffsetDateTime expiresOn) {
        this(creationTime, lastModified, eTag, blobSize, contentType, contentMd5, contentEncoding, contentDisposition,
            contentLanguage, cacheControl, blobSequenceNumber, blobType, leaseStatus, leaseState, leaseDuration,
            copyId, copyStatus, copySource, copyProgress, copyCompletionTime, copyStatusDescription, isServerEncrypted,
            isIncrementalCopy, copyDestinationSnapshot, accessTier, isAccessTierInferred, archiveStatus,
            encryptionKeySha256, encryptionScope, accessTierChangeTime, metadata, committedBlockCount, tagCount,
            versionId, isCurrentVersion, objectReplicationSourcePolicies, objectReplicationDestinationPolicyId,
            rehydratePriority, isSealed, lastAccessedTime, expiresOn, null, false);
    }

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
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the blob on the
     * server.
     * @param encryptionScope The name of the encryption scope under which the blob is encrypted.
     * @param accessTierChangeTime Datetime when the access tier of the blob last changed.
     * @param metadata Metadata associated with the blob.
     * @param committedBlockCount Number of blocks committed to an append blob, if the blob is a block or page blob pass
     * {@code null}.
     * @param versionId The version identifier of the blob.
     * @param isCurrentVersion Flag indicating if version identifier points to current version of the blob.
     * @param tagCount Number of tags associated with the blob.
     * @param objectReplicationSourcePolicies The already parsed object replication policies.
     * @param objectReplicationDestinationPolicyId The policy id on the destination blob.
     * @param rehydratePriority The rehydrate priority
     * @param isSealed Whether the blob is sealed.
     * @param lastAccessedTime The date and time the blob was read or written to.
     * @param expiresOn The time when the blob is going to expire.
     * @param immutabilityPolicy The immutability policy of the blob.
     * @param hasLegalHold Whether the blob has a legal hold.
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
        final String encryptionScope, final OffsetDateTime accessTierChangeTime, final Map<String, String> metadata,
        final Integer committedBlockCount, final Long tagCount, final String versionId, final Boolean isCurrentVersion,
        final List<ObjectReplicationPolicy> objectReplicationSourcePolicies,
        final String objectReplicationDestinationPolicyId, final RehydratePriority rehydratePriority,
        final Boolean isSealed, final OffsetDateTime lastAccessedTime, final OffsetDateTime expiresOn,
        BlobImmutabilityPolicy immutabilityPolicy, Boolean hasLegalHold) {
        this(new BlobPropertiesInternalConstructorProperties(creationTime, lastModified, eTag, blobSize, contentType,
            contentMd5, contentEncoding, contentDisposition, contentLanguage, cacheControl, blobSequenceNumber,
            blobType, leaseStatus, leaseState, leaseDuration, copyId, copyStatus, copySource, copyProgress,
            copyCompletionTime, copyStatusDescription, isServerEncrypted, isIncrementalCopy, copyDestinationSnapshot,
            accessTier, isAccessTierInferred, archiveStatus, encryptionKeySha256, encryptionScope, accessTierChangeTime,
            metadata, committedBlockCount, tagCount, versionId, isCurrentVersion, objectReplicationSourcePolicies,
            objectReplicationDestinationPolicyId, rehydratePriority, isSealed, lastAccessedTime, expiresOn,
            immutabilityPolicy, hasLegalHold));
    }

    /**
     * @return the time when the blob was created
     */
    public OffsetDateTime getCreationTime() {
        return internalProperties.getCreationTime();
    }

    /**
     * @return the time when the blob was last modified
     */
    public OffsetDateTime getLastModified() {
        return internalProperties.getLastModified();
    }

    /**
     * @return the eTag of the blob
     */
    public String getETag() {
        return internalProperties.getETag();
    }

    /**
     * @return the size of the blob in bytes
     */
    public long getBlobSize() {
        return internalProperties.getBlobSize();
    }

    /**
     * @return the content type of the blob
     */
    public String getContentType() {
        return internalProperties.getContentType();
    }

    /**
     * @return the MD5 of the blob's content
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(internalProperties.getContentMd5());
    }

    /**
     * @return the content encoding of the blob
     */
    public String getContentEncoding() {
        return internalProperties.getContentEncoding();
    }

    /**
     * @return the content disposition of the blob
     */
    public String getContentDisposition() {
        return internalProperties.getContentDisposition();
    }

    /**
     * @return the content language of the blob
     */
    public String getContentLanguage() {
        return internalProperties.getContentLanguage();
    }

    /**
     * @return the cache control of the blob
     */
    public String getCacheControl() {
        return internalProperties.getCacheControl();
    }

    /**
     * @return the current sequence number of the page blob. This is only returned for page blobs.
     */
    public Long getBlobSequenceNumber() {
        return internalProperties.getBlobSequenceNumber();
    }

    /**
     * @return the type of the blob
     */
    public BlobType getBlobType() {
        return internalProperties.getBlobType();
    }

    /**
     * @return the lease status of the blob
     */
    public LeaseStatusType getLeaseStatus() {
        return internalProperties.getLeaseStatus();
    }

    /**
     * @return the lease state of the blob
     */
    public LeaseStateType getLeaseState() {
        return internalProperties.getLeaseState();
    }

    /**
     * @return the lease duration if the blob is leased
     */
    public LeaseDurationType getLeaseDuration() {
        return internalProperties.getLeaseDuration();
    }

    /**
     * @return the identifier of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public String getCopyId() {
        return internalProperties.getCopyId();
    }

    /**
     * @return the status of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public CopyStatusType getCopyStatus() {
        return internalProperties.getCopyStatus();
    }

    /**
     * @return the source blob URL from the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public String getCopySource() {
        return internalProperties.getCopySource();
    }

    /**
     * @return the number of bytes copied and total bytes in the source from the last copy operation (bytes copied/total
     * bytes). If this blob hasn't been the target of a copy operation or has been modified since this won't be set.
     */
    public String getCopyProgress() {
        return internalProperties.getCopyProgress();
    }

    /**
     * @return the completion time of the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public OffsetDateTime getCopyCompletionTime() {
        return internalProperties.getCopyCompletionTime();
    }

    /**
     * @return the description of the last copy failure, this is set when the {@link #getCopyStatus() getCopyStatus} is
     * {@link CopyStatusType#FAILED failed} or {@link CopyStatusType#ABORTED aborted}. If this blob hasn't been the
     * target of a copy operation or has been modified since this won't be set.
     */
    public String getCopyStatusDescription() {
        return internalProperties.getCopyStatusDescription();
    }

    /**
     * @return the status of the blob being encrypted on the server
     */
    public Boolean isServerEncrypted() {
        return internalProperties.isServerEncrypted();
    }

    /**
     * @return the status of the blob being an incremental copy blob
     */
    public Boolean isIncrementalCopy() {
        return internalProperties.isIncrementalCopy();
    }

    /**
     * @return the snapshot time of the last successful incremental copy snapshot for this blob. If this blob isn't an
     * incremental copy blob or incremental copy snapshot or {@link #getCopyStatus() getCopyStatus} isn't {@link
     * CopyStatusType#SUCCESS success} this won't be set.
     */
    public String getCopyDestinationSnapshot() {
        return internalProperties.getCopyDestinationSnapshot();
    }

    /**
     * @return the tier of the blob. This is only set for Page blobs on a premium storage account or for Block blobs on
     * blob storage or general purpose V2 account.
     */
    public AccessTier getAccessTier() {
        return internalProperties.getAccessTier();
    }

    /**
     * @return the status of the tier being inferred for the blob. This is only set for Page blobs on a premium storage
     * account or for Block blobs on blob storage or general purpose V2 account.
     */
    public Boolean isAccessTierInferred() {
        return internalProperties.isAccessTierInferred();
    }

    /**
     * @return the archive status of the blob. This is only for blobs on a blob storage and general purpose v2 account.
     */
    public ArchiveStatus getArchiveStatus() {
        return internalProperties.getArchiveStatus();
    }

    /**
     * @return the key used to encrypt the blob
     */
    public String getEncryptionKeySha256() {
        return internalProperties.getEncryptionKeySha256();
    }

    /**
     * @return The name of the encryption scope under which the blob is encrypted.
     */
    public String getEncryptionScope() {
        return internalProperties.getEncryptionScope();
    }

    /**
     * @return the time when the access tier for the blob was last changed
     */
    public OffsetDateTime getAccessTierChangeTime() {
        return internalProperties.getAccessTierChangeTime();
    }

    /**
     * @return the metadata associated with this blob
     */
    public Map<String, String> getMetadata() {
        return internalProperties.getMetadata();
    }

    /**
     * @return the number of committed blocks in the blob. This is only returned for Append blobs.
     */
    public Integer getCommittedBlockCount() {
        return internalProperties.getCommittedBlockCount();
    }

    /**
     * @return The number of tags associated with the blob.
     */
    public Long getTagCount() {
        return internalProperties.getTagCount();
    }

    /**
     * @return the version identifier the blob.
     */
    public String getVersionId() {
        return internalProperties.getVersionId();
    }

    /**
     * @return the flag indicating whether version identifier points to current version of the blob.
     */
    public Boolean isCurrentVersion() {
        return internalProperties.isCurrentVersion();
    }

    /**
     * @return a {@link List} that contains information on the object replication policies associated with this blob and
     * the status of the replication for each policy. Only available when the blob is the source of object replication.
     */
    public List<ObjectReplicationPolicy> getObjectReplicationSourcePolicies() {
        return Collections.unmodifiableList(internalProperties.getObjectReplicationSourcePolicies());
    }

    /**
     * @return a {@code String} that identifies the Object Replication Policy which made this blob the destination of a
     * copy.
     */
    public String getObjectReplicationDestinationPolicyId() {
        return this.internalProperties.getObjectReplicationDestinationPolicyId();
    }

    /**
     * @return The {@link RehydratePriority} of the blob if it is in RehydratePending state.
     */
    public RehydratePriority getRehydratePriority() {
        return internalProperties.getRehydratePriority();
    }

    /**
     * @return the flag indicating whether this blob has been sealed (marked as read only). This is only returned for
     * Append blobs.
     */
    public Boolean isSealed() {
        return internalProperties.isSealed();
    }

    /**
     * @return The date and time the blob was read or written to.
     */
    public OffsetDateTime getLastAccessedTime() {
        return internalProperties.getLastAccessedTime();
    }

    /**
     * @return the time when the blob is going to expire.
     */
    public OffsetDateTime getExpiresOn() {
        return internalProperties.getExpiresOn();
    }

    /**
     * @return the immutability policy.
     */
    public BlobImmutabilityPolicy getImmutabilityPolicy() {
        return internalProperties.getImmutabilityPolicy();
    }

    /**
     * @return whether the blob has a legal hold.
     */
    public Boolean hasLegalHold() {
        return internalProperties.hasLegalHold();
    }
}
