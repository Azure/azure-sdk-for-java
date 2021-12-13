// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.models;

import com.azure.storage.blob.implementation.util.ModelHelper;
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
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link BlobPropertiesInternal} that represents the get properties {@link BlobProperties}.
 */
public final class BlobPropertiesInternalGetProperties implements BlobPropertiesInternal {
    private final BlobsGetPropertiesHeaders headers;

    /**
     * Creates an instance of {@link BlobPropertiesInternalGetProperties}.
     *
     * @param headers The get properties headers.
     */
    public BlobPropertiesInternalGetProperties(BlobsGetPropertiesHeaders headers) {
        this.headers = headers;
    }

    @Override
    public OffsetDateTime getCreationTime() {
        return headers.getXMsCreationTime();
    }

    @Override
    public OffsetDateTime getLastModified() {
        return headers.getLastModified();
    }

    @Override
    public String getETag() {
        return headers.getETag();
    }

    @Override
    public long getBlobSize() {
        return headers.getContentLength() == null ? 0 : headers.getContentLength();
    }

    @Override
    public String getContentType() {
        return headers.getContentType();
    }

    @Override
    public byte[] getContentMd5() {
        return headers.getContentMD5();
    }

    @Override
    public String getContentEncoding() {
        return headers.getContentEncoding();
    }

    @Override
    public String getContentDisposition() {
        return headers.getContentDisposition();
    }

    @Override
    public String getContentLanguage() {
        return headers.getContentLanguage();
    }

    @Override
    public String getCacheControl() {
        return headers.getCacheControl();
    }

    @Override
    public Long getBlobSequenceNumber() {
        return headers.getXMsBlobSequenceNumber();
    }

    @Override
    public BlobType getBlobType() {
        return headers.getXMsBlobType();
    }

    @Override
    public LeaseStatusType getLeaseStatus() {
        return headers.getXMsLeaseStatus();
    }

    @Override
    public LeaseStateType getLeaseState() {
        return headers.getXMsLeaseState();
    }

    @Override
    public LeaseDurationType getLeaseDuration() {
        return headers.getXMsLeaseDuration();
    }

    @Override
    public String getCopyId() {
        return headers.getXMsCopyId();
    }

    @Override
    public CopyStatusType getCopyStatus() {
        return headers.getXMsCopyStatus();
    }

    @Override
    public String getCopySource() {
        return headers.getXMsCopySource();
    }

    @Override
    public String getCopyProgress() {
        return headers.getXMsCopyProgress();
    }

    @Override
    public OffsetDateTime getCopyCompletionTime() {
        return headers.getXMsCopyCompletionTime();
    }

    @Override
    public String getCopyStatusDescription() {
        return headers.getXMsCopyStatusDescription();
    }

    @Override
    public Boolean isServerEncrypted() {
        return headers.isXMsServerEncrypted();
    }

    @Override
    public Boolean isIncrementalCopy() {
        return headers.isXMsIncrementalCopy();
    }

    @Override
    public String getCopyDestinationSnapshot() {
        return headers.getXMsCopyDestinationSnapshot();
    }

    @Override
    public AccessTier getAccessTier() {
        return AccessTier.fromString(headers.getXMsAccessTier());
    }

    @Override
    public Boolean isAccessTierInferred() {
        return headers.isXMsAccessTierInferred();
    }

    @Override
    public ArchiveStatus getArchiveStatus() {
        return ArchiveStatus.fromString(headers.getXMsArchiveStatus());
    }

    @Override
    public String getEncryptionKeySha256() {
        return headers.getXMsEncryptionKeySha256();
    }

    @Override
    public String getEncryptionScope() {
        return headers.getXMsEncryptionScope();
    }

    @Override
    public OffsetDateTime getAccessTierChangeTime() {
        return headers.getXMsAccessTierChangeTime();
    }

    @Override
    public Map<String, String> getMetadata() {
        return headers.getXMsMeta();
    }

    @Override
    public Integer getCommittedBlockCount() {
        return headers.getXMsBlobCommittedBlockCount();
    }

    @Override
    public Long getTagCount() {
        return headers.getXMsTagCount();
    }

    @Override
    public String getVersionId() {
        return headers.getXMsVersionId();
    }

    @Override
    public Boolean isCurrentVersion() {
        return headers.isXMsIsCurrentVersion();
    }

    @Override
    public List<ObjectReplicationPolicy> getObjectReplicationSourcePolicies() {
        return ModelHelper.getObjectReplicationSourcePolicies(headers.getXMsOr());
    }

    @Override
    public String getObjectReplicationDestinationPolicyId() {
        return ModelHelper.getObjectReplicationDestinationPolicyId(headers.getXMsOr());
    }

    @Override
    public RehydratePriority getRehydratePriority() {
        return RehydratePriority.fromString(headers.getXMsRehydratePriority());
    }

    @Override
    public Boolean isSealed() {
        return headers.isXMsBlobSealed();
    }

    @Override
    public OffsetDateTime getLastAccessedTime() {
        return headers.getXMsLastAccessTime();
    }

    @Override
    public OffsetDateTime getExpiresOn() {
        return headers.getXMsExpiryTime();
    }

    @Override
    public BlobImmutabilityPolicy getImmutabilityPolicy() {
        // This could be cached but the returned object is mutable.
        return new BlobImmutabilityPolicy().setExpiryTime(headers.getXMsImmutabilityPolicyUntilDate())
            .setPolicyMode(headers.getXMsImmutabilityPolicyMode());
    }

    @Override
    public Boolean hasLegalHold() {
        return headers.isXMsLegalHold();
    }
}
