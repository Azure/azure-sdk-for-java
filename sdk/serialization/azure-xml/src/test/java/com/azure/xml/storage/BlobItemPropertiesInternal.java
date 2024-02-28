// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

import javax.xml.stream.XMLStreamException;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static com.azure.xml.AzureXmlTestUtils.getRootElementName;

public class BlobItemPropertiesInternal implements XmlSerializable<BlobItemPropertiesInternal> {
    /*
     * The Creation-Time property.
     */
    private DateTimeRfc1123 creationTime;

    /*
     * The Last-Modified property.
     */
    private DateTimeRfc1123 lastModified;

    /*
     * The Etag property.
     */
    private String eTag;

    /*
     * Size in bytes
     */
    private Long contentLength;

    /*
     * The Content-Type property.
     */
    private String contentType;

    /*
     * The Content-Encoding property.
     */
    private String contentEncoding;

    /*
     * The Content-Language property.
     */
    private String contentLanguage;

    /*
     * The Content-MD5 property.
     */
    private byte[] contentMd5;

    /*
     * The Content-Disposition property.
     */
    private String contentDisposition;

    /*
     * The Cache-Control property.
     */
    private String cacheControl;

    /*
     * The x-ms-blob-sequence-number property.
     */
    private Long blobSequenceNumber;

    /*
     * The BlobType property.
     */
    private BlobType blobType;

    /*
     * The LeaseStatus property.
     */
    private LeaseStatusType leaseStatus;

    /*
     * The LeaseState property.
     */
    private LeaseStateType leaseState;

    /*
     * The LeaseDuration property.
     */
    private LeaseDurationType leaseDuration;

    /*
     * The CopyId property.
     */
    private String copyId;

    /*
     * The CopyStatus property.
     */
    private CopyStatusType copyStatus;

    /*
     * The CopySource property.
     */
    private String copySource;

    /*
     * The CopyProgress property.
     */
    private String copyProgress;

    /*
     * The CopyCompletionTime property.
     */
    private DateTimeRfc1123 copyCompletionTime;

    /*
     * The CopyStatusDescription property.
     */
    private String copyStatusDescription;

    /*
     * The ServerEncrypted property.
     */
    private Boolean serverEncrypted;

    /*
     * The IncrementalCopy property.
     */
    private Boolean incrementalCopy;

    /*
     * The DestinationSnapshot property.
     */
    private String destinationSnapshot;

    /*
     * The DeletedTime property.
     */
    private DateTimeRfc1123 deletedTime;

    /*
     * The RemainingRetentionDays property.
     */
    private Integer remainingRetentionDays;

    /*
     * The AccessTier property.
     */
    private AccessTier accessTier;

    /*
     * The AccessTierInferred property.
     */
    private Boolean accessTierInferred;

    /*
     * The ArchiveStatus property.
     */
    private ArchiveStatus archiveStatus;

    /*
     * The CustomerProvidedKeySha256 property.
     */
    private String customerProvidedKeySha256;

    /*
     * The name of the encryption scope under which the blob is encrypted.
     */
    private String encryptionScope;

    /*
     * The AccessTierChangeTime property.
     */
    private DateTimeRfc1123 accessTierChangeTime;

    /*
     * The TagCount property.
     */
    private Integer tagCount;

    /*
     * The Expiry-Time property.
     */
    private DateTimeRfc1123 expiresOn;

    /*
     * The Sealed property.
     */
    private Boolean isSealed;

    /*
     * If an object is in rehydrate pending state then this header is returned
     * with priority of rehydrate. Valid values are High and Standard.
     */
    private RehydratePriority rehydratePriority;

    /*
     * The LastAccessTime property.
     */
    private DateTimeRfc1123 lastAccessedOn;

    /*
     * The ImmutabilityPolicyUntilDate property.
     */
    private DateTimeRfc1123 immutabilityPolicyExpiresOn;

    /*
     * The ImmutabilityPolicyMode property.
     */
    private BlobImmutabilityPolicyMode immutabilityPolicyMode;

    /*
     * The LegalHold property.
     */
    private Boolean legalHold;

    /**
     * Get the creationTime property: The Creation-Time property.
     *
     * @return the creationTime value.
     */
    public OffsetDateTime getCreationTime() {
        if (this.creationTime == null) {
            return null;
        }
        return this.creationTime.getDateTime();
    }

    /**
     * Set the creationTime property: The Creation-Time property.
     *
     * @param creationTime the creationTime value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setCreationTime(OffsetDateTime creationTime) {
        if (creationTime == null) {
            this.creationTime = null;
        } else {
            this.creationTime = new DateTimeRfc1123(creationTime);
        }
        return this;
    }

    /**
     * Get the lastModified property: The Last-Modified property.
     *
     * @return the lastModified value.
     */
    public OffsetDateTime getLastModified() {
        if (this.lastModified == null) {
            return null;
        }
        return this.lastModified.getDateTime();
    }

    /**
     * Set the lastModified property: The Last-Modified property.
     *
     * @param lastModified the lastModified value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setLastModified(OffsetDateTime lastModified) {
        if (lastModified == null) {
            this.lastModified = null;
        } else {
            this.lastModified = new DateTimeRfc1123(lastModified);
        }
        return this;
    }

    /**
     * Get the eTag property: The Etag property.
     *
     * @return the eTag value.
     */
    public String getETag() {
        return this.eTag;
    }

    /**
     * Set the eTag property: The Etag property.
     *
     * @param eTag the eTag value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setETag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    /**
     * Get the contentLength property: Size in bytes.
     *
     * @return the contentLength value.
     */
    public Long getContentLength() {
        return this.contentLength;
    }

    /**
     * Set the contentLength property: Size in bytes.
     *
     * @param contentLength the contentLength value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setContentLength(Long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    /**
     * Get the contentType property: The Content-Type property.
     *
     * @return the contentType value.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the contentType property: The Content-Type property.
     *
     * @param contentType the contentType value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get the contentEncoding property: The Content-Encoding property.
     *
     * @return the contentEncoding value.
     */
    public String getContentEncoding() {
        return this.contentEncoding;
    }

    /**
     * Set the contentEncoding property: The Content-Encoding property.
     *
     * @param contentEncoding the contentEncoding value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    /**
     * Get the contentLanguage property: The Content-Language property.
     *
     * @return the contentLanguage value.
     */
    public String getContentLanguage() {
        return this.contentLanguage;
    }

    /**
     * Set the contentLanguage property: The Content-Language property.
     *
     * @param contentLanguage the contentLanguage value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
        return this;
    }

    /**
     * Get the contentMd5 property: The Content-MD5 property.
     *
     * @return the contentMd5 value.
     */
    public byte[] getContentMd5() {
        return (contentMd5 == null) ? null : Arrays.copyOf(contentMd5, contentMd5.length);
    }

    /**
     * Set the contentMd5 property: The Content-MD5 property.
     *
     * @param contentMd5 the contentMd5 value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setContentMd5(byte[] contentMd5) {
        this.contentMd5 = (contentMd5 == null) ? null : Arrays.copyOf(contentMd5, contentMd5.length);
        return this;
    }

    /**
     * Get the contentDisposition property: The Content-Disposition property.
     *
     * @return the contentDisposition value.
     */
    public String getContentDisposition() {
        return this.contentDisposition;
    }

    /**
     * Set the contentDisposition property: The Content-Disposition property.
     *
     * @param contentDisposition the contentDisposition value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
        return this;
    }

    /**
     * Get the cacheControl property: The Cache-Control property.
     *
     * @return the cacheControl value.
     */
    public String getCacheControl() {
        return this.cacheControl;
    }

    /**
     * Set the cacheControl property: The Cache-Control property.
     *
     * @param cacheControl the cacheControl value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }

    /**
     * Get the blobSequenceNumber property: The x-ms-blob-sequence-number property.
     *
     * @return the blobSequenceNumber value.
     */
    public Long getBlobSequenceNumber() {
        return this.blobSequenceNumber;
    }

    /**
     * Set the blobSequenceNumber property: The x-ms-blob-sequence-number property.
     *
     * @param blobSequenceNumber the blobSequenceNumber value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setBlobSequenceNumber(Long blobSequenceNumber) {
        this.blobSequenceNumber = blobSequenceNumber;
        return this;
    }

    /**
     * Get the blobType property: The BlobType property.
     *
     * @return the blobType value.
     */
    public BlobType getBlobType() {
        return this.blobType;
    }

    /**
     * Set the blobType property: The BlobType property.
     *
     * @param blobType the blobType value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setBlobType(BlobType blobType) {
        this.blobType = blobType;
        return this;
    }

    /**
     * Get the leaseStatus property: The LeaseStatus property.
     *
     * @return the leaseStatus value.
     */
    public LeaseStatusType getLeaseStatus() {
        return this.leaseStatus;
    }

    /**
     * Set the leaseStatus property: The LeaseStatus property.
     *
     * @param leaseStatus the leaseStatus value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setLeaseStatus(LeaseStatusType leaseStatus) {
        this.leaseStatus = leaseStatus;
        return this;
    }

    /**
     * Get the leaseState property: The LeaseState property.
     *
     * @return the leaseState value.
     */
    public LeaseStateType getLeaseState() {
        return this.leaseState;
    }

    /**
     * Set the leaseState property: The LeaseState property.
     *
     * @param leaseState the leaseState value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setLeaseState(LeaseStateType leaseState) {
        this.leaseState = leaseState;
        return this;
    }

    /**
     * Get the leaseDuration property: The LeaseDuration property.
     *
     * @return the leaseDuration value.
     */
    public LeaseDurationType getLeaseDuration() {
        return this.leaseDuration;
    }

    /**
     * Set the leaseDuration property: The LeaseDuration property.
     *
     * @param leaseDuration the leaseDuration value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setLeaseDuration(LeaseDurationType leaseDuration) {
        this.leaseDuration = leaseDuration;
        return this;
    }

    /**
     * Get the copyId property: The CopyId property.
     *
     * @return the copyId value.
     */
    public String getCopyId() {
        return this.copyId;
    }

    /**
     * Set the copyId property: The CopyId property.
     *
     * @param copyId the copyId value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setCopyId(String copyId) {
        this.copyId = copyId;
        return this;
    }

    /**
     * Get the copyStatus property: The CopyStatus property.
     *
     * @return the copyStatus value.
     */
    public CopyStatusType getCopyStatus() {
        return this.copyStatus;
    }

    /**
     * Set the copyStatus property: The CopyStatus property.
     *
     * @param copyStatus the copyStatus value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setCopyStatus(CopyStatusType copyStatus) {
        this.copyStatus = copyStatus;
        return this;
    }

    /**
     * Get the copySource property: The CopySource property.
     *
     * @return the copySource value.
     */
    public String getCopySource() {
        return this.copySource;
    }

    /**
     * Set the copySource property: The CopySource property.
     *
     * @param copySource the copySource value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setCopySource(String copySource) {
        this.copySource = copySource;
        return this;
    }

    /**
     * Get the copyProgress property: The CopyProgress property.
     *
     * @return the copyProgress value.
     */
    public String getCopyProgress() {
        return this.copyProgress;
    }

    /**
     * Set the copyProgress property: The CopyProgress property.
     *
     * @param copyProgress the copyProgress value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setCopyProgress(String copyProgress) {
        this.copyProgress = copyProgress;
        return this;
    }

    /**
     * Get the copyCompletionTime property: The CopyCompletionTime property.
     *
     * @return the copyCompletionTime value.
     */
    public OffsetDateTime getCopyCompletionTime() {
        if (this.copyCompletionTime == null) {
            return null;
        }
        return this.copyCompletionTime.getDateTime();
    }

    /**
     * Set the copyCompletionTime property: The CopyCompletionTime property.
     *
     * @param copyCompletionTime the copyCompletionTime value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setCopyCompletionTime(OffsetDateTime copyCompletionTime) {
        if (copyCompletionTime == null) {
            this.copyCompletionTime = null;
        } else {
            this.copyCompletionTime = new DateTimeRfc1123(copyCompletionTime);
        }
        return this;
    }

    /**
     * Get the copyStatusDescription property: The CopyStatusDescription property.
     *
     * @return the copyStatusDescription value.
     */
    public String getCopyStatusDescription() {
        return this.copyStatusDescription;
    }

    /**
     * Set the copyStatusDescription property: The CopyStatusDescription property.
     *
     * @param copyStatusDescription the copyStatusDescription value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setCopyStatusDescription(String copyStatusDescription) {
        this.copyStatusDescription = copyStatusDescription;
        return this;
    }

    /**
     * Get the serverEncrypted property: The ServerEncrypted property.
     *
     * @return the serverEncrypted value.
     */
    public Boolean isServerEncrypted() {
        return this.serverEncrypted;
    }

    /**
     * Set the serverEncrypted property: The ServerEncrypted property.
     *
     * @param serverEncrypted the serverEncrypted value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setServerEncrypted(Boolean serverEncrypted) {
        this.serverEncrypted = serverEncrypted;
        return this;
    }

    /**
     * Get the incrementalCopy property: The IncrementalCopy property.
     *
     * @return the incrementalCopy value.
     */
    public Boolean isIncrementalCopy() {
        return this.incrementalCopy;
    }

    /**
     * Set the incrementalCopy property: The IncrementalCopy property.
     *
     * @param incrementalCopy the incrementalCopy value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setIncrementalCopy(Boolean incrementalCopy) {
        this.incrementalCopy = incrementalCopy;
        return this;
    }

    /**
     * Get the destinationSnapshot property: The DestinationSnapshot property.
     *
     * @return the destinationSnapshot value.
     */
    public String getDestinationSnapshot() {
        return this.destinationSnapshot;
    }

    /**
     * Set the destinationSnapshot property: The DestinationSnapshot property.
     *
     * @param destinationSnapshot the destinationSnapshot value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setDestinationSnapshot(String destinationSnapshot) {
        this.destinationSnapshot = destinationSnapshot;
        return this;
    }

    /**
     * Get the deletedTime property: The DeletedTime property.
     *
     * @return the deletedTime value.
     */
    public OffsetDateTime getDeletedTime() {
        if (this.deletedTime == null) {
            return null;
        }
        return this.deletedTime.getDateTime();
    }

    /**
     * Set the deletedTime property: The DeletedTime property.
     *
     * @param deletedTime the deletedTime value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setDeletedTime(OffsetDateTime deletedTime) {
        if (deletedTime == null) {
            this.deletedTime = null;
        } else {
            this.deletedTime = new DateTimeRfc1123(deletedTime);
        }
        return this;
    }

    /**
     * Get the remainingRetentionDays property: The RemainingRetentionDays property.
     *
     * @return the remainingRetentionDays value.
     */
    public Integer getRemainingRetentionDays() {
        return this.remainingRetentionDays;
    }

    /**
     * Set the remainingRetentionDays property: The RemainingRetentionDays property.
     *
     * @param remainingRetentionDays the remainingRetentionDays value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setRemainingRetentionDays(Integer remainingRetentionDays) {
        this.remainingRetentionDays = remainingRetentionDays;
        return this;
    }

    /**
     * Get the accessTier property: The AccessTier property.
     *
     * @return the accessTier value.
     */
    public AccessTier getAccessTier() {
        return this.accessTier;
    }

    /**
     * Set the accessTier property: The AccessTier property.
     *
     * @param accessTier the accessTier value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setAccessTier(AccessTier accessTier) {
        this.accessTier = accessTier;
        return this;
    }

    /**
     * Get the accessTierInferred property: The AccessTierInferred property.
     *
     * @return the accessTierInferred value.
     */
    public Boolean isAccessTierInferred() {
        return this.accessTierInferred;
    }

    /**
     * Set the accessTierInferred property: The AccessTierInferred property.
     *
     * @param accessTierInferred the accessTierInferred value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setAccessTierInferred(Boolean accessTierInferred) {
        this.accessTierInferred = accessTierInferred;
        return this;
    }

    /**
     * Get the archiveStatus property: The ArchiveStatus property.
     *
     * @return the archiveStatus value.
     */
    public ArchiveStatus getArchiveStatus() {
        return this.archiveStatus;
    }

    /**
     * Set the archiveStatus property: The ArchiveStatus property.
     *
     * @param archiveStatus the archiveStatus value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setArchiveStatus(ArchiveStatus archiveStatus) {
        this.archiveStatus = archiveStatus;
        return this;
    }

    /**
     * Get the customerProvidedKeySha256 property: The CustomerProvidedKeySha256 property.
     *
     * @return the customerProvidedKeySha256 value.
     */
    public String getCustomerProvidedKeySha256() {
        return this.customerProvidedKeySha256;
    }

    /**
     * Set the customerProvidedKeySha256 property: The CustomerProvidedKeySha256 property.
     *
     * @param customerProvidedKeySha256 the customerProvidedKeySha256 value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setCustomerProvidedKeySha256(String customerProvidedKeySha256) {
        this.customerProvidedKeySha256 = customerProvidedKeySha256;
        return this;
    }

    /**
     * Get the encryptionScope property: The name of the encryption scope under which the blob is encrypted.
     *
     * @return the encryptionScope value.
     */
    public String getEncryptionScope() {
        return this.encryptionScope;
    }

    /**
     * Set the encryptionScope property: The name of the encryption scope under which the blob is encrypted.
     *
     * @param encryptionScope the encryptionScope value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setEncryptionScope(String encryptionScope) {
        this.encryptionScope = encryptionScope;
        return this;
    }

    /**
     * Get the accessTierChangeTime property: The AccessTierChangeTime property.
     *
     * @return the accessTierChangeTime value.
     */
    public OffsetDateTime getAccessTierChangeTime() {
        if (this.accessTierChangeTime == null) {
            return null;
        }
        return this.accessTierChangeTime.getDateTime();
    }

    /**
     * Set the accessTierChangeTime property: The AccessTierChangeTime property.
     *
     * @param accessTierChangeTime the accessTierChangeTime value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setAccessTierChangeTime(OffsetDateTime accessTierChangeTime) {
        if (accessTierChangeTime == null) {
            this.accessTierChangeTime = null;
        } else {
            this.accessTierChangeTime = new DateTimeRfc1123(accessTierChangeTime);
        }
        return this;
    }

    /**
     * Get the tagCount property: The TagCount property.
     *
     * @return the tagCount value.
     */
    public Integer getTagCount() {
        return this.tagCount;
    }

    /**
     * Set the tagCount property: The TagCount property.
     *
     * @param tagCount the tagCount value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setTagCount(Integer tagCount) {
        this.tagCount = tagCount;
        return this;
    }

    /**
     * Get the expiresOn property: The Expiry-Time property.
     *
     * @return the expiresOn value.
     */
    public OffsetDateTime getExpiresOn() {
        if (this.expiresOn == null) {
            return null;
        }
        return this.expiresOn.getDateTime();
    }

    /**
     * Set the expiresOn property: The Expiry-Time property.
     *
     * @param expiresOn the expiresOn value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setExpiresOn(OffsetDateTime expiresOn) {
        if (expiresOn == null) {
            this.expiresOn = null;
        } else {
            this.expiresOn = new DateTimeRfc1123(expiresOn);
        }
        return this;
    }

    /**
     * Get the isSealed property: The Sealed property.
     *
     * @return the isSealed value.
     */
    public Boolean isSealed() {
        return this.isSealed;
    }

    /**
     * Set the isSealed property: The Sealed property.
     *
     * @param isSealed the isSealed value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setIsSealed(Boolean isSealed) {
        this.isSealed = isSealed;
        return this;
    }

    /**
     * Get the rehydratePriority property: If an object is in rehydrate pending state then this header is returned with
     * priority of rehydrate. Valid values are High and Standard.
     *
     * @return the rehydratePriority value.
     */
    public RehydratePriority getRehydratePriority() {
        return this.rehydratePriority;
    }

    /**
     * Set the rehydratePriority property: If an object is in rehydrate pending state then this header is returned with
     * priority of rehydrate. Valid values are High and Standard.
     *
     * @param rehydratePriority the rehydratePriority value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setRehydratePriority(RehydratePriority rehydratePriority) {
        this.rehydratePriority = rehydratePriority;
        return this;
    }

    /**
     * Get the lastAccessedOn property: The LastAccessTime property.
     *
     * @return the lastAccessedOn value.
     */
    public OffsetDateTime getLastAccessedOn() {
        if (this.lastAccessedOn == null) {
            return null;
        }
        return this.lastAccessedOn.getDateTime();
    }

    /**
     * Set the lastAccessedOn property: The LastAccessTime property.
     *
     * @param lastAccessedOn the lastAccessedOn value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setLastAccessedOn(OffsetDateTime lastAccessedOn) {
        if (lastAccessedOn == null) {
            this.lastAccessedOn = null;
        } else {
            this.lastAccessedOn = new DateTimeRfc1123(lastAccessedOn);
        }
        return this;
    }

    /**
     * Get the immutabilityPolicyExpiresOn property: The ImmutabilityPolicyUntilDate property.
     *
     * @return the immutabilityPolicyExpiresOn value.
     */
    public OffsetDateTime getImmutabilityPolicyExpiresOn() {
        if (this.immutabilityPolicyExpiresOn == null) {
            return null;
        }
        return this.immutabilityPolicyExpiresOn.getDateTime();
    }

    /**
     * Set the immutabilityPolicyExpiresOn property: The ImmutabilityPolicyUntilDate property.
     *
     * @param immutabilityPolicyExpiresOn the immutabilityPolicyExpiresOn value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setImmutabilityPolicyExpiresOn(OffsetDateTime immutabilityPolicyExpiresOn) {
        if (immutabilityPolicyExpiresOn == null) {
            this.immutabilityPolicyExpiresOn = null;
        } else {
            this.immutabilityPolicyExpiresOn = new DateTimeRfc1123(immutabilityPolicyExpiresOn);
        }
        return this;
    }

    /**
     * Get the immutabilityPolicyMode property: The ImmutabilityPolicyMode property.
     *
     * @return the immutabilityPolicyMode value.
     */
    public BlobImmutabilityPolicyMode getImmutabilityPolicyMode() {
        return this.immutabilityPolicyMode;
    }

    /**
     * Set the immutabilityPolicyMode property: The ImmutabilityPolicyMode property.
     *
     * @param immutabilityPolicyMode the immutabilityPolicyMode value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setImmutabilityPolicyMode(BlobImmutabilityPolicyMode immutabilityPolicyMode) {
        this.immutabilityPolicyMode = immutabilityPolicyMode;
        return this;
    }

    /**
     * Get the legalHold property: The LegalHold property.
     *
     * @return the legalHold value.
     */
    public Boolean isLegalHold() {
        return this.legalHold;
    }

    /**
     * Set the legalHold property: The LegalHold property.
     *
     * @param legalHold the legalHold value to set.
     * @return the BlobItemPropertiesInternal object itself.
     */
    public BlobItemPropertiesInternal setLegalHold(Boolean legalHold) {
        this.legalHold = legalHold;
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        xmlWriter.writeStartElement(getRootElementName(rootElementName, "Properties"));
        return xmlWriter.writeEndElement();
    }

    public static BlobItemPropertiesInternal fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    public static BlobItemPropertiesInternal fromXml(XmlReader xmlReader, String rootElementName)
        throws XMLStreamException {
        return xmlReader.readObject(getRootElementName(rootElementName, "Properties"), reader -> {
            BlobItemPropertiesInternal deserialized = new BlobItemPropertiesInternal();

            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = reader.getElementName().getLocalPart();

                if ("Creation-Time".equals(elementName)) {
                    deserialized.creationTime = reader.getNullableElement(DateTimeRfc1123::fromString);
                } else if ("Last-Modified".equals(elementName)) {
                    deserialized.lastModified = reader.getNullableElement(DateTimeRfc1123::fromString);
                } else if ("Etag".equals(elementName)) {
                    deserialized.eTag = reader.getStringElement();
                } else if ("Content-Length".equals(elementName)) {
                    deserialized.contentLength = reader.getNullableElement(Long::parseLong);
                } else if ("Content-Type".equals(elementName)) {
                    deserialized.contentType = reader.getStringElement();
                } else if ("Content-Encoding".equals(elementName)) {
                    deserialized.contentEncoding = reader.getStringElement();
                } else if ("Content-Language".equals(elementName)) {
                    deserialized.contentLanguage = reader.getStringElement();
                } else if ("Content-MD5".equals(elementName)) {
                    deserialized.contentMd5 = reader.getBinaryElement();
                } else if ("Content-Disposition".equals(elementName)) {
                    deserialized.contentDisposition = reader.getStringElement();
                } else if ("Cache-Control".equals(elementName)) {
                    deserialized.cacheControl = reader.getStringElement();
                } else if ("x-ms-blob-sequence-number".equals(elementName)) {
                    deserialized.blobSequenceNumber = reader.getNullableElement(Long::parseLong);
                } else if ("BlobType".equals(elementName)) {
                    deserialized.blobType = reader.getNullableElement(BlobType::fromString);
                } else if ("LeaseStatus".equals(elementName)) {
                    deserialized.leaseStatus = reader.getNullableElement(LeaseStatusType::fromString);
                } else if ("LeaseState".equals(elementName)) {
                    deserialized.leaseState = reader.getNullableElement(LeaseStateType::fromString);
                } else if ("LeaseDuration".equals(elementName)) {
                    deserialized.leaseDuration = reader.getNullableElement(LeaseDurationType::fromString);
                } else if ("CopyId".equals(elementName)) {
                    deserialized.copyId = reader.getStringElement();
                } else if ("CopyStatus".equals(elementName)) {
                    deserialized.copyStatus = reader.getNullableElement(CopyStatusType::fromString);
                } else if ("CopySource".equals(elementName)) {
                    deserialized.copySource = reader.getStringElement();
                } else if ("CopyProgress".equals(elementName)) {
                    deserialized.copyProgress = reader.getStringElement();
                } else if ("CopyCompletionTime".equals(elementName)) {
                    deserialized.copyCompletionTime = reader.getNullableElement(DateTimeRfc1123::fromString);
                } else if ("CopyStatusDescription".equals(elementName)) {
                    deserialized.copyStatusDescription = reader.getStringElement();
                } else if ("ServerEncrypted".equals(elementName)) {
                    deserialized.serverEncrypted = reader.getNullableElement(Boolean::parseBoolean);
                } else if ("IncrementalCopy".equals(elementName)) {
                    deserialized.incrementalCopy = reader.getNullableElement(Boolean::parseBoolean);
                } else if ("DestinationSnapshot".equals(elementName)) {
                    deserialized.destinationSnapshot = reader.getStringElement();
                } else if ("DeletedTime".equals(elementName)) {
                    deserialized.deletedTime = reader.getNullableElement(DateTimeRfc1123::fromString);
                } else if ("RemainingRetentionDays".equals(elementName)) {
                    deserialized.remainingRetentionDays = reader.getNullableElement(Integer::parseInt);
                } else if ("AccessTier".equals(elementName)) {
                    deserialized.accessTier = reader.getNullableElement(AccessTier::fromString);
                } else if ("AccessTierInferred".equals(elementName)) {
                    deserialized.accessTierInferred = reader.getNullableElement(Boolean::parseBoolean);
                } else if ("ArchiveStatus".equals(elementName)) {
                    deserialized.archiveStatus = reader.getNullableElement(ArchiveStatus::fromString);
                } else if ("CustomerProvidedKeySha256".equals(elementName)) {
                    deserialized.customerProvidedKeySha256 = reader.getStringElement();
                } else if ("EncryptionScope".equals(elementName)) {
                    deserialized.encryptionScope = reader.getStringElement();
                } else if ("AccessTierChangeTime".equals(elementName)) {
                    deserialized.accessTierChangeTime = reader.getNullableElement(DateTimeRfc1123::fromString);
                } else if ("TagCount".equals(elementName)) {
                    deserialized.tagCount = reader.getNullableElement(Integer::parseInt);
                } else if ("Expiry-Time".equals(elementName)) {
                    deserialized.expiresOn = reader.getNullableElement(DateTimeRfc1123::fromString);
                } else if ("Sealed".equals(elementName)) {
                    deserialized.isSealed = reader.getNullableElement(Boolean::parseBoolean);
                } else if ("RehydratePriority".equals(elementName)) {
                    deserialized.rehydratePriority = reader.getNullableElement(RehydratePriority::fromString);
                } else if ("LastAccessTime".equals(elementName)) {
                    deserialized.lastAccessedOn = reader.getNullableElement(DateTimeRfc1123::fromString);
                } else if ("ImmutabilityPolicyUntilDate".equals(elementName)) {
                    deserialized.immutabilityPolicyExpiresOn = reader.getNullableElement(DateTimeRfc1123::fromString);
                } else if ("ImmutabilityPolicyMode".equals(elementName)) {
                    deserialized.immutabilityPolicyMode
                        = reader.getNullableElement(BlobImmutabilityPolicyMode::fromString);
                } else if ("LegalHold".equals(elementName)) {
                    deserialized.legalHold = reader.getNullableElement(Boolean::parseBoolean);
                } else {
                    reader.nextElement();
                }
            }

            return deserialized;
        });
    }
}
