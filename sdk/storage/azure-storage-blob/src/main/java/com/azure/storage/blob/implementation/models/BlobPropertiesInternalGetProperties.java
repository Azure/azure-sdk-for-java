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
        return headers.getCreationTime();
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
        return headers.getContentMd5();
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
        return headers.getBlobSequenceNumber();
    }

    @Override
    public BlobType getBlobType() {
        return headers.getBlobType();
    }

    @Override
    public LeaseStatusType getLeaseStatus() {
        return headers.getLeaseStatus();
    }

    @Override
    public LeaseStateType getLeaseState() {
        return headers.getLeaseState();
    }

    @Override
    public LeaseDurationType getLeaseDuration() {
        return headers.getDuration();
    }

    @Override
    public String getCopyId() {
        return headers.getCopyId();
    }

    @Override
    public CopyStatusType getCopyStatus() {
        return headers.getCopyStatus();
    }

    @Override
    public String getCopySource() {
        return headers.getCopySource();
    }

    @Override
    public String getCopyProgress() {
        return headers.getCopyProgress();
    }

    @Override
    public OffsetDateTime getCopyCompletionTime() {
        return headers.getCopyCompletionTime();
    }

    @Override
    public String getCopyStatusDescription() {
        return headers.getCopyStatusDescription();
    }

    @Override
    public Boolean isServerEncrypted() {
        return headers.isServerEncrypted();
    }

    @Override
    public Boolean isIncrementalCopy() {
        return headers.isIncrementalCopy();
    }

    @Override
    public String getCopyDestinationSnapshot() {
        return headers.getDestinationSnapshot();
    }

    @Override
    public AccessTier getAccessTier() {
        return AccessTier.fromString(headers.getAccessTier());
    }

    @Override
    public Boolean isAccessTierInferred() {
        return headers.isAccessTierInferred();
    }

    @Override
    public AccessTier getSmartAccessTier() {
        return AccessTier.fromString(headers.getSmartAccessTier());
    }

    @Override
    public ArchiveStatus getArchiveStatus() {
        return ArchiveStatus.fromString(headers.getArchiveStatus());
    }

    @Override
    public String getEncryptionKeySha256() {
        return headers.getEncryptionKeySha256();
    }

    @Override
    public String getEncryptionScope() {
        return headers.getEncryptionScope();
    }

    @Override
    public OffsetDateTime getAccessTierChangeTime() {
        return headers.getAccessTierChangeTime();
    }

    @Override
    public Map<String, String> getMetadata() {
        return headers.getXMsMeta();
    }

    @Override
    public Integer getCommittedBlockCount() {
        return headers.getBlobCommittedBlockCount();
    }

    @Override
    public Long getTagCount() {
        return headers.getTagCount();
    }

    @Override
    public String getVersionId() {
        return headers.getVersionId();
    }

    @Override
    public Boolean isCurrentVersion() {
        return headers.isCurrentVersion();
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
        return RehydratePriority.fromString(headers.getRehydratePriority());
    }

    @Override
    public Boolean isSealed() {
        return headers.isSealed();
    }

    @Override
    public OffsetDateTime getLastAccessedTime() {
        return headers.getLastAccessed();
    }

    @Override
    public OffsetDateTime getExpiresOn() {
        return headers.getExpiresOn();
    }

    @Override
    public BlobImmutabilityPolicy getImmutabilityPolicy() {
        // This could be cached but the returned object is mutable.
        return new BlobImmutabilityPolicy().setExpiryTime(headers.getImmutabilityPolicyExpiresOn())
            .setPolicyMode(headers.getImmutabilityPolicyMode());
    }

    @Override
    public Boolean hasLegalHold() {
        return headers.isLegalHold();
    }

    @Override
    public String getRequestId() {
        return headers.getRequestId();
    }
}
