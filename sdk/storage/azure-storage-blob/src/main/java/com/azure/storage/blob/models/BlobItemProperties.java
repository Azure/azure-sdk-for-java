// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.implementation.accesshelpers.BlobItemPropertiesConstructorProxy;
import com.azure.storage.blob.implementation.models.BlobItemPropertiesInternal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.OffsetDateTime;

/**
 * Properties of a blob.
 */
@JacksonXmlRootElement(localName = "Properties")
@Fluent
public final class BlobItemProperties {
    @JsonUnwrapped
    private final BlobItemPropertiesInternal internalProperties;

    static {
        BlobItemPropertiesConstructorProxy.setAccessor(
            new BlobItemPropertiesConstructorProxy.BlobItemPropertiesConstructorAccessor() {
                @Override
                public BlobItemProperties create(BlobItemPropertiesInternal internalProperties) {
                    return new BlobItemProperties(internalProperties);
                }

                @Override
                public BlobItemPropertiesInternal getInternalProperties(BlobItemProperties properties) {
                    return properties.internalProperties;
                }
            });
    }

    private BlobItemProperties(BlobItemPropertiesInternal internalProperties) {
        this.internalProperties = internalProperties;
    }

    /**
     * Constructs a new instance of {@link BlobItemProperties}.
     */
    public BlobItemProperties() {
        // Added to maintain backwards compatibility as the private constructor removes the implicit no args
        // constructor.
        this.internalProperties = new BlobItemPropertiesInternal();
    }

    @JsonIgnore
    private BlobImmutabilityPolicy convertedImmutabilityPolicy;

    /**
     * Get the creationTime property: The creationTime property.
     *
     * @return the creationTime value.
     */
    public OffsetDateTime getCreationTime() {
        return internalProperties.getCreationTime();
    }

    /**
     * Set the creationTime property: The creationTime property.
     *
     * @param creationTime the creationTime value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setCreationTime(OffsetDateTime creationTime) {
        internalProperties.setCreationTime(creationTime);
        return this;
    }

    /**
     * Get the lastModified property: The lastModified property.
     *
     * @return the lastModified value.
     */
    public OffsetDateTime getLastModified() {
        return internalProperties.getLastModified();
    }

    /**
     * Set the lastModified property: The lastModified property.
     *
     * @param lastModified the lastModified value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setLastModified(OffsetDateTime lastModified) {
        internalProperties.setLastModified(lastModified);
        return this;
    }

    /**
     * Get the eTag property: The eTag property.
     *
     * @return the eTag value.
     */
    public String getETag() {
        return internalProperties.getETag();
    }

    /**
     * Set the eTag property: The eTag property.
     *
     * @param eTag the eTag value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setETag(String eTag) {
        internalProperties.setETag(eTag);
        return this;
    }

    /**
     * Get the contentLength property: Size in bytes.
     *
     * @return the contentLength value.
     */
    public Long getContentLength() {
        return internalProperties.getContentLength();
    }

    /**
     * Set the contentLength property: Size in bytes.
     *
     * @param contentLength the contentLength value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setContentLength(Long contentLength) {
        internalProperties.setContentLength(contentLength);
        return this;
    }

    /**
     * Get the contentType property: The contentType property.
     *
     * @return the contentType value.
     */
    public String getContentType() {
        return internalProperties.getContentType();
    }

    /**
     * Set the contentType property: The contentType property.
     *
     * @param contentType the contentType value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setContentType(String contentType) {
        internalProperties.setContentType(contentType);
        return this;
    }

    /**
     * Get the contentEncoding property: The contentEncoding property.
     *
     * @return the contentEncoding value.
     */
    public String getContentEncoding() {
        return internalProperties.getContentEncoding();
    }

    /**
     * Set the contentEncoding property: The contentEncoding property.
     *
     * @param contentEncoding the contentEncoding value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setContentEncoding(String contentEncoding) {
        internalProperties.setContentEncoding(contentEncoding);
        return this;
    }

    /**
     * Get the contentLanguage property: The contentLanguage property.
     *
     * @return the contentLanguage value.
     */
    public String getContentLanguage() {
        return internalProperties.getContentLanguage();
    }

    /**
     * Set the contentLanguage property: The contentLanguage property.
     *
     * @param contentLanguage the contentLanguage value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setContentLanguage(String contentLanguage) {
        internalProperties.setContentLanguage(contentLanguage);
        return this;
    }

    /**
     * Get the contentMd5 property: The contentMd5 property.
     *
     * @return the contentMd5 value.
     */
    public byte[] getContentMd5() {
        return internalProperties.getContentMd5();
    }

    /**
     * Set the contentMd5 property: The contentMd5 property.
     *
     * @param contentMd5 the contentMd5 value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setContentMd5(byte[] contentMd5) {
        internalProperties.setContentMd5(contentMd5);
        return this;
    }

    /**
     * Get the contentDisposition property: The contentDisposition property.
     *
     * @return the contentDisposition value.
     */
    public String getContentDisposition() {
        return internalProperties.getContentDisposition();
    }

    /**
     * Set the contentDisposition property: The contentDisposition property.
     *
     * @param contentDisposition the contentDisposition value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setContentDisposition(String contentDisposition) {
        internalProperties.setContentDisposition(contentDisposition);
        return this;
    }

    /**
     * Get the cacheControl property: The cacheControl property.
     *
     * @return the cacheControl value.
     */
    public String getCacheControl() {
        return internalProperties.getCacheControl();
    }

    /**
     * Set the cacheControl property: The cacheControl property.
     *
     * @param cacheControl the cacheControl value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setCacheControl(String cacheControl) {
        internalProperties.setCacheControl(cacheControl);
        return this;
    }

    /**
     * Get the blobSequenceNumber property: The blobSequenceNumber property.
     *
     * @return the blobSequenceNumber value.
     */
    public Long getBlobSequenceNumber() {
        return internalProperties.getBlobSequenceNumber();
    }

    /**
     * Set the blobSequenceNumber property: The blobSequenceNumber property.
     *
     * @param blobSequenceNumber the blobSequenceNumber value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setBlobSequenceNumber(Long blobSequenceNumber) {
        internalProperties.setBlobSequenceNumber(blobSequenceNumber);
        return this;
    }

    /**
     * Get the blobType property: Possible values include: 'BlockBlob', 'PageBlob', 'AppendBlob'.
     *
     * @return the blobType value.
     */
    public BlobType getBlobType() {
        return internalProperties.getBlobType();
    }

    /**
     * Set the blobType property: Possible values include: 'BlockBlob', 'PageBlob', 'AppendBlob'.
     *
     * @param blobType the blobType value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setBlobType(BlobType blobType) {
        internalProperties.setBlobType(blobType);
        return this;
    }

    /**
     * Get the leaseStatus property: Possible values include: 'locked', 'unlocked'.
     *
     * @return the leaseStatus value.
     */
    public LeaseStatusType getLeaseStatus() {
        return internalProperties.getLeaseStatus();
    }

    /**
     * Set the leaseStatus property: Possible values include: 'locked', 'unlocked'.
     *
     * @param leaseStatus the leaseStatus value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setLeaseStatus(LeaseStatusType leaseStatus) {
        internalProperties.setLeaseStatus(leaseStatus);
        return this;
    }

    /**
     * Get the leaseState property: Possible values include: 'available', 'leased', 'expired', 'breaking', 'broken'.
     *
     * @return the leaseState value.
     */
    public LeaseStateType getLeaseState() {
        return internalProperties.getLeaseState();
    }

    /**
     * Set the leaseState property: Possible values include: 'available', 'leased', 'expired', 'breaking', 'broken'.
     *
     * @param leaseState the leaseState value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setLeaseState(LeaseStateType leaseState) {
        internalProperties.setLeaseState(leaseState);
        return this;
    }

    /**
     * Get the leaseDuration property: Possible values include: 'infinite', 'fixed'.
     *
     * @return the leaseDuration value.
     */
    public LeaseDurationType getLeaseDuration() {
        return internalProperties.getLeaseDuration();
    }

    /**
     * Set the leaseDuration property: Possible values include: 'infinite', 'fixed'.
     *
     * @param leaseDuration the leaseDuration value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setLeaseDuration(LeaseDurationType leaseDuration) {
        internalProperties.setLeaseDuration(leaseDuration);
        return this;
    }

    /**
     * Get the copyId property: The copyId property.
     *
     * @return the copyId value.
     */
    public String getCopyId() {
        return internalProperties.getCopyId();
    }

    /**
     * Set the copyId property: The copyId property.
     *
     * @param copyId the copyId value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setCopyId(String copyId) {
        internalProperties.setCopyId(copyId);
        return this;
    }

    /**
     * Get the copyStatus property: Possible values include: 'pending', 'success', 'aborted', 'failed'.
     *
     * @return the copyStatus value.
     */
    public CopyStatusType getCopyStatus() {
        return internalProperties.getCopyStatus();
    }

    /**
     * Set the copyStatus property: Possible values include: 'pending', 'success', 'aborted', 'failed'.
     *
     * @param copyStatus the copyStatus value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setCopyStatus(CopyStatusType copyStatus) {
        internalProperties.setCopyStatus(copyStatus);
        return this;
    }

    /**
     * Get the copySource property: The copySource property.
     *
     * @return the copySource value.
     */
    public String getCopySource() {
        return internalProperties.getCopySource();
    }

    /**
     * Set the copySource property: The copySource property.
     *
     * @param copySource the copySource value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setCopySource(String copySource) {
        internalProperties.setCopySource(copySource);
        return this;
    }

    /**
     * Get the copyProgress property: The copyProgress property.
     *
     * @return the copyProgress value.
     */
    public String getCopyProgress() {
        return internalProperties.getCopyProgress();
    }

    /**
     * Set the copyProgress property: The copyProgress property.
     *
     * @param copyProgress the copyProgress value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setCopyProgress(String copyProgress) {
        internalProperties.setCopyProgress(copyProgress);
        return this;
    }

    /**
     * Get the copyCompletionTime property: The copyCompletionTime property.
     *
     * @return the copyCompletionTime value.
     */
    public OffsetDateTime getCopyCompletionTime() {
        return internalProperties.getCopyCompletionTime();
    }

    /**
     * Set the copyCompletionTime property: The copyCompletionTime property.
     *
     * @param copyCompletionTime the copyCompletionTime value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setCopyCompletionTime(OffsetDateTime copyCompletionTime) {
        internalProperties.setCopyCompletionTime(copyCompletionTime);
        return this;
    }

    /**
     * Get the copyStatusDescription property: The copyStatusDescription property.
     *
     * @return the copyStatusDescription value.
     */
    public String getCopyStatusDescription() {
        return internalProperties.getCopyStatusDescription();
    }

    /**
     * Set the copyStatusDescription property: The copyStatusDescription property.
     *
     * @param copyStatusDescription the copyStatusDescription value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setCopyStatusDescription(String copyStatusDescription) {
        internalProperties.setCopyStatusDescription(copyStatusDescription);
        return this;
    }

    /**
     * Get the serverEncrypted property: The serverEncrypted property.
     *
     * @return the serverEncrypted value.
     */
    public Boolean isServerEncrypted() {
        return internalProperties.isServerEncrypted();
    }

    /**
     * Set the serverEncrypted property: The serverEncrypted property.
     *
     * @param serverEncrypted the serverEncrypted value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setServerEncrypted(Boolean serverEncrypted) {
        internalProperties.setServerEncrypted(serverEncrypted);
        return this;
    }

    /**
     * Get the incrementalCopy property: The incrementalCopy property.
     *
     * @return the incrementalCopy value.
     */
    public Boolean isIncrementalCopy() {
        return internalProperties.isIncrementalCopy();
    }

    /**
     * Set the incrementalCopy property: The incrementalCopy property.
     *
     * @param incrementalCopy the incrementalCopy value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setIncrementalCopy(Boolean incrementalCopy) {
        internalProperties.setIncrementalCopy(incrementalCopy);
        return this;
    }

    /**
     * Get the destinationSnapshot property: The destinationSnapshot property.
     *
     * @return the destinationSnapshot value.
     */
    public String getDestinationSnapshot() {
        return internalProperties.getDestinationSnapshot();
    }

    /**
     * Set the destinationSnapshot property: The destinationSnapshot property.
     *
     * @param destinationSnapshot the destinationSnapshot value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setDestinationSnapshot(String destinationSnapshot) {
        internalProperties.setDestinationSnapshot(destinationSnapshot);
        return this;
    }

    /**
     * Get the deletedTime property: The deletedTime property.
     *
     * @return the deletedTime value.
     */
    public OffsetDateTime getDeletedTime() {
        return internalProperties.getDeletedTime();
    }

    /**
     * Set the deletedTime property: The deletedTime property.
     *
     * @param deletedTime the deletedTime value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setDeletedTime(OffsetDateTime deletedTime) {
        internalProperties.setDeletedTime(deletedTime);
        return this;
    }

    /**
     * Get the remainingRetentionDays property: The remainingRetentionDays property.
     *
     * @return the remainingRetentionDays value.
     */
    public Integer getRemainingRetentionDays() {
        return internalProperties.getRemainingRetentionDays();
    }

    /**
     * Set the remainingRetentionDays property: The remainingRetentionDays property.
     *
     * @param remainingRetentionDays the remainingRetentionDays value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setRemainingRetentionDays(Integer remainingRetentionDays) {
        internalProperties.setRemainingRetentionDays(remainingRetentionDays);
        return this;
    }

    /**
     * Get the accessTier property: Possible values include: 'P4', 'P6', 'P10', 'P15', 'P20', 'P30', 'P40', 'P50',
     * 'P60', 'P70', 'P80', 'Hot', 'Cool', 'Archive'.
     *
     * @return the accessTier value.
     */
    public AccessTier getAccessTier() {
        return internalProperties.getAccessTier();
    }

    /**
     * Set the accessTier property: Possible values include: 'P4', 'P6', 'P10', 'P15', 'P20', 'P30', 'P40', 'P50',
     * 'P60', 'P70', 'P80', 'Hot', 'Cool', 'Archive'.
     *
     * @param accessTier the accessTier value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setAccessTier(AccessTier accessTier) {
        internalProperties.setAccessTier(accessTier);
        return this;
    }

    /**
     * Get the accessTierInferred property: The accessTierInferred property.
     *
     * @return the accessTierInferred value.
     */
    public Boolean isAccessTierInferred() {
        return internalProperties.isAccessTierInferred();
    }

    /**
     * Set the accessTierInferred property: The accessTierInferred property.
     *
     * @param accessTierInferred the accessTierInferred value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setAccessTierInferred(Boolean accessTierInferred) {
        internalProperties.setAccessTierInferred(accessTierInferred);
        return this;
    }

    /**
     * Get the archiveStatus property: Possible values include: 'rehydrate-pending-to-hot',
     * 'rehydrate-pending-to-cool'.
     *
     * @return the archiveStatus value.
     */
    public ArchiveStatus getArchiveStatus() {
        return internalProperties.getArchiveStatus();
    }

    /**
     * Set the archiveStatus property: Possible values include: 'rehydrate-pending-to-hot',
     * 'rehydrate-pending-to-cool'.
     *
     * @param archiveStatus the archiveStatus value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setArchiveStatus(ArchiveStatus archiveStatus) {
        internalProperties.setArchiveStatus(archiveStatus);
        return this;
    }

    /**
     * Get the customerProvidedKeySha256 property: The customerProvidedKeySha256 property.
     *
     * @return the customerProvidedKeySha256 value.
     */
    public String getCustomerProvidedKeySha256() {
        return internalProperties.getCustomerProvidedKeySha256();
    }

    /**
     * Set the customerProvidedKeySha256 property: The customerProvidedKeySha256 property.
     *
     * @param customerProvidedKeySha256 the customerProvidedKeySha256 value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setCustomerProvidedKeySha256(String customerProvidedKeySha256) {
        internalProperties.setCustomerProvidedKeySha256(customerProvidedKeySha256);
        return this;
    }

    /**
     * Get the encryptionScope property: The name of the encryption scope under which the blob is encrypted.
     *
     * @return the encryptionScope value.
     */
    public String getEncryptionScope() {
        return internalProperties.getEncryptionScope();
    }

    /**
     * Set the encryptionScope property: The name of the encryption scope under which the blob is encrypted.
     *
     * @param encryptionScope the encryptionScope value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setEncryptionScope(String encryptionScope) {
        internalProperties.setEncryptionScope(encryptionScope);
        return this;
    }

    /**
     * Get the accessTierChangeTime property: The accessTierChangeTime property.
     *
     * @return the accessTierChangeTime value.
     */
    public OffsetDateTime getAccessTierChangeTime() {
        return internalProperties.getAccessTierChangeTime();
    }

    /**
     * Set the accessTierChangeTime property: The accessTierChangeTime property.
     *
     * @param accessTierChangeTime the accessTierChangeTime value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setAccessTierChangeTime(OffsetDateTime accessTierChangeTime) {
        internalProperties.setAccessTierChangeTime(accessTierChangeTime);
        return this;
    }

    /**
     * Get the tagCount property: The tagCount property.
     *
     * @return the tagCount value.
     */
    public Integer getTagCount() {
        return internalProperties.getTagCount();
    }

    /**
     * Set the tagCount property: The tagCount property.
     *
     * @param tagCount the tagCount value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setTagCount(Integer tagCount) {
        internalProperties.setTagCount(tagCount);
        return this;
    }

    /**
     * Get the rehydratePriority property: Possible values include: 'High', 'Standard'.
     *
     * @return the rehydratePriority value.
     */
    public RehydratePriority getRehydratePriority() {
        return internalProperties.getRehydratePriority();
    }

    /**
     * Set the rehydratePriority property: Possible values include: 'High', 'Standard'.
     *
     * @param rehydratePriority the rehydratePriority value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setRehydratePriority(RehydratePriority rehydratePriority) {
        internalProperties.setRehydratePriority(rehydratePriority);
        return this;
    }

    /**
     * Get the sealed property: The sealed property.
     *
     * @return Whether or not the blob is sealed  (marked as read only). This is only applicable for Append blobs.
     */
    public Boolean isSealed() {
        return internalProperties.isSealed();
    }

    /**
     * Set the sealed property: The sealed property.
     *
     * @param sealed Whether or not the blob is sealed  (marked as read only). This is only applicable for Append
     * blobs.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setSealed(Boolean sealed) {
        internalProperties.setIsSealed(sealed);
        return this;
    }

    /**
     * Get the lastAccessedTime property: The lastAccessedTime property.
     *
     * @return the lastAccessedTime value.
     */
    public OffsetDateTime getLastAccessedTime() {
        return internalProperties.getLastAccessedOn();
    }

    /**
     * Set the lastAccessedTime property: The lastAccessedTime property.
     *
     * @param lastAccessedTime the lastAccessedTime value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setLastAccessedTime(OffsetDateTime lastAccessedTime) {
        internalProperties.setLastAccessedOn(lastAccessedTime);
        return this;
    }

    /**
     * Get the expiryTime property: The Expiry-Time property.
     *
     * @return the expiryTime value.
     */
    public OffsetDateTime getExpiryTime() {
        return internalProperties.getExpiresOn();
    }

    /**
     * Set the expiryTime property: The Expiry-Time property.
     *
     * @param expiryTime the expiryTime value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setExpiryTime(OffsetDateTime expiryTime) {
        internalProperties.setExpiresOn(expiryTime);
        return this;
    }

    /**
     * Get the immutabilityPolicy property: The ImmutabilityPolicyUntilDate and ImmutabilityPolicyMode property.
     *
     * @return the immutabilityPolicy value.
     */
    public BlobImmutabilityPolicy getImmutabilityPolicy() {
        if (convertedImmutabilityPolicy == null) {
            convertedImmutabilityPolicy = new BlobImmutabilityPolicy()
                .setExpiryTime(internalProperties.getImmutabilityPolicyExpiresOn())
                .setPolicyMode(internalProperties.getImmutabilityPolicyMode());
        }

        return convertedImmutabilityPolicy;
    }

    /**
     * Set the immutabilityPolicy property: The ImmutabilityPolicyUntilDate and ImmutabilityPolicyMode property.
     *
     * @param immutabilityPolicy the immutabilityPolicy value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy) {
        this.convertedImmutabilityPolicy = immutabilityPolicy;
        if (immutabilityPolicy != null) {
            internalProperties.setImmutabilityPolicyExpiresOn(immutabilityPolicy.getExpiryTime());
            internalProperties.setImmutabilityPolicyMode(immutabilityPolicy.getPolicyMode());
        } else {
            internalProperties.setImmutabilityPolicyExpiresOn(null);
            internalProperties.setImmutabilityPolicyMode(null);
        }

        return this;
    }

    /**
     * Get the hasLegalHold property: The LegalHold property.
     *
     * @return the hasLegalHold value.
     */
    public Boolean hasLegalHold() {
        return internalProperties.isLegalHold();
    }

    /**
     * Set the hasLegalHold property: The LegalHold property.
     *
     * @param hasLegalHold the hasLegalHold value to set.
     * @return the BlobItemProperties object itself.
     */
    public BlobItemProperties setHasLegalHold(Boolean hasLegalHold) {
        internalProperties.setLegalHold(hasLegalHold);
        return this;
    }
}
