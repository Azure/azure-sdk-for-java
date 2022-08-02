// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

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

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) {
        return null;
    }

    public static BlobItemPropertiesInternal fromXml(XmlReader xmlReader) {
        return xmlReader.readObject("Properties", reader -> {
            BlobItemPropertiesInternal deserialized = new BlobItemPropertiesInternal();

            boolean lastModifiedFound = false;
            boolean eTagFound = false;

            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = reader.getElementName().getLocalPart();

                if ("Creation-Time".equals(elementName)) {
                    deserialized.creationTime = reader.getElementNullableValue(DateTimeRfc1123::fromString);
                } else if ("Last-Modified".equals(elementName)) {
                    deserialized.lastModified = reader.getElementNullableValue(DateTimeRfc1123::fromString);
                    lastModifiedFound = true;
                } else if ("ETag".equals(elementName)) {
                    deserialized.eTag = reader.getElementStringValue();
                    eTagFound = true;
                } else if ("Content-Length".equals(elementName)) {
                    deserialized.contentLength = reader.getElementNullableValue(Long::parseLong);
                } else if ("Content-Type".equals(elementName)) {
                    deserialized.contentType = reader.getElementStringValue();
                } else if ("Content-Encoding".equals(elementName)) {
                    deserialized.contentEncoding = reader.getElementStringValue();
                } else if ("Content-Language".equals(elementName)) {
                    deserialized.contentLanguage = reader.getElementStringValue();
                } else if ("Content-MD5".equals(elementName)) {
                    deserialized.contentMd5 = reader.getElementBinaryValue();
                } else if ("Content-Disposition".equals(elementName)) {
                    deserialized.contentDisposition = reader.getElementStringValue();
                } else if ("Cache-Control".equals(elementName)) {
                    deserialized.cacheControl = reader.getElementStringValue();
                } else if ("x-ms-blob-sequence-number".equals(elementName)) {
                    deserialized.blobSequenceNumber = reader.getElementNullableValue(Long::parseLong);
                } else if ("BlobType".equals(elementName)) {
                    deserialized.blobType = reader.getElementNullableValue(BlobType::fromString);
                } else if ("LeaseStatus".equals(elementName)) {
                    deserialized.leaseStatus = reader.getElementNullableValue(LeaseStatusType::fromString);
                } else if ("LeaseState".equals(elementName)) {
                    deserialized.leaseState = reader.getElementNullableValue(LeaseStateType::fromString);
                } else if ("LeaseDuration".equals(elementName)) {
                    deserialized.leaseDuration = reader.getElementNullableValue(LeaseDurationType::fromString);
                } else if ("CopyId".equals(elementName)) {
                    deserialized.copyId = reader.getElementStringValue();
                } else if ("CopyStatus".equals(elementName)) {
                    deserialized.copyStatus = reader.getElementNullableValue(CopyStatusType::fromString);
                } else if ("CopySource".equals(elementName)) {
                    deserialized.copySource = reader.getElementStringValue();
                } else if ("CopyProgress".equals(elementName)) {
                    deserialized.copyProgress = reader.getElementStringValue();
                } else if ("CopyCompletionTime".equals(elementName)) {
                    deserialized.copyCompletionTime = reader.getElementNullableValue(DateTimeRfc1123::fromString);
                } else if ("CopyStatusDescription".equals(elementName)) {
                    deserialized.copyStatusDescription = reader.getElementStringValue();
                } else if ("ServerEncrypted".equals(elementName)) {
                    deserialized.serverEncrypted = reader.getElementNullableValue(Boolean::parseBoolean);
                } else if ("IncrementalCopy".equals(elementName)) {
                    deserialized.incrementalCopy = reader.getElementNullableValue(Boolean::parseBoolean);
                } else if ("DestinationSnapshot".equals(elementName)) {
                    deserialized.destinationSnapshot = reader.getElementStringValue();
                } else if ("DeletedTime".equals(elementName)) {
                    deserialized.deletedTime = reader.getElementNullableValue(DateTimeRfc1123::fromString);
                } else if ("RemainingRetentionDays".equals(elementName)) {
                    deserialized.remainingRetentionDays = reader.getElementNullableValue(Integer::parseInt);
                } else if ("AccessTier".equals(elementName)) {
                    deserialized.accessTier = reader.getElementNullableValue(AccessTier::fromString);
                } else if ("AccessTierInferred".equals(elementName)) {
                    deserialized.accessTierInferred = reader.getElementNullableValue(Boolean::parseBoolean);
                } else if ("ArchiveStatus".equals(elementName)) {
                    deserialized.archiveStatus = reader.getElementNullableValue(ArchiveStatus::fromString);
                } else if ("CustomerProvidedKeySha256".equals(elementName)) {
                    deserialized.customerProvidedKeySha256 = reader.getElementStringValue();
                } else if ("EncryptionScope".equals(elementName)) {
                    deserialized.encryptionScope = reader.getElementStringValue();
                } else if ("AccessTierChangeTime".equals(elementName)) {
                    deserialized.accessTierChangeTime = reader.getElementNullableValue(DateTimeRfc1123::fromString);
                } else if ("TagCount".equals(elementName)) {
                    deserialized.tagCount = reader.getElementNullableValue(Integer::parseInt);
                } else if ("Expiry-Time".equals(elementName)) {
                    deserialized.expiresOn = reader.getElementNullableValue(DateTimeRfc1123::fromString);
                } else if ("Sealed".equals(elementName)) {
                    deserialized.isSealed = reader.getElementNullableValue(Boolean::parseBoolean);
                } else if ("RehydratePriority".equals(elementName)) {
                    deserialized.rehydratePriority = reader.getElementNullableValue(RehydratePriority::fromString);
                } else if ("LastAccessTime".equals(elementName)) {
                    deserialized.lastAccessedOn = reader.getElementNullableValue(DateTimeRfc1123::fromString);
                } else if ("ImmutabilityPolicyUntilDate".equals(elementName)) {
                    deserialized.immutabilityPolicyExpiresOn =
                        reader.getElementNullableValue(DateTimeRfc1123::fromString);
                } else if ("ImmutabilityPolicyMode".equals(elementName)) {
                    deserialized.immutabilityPolicyMode =
                        reader.getElementNullableValue(BlobImmutabilityPolicyMode::fromString);
                } else if ("LegalHold".equals(elementName)) {
                    deserialized.legalHold = reader.getElementNullableValue(Boolean::parseBoolean);
                }
            }

            if (lastModifiedFound && eTagFound) {
                return deserialized;
            }

            throw new IllegalStateException("Missing required tags.");
        });
    }
}
