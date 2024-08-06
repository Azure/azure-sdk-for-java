// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.implementation.accesshelpers.BlobDownloadHeadersConstructorProxy;
import com.azure.storage.blob.implementation.models.BlobsDownloadHeaders;
import com.azure.storage.blob.implementation.util.ModelHelper;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines headers for Download operation.
 */
@Fluent
public final class BlobDownloadHeaders {
    private final BlobsDownloadHeaders internalHeaders;

    static {
        BlobDownloadHeadersConstructorProxy.setAccessor(BlobDownloadHeaders::new);
    }

    private BlobDownloadHeaders(BlobsDownloadHeaders internalHeaders) {
        this.internalHeaders = internalHeaders;
    }

    /**
     * Constructs a new instance of {@link BlobDownloadHeaders}.
     */
    public BlobDownloadHeaders() {
        // Added to maintain backwards compatibility as the private constructor removes the implicit no args
        // constructor.
        this.internalHeaders = new BlobsDownloadHeaders(new HttpHeaders());
    }

    /*
     * The errorCode property.
     */
    private String errorCode;

    /**
     * Get the lastModified property: Returns the date and time the container was last modified. Any operation that
     * modifies the blob, including an update of the blob's metadata or properties, changes the last-modified time of
     * the blob.
     *
     * @return the lastModified value.
     */
    public OffsetDateTime getLastModified() {
        return internalHeaders.getLastModified();
    }

    /**
     * Set the lastModified property: Returns the date and time the container was last modified. Any operation that
     * modifies the blob, including an update of the blob's metadata or properties, changes the last-modified time of
     * the blob.
     *
     * @param lastModified the lastModified value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setLastModified(OffsetDateTime lastModified) {
        internalHeaders.setLastModified(lastModified);
        return this;
    }

    /**
     * Get the metadata property: The metadata property.
     *
     * @return the metadata value.
     */
    public Map<String, String> getMetadata() {
        return internalHeaders.getXMsMeta();
    }

    /**
     * Set the metadata property: The metadata property.
     *
     * @param metadata the metadata value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setMetadata(Map<String, String> metadata) {
        internalHeaders.setXMsMeta(metadata);
        return this;
    }

    /**
     * Get the objectReplicationDestinationPolicyId property: Optional. Only valid when Object Replication is enabled
     * for the storage container and on the destination blob of the replication.
     *
     * @return the objectReplicationDestinationPolicyId value.
     */
    public String getObjectReplicationDestinationPolicyId() {
        return internalHeaders.getXMsOrPolicyId();
    }

    /**
     * Set the objectReplicationDestinationPolicyId property: Optional. Only valid when Object Replication is enabled
     * for the storage container and on the destination blob of the replication.
     *
     * @param objectReplicationDestinationPolicyId the objectReplicationDestinationPolicyId value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setObjectReplicationDestinationPolicyId(String objectReplicationDestinationPolicyId) {
        internalHeaders.setXMsOrPolicyId(objectReplicationDestinationPolicyId);
        return this;
    }

    /**
     * Get the objectReplicationSourcePolicies property: The objectReplicationSourcePolicies property.
     *
     * @return the objectReplicationSourcePolicies value.
     */
    public List<ObjectReplicationPolicy> getObjectReplicationSourcePolicies() {
        return Collections.unmodifiableList(ModelHelper.getObjectReplicationSourcePolicies(internalHeaders.getXMsOr()));
    }

    /**
     * Set the objectReplicationSourcePolicies property: The objectReplicationSourcePolicies property.
     *
     * @param objectReplicationSourcePolicies the objectReplicationSourcePolicies value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setObjectReplicationSourcePolicies(
        List<ObjectReplicationPolicy> objectReplicationSourcePolicies) {
        Map<String, String> xMsOr = new HashMap<>();

        if (!CoreUtils.isNullOrEmpty(objectReplicationSourcePolicies)) {
            for (ObjectReplicationPolicy policy : objectReplicationSourcePolicies) {
                String policyId = policy.getPolicyId();
                for (ObjectReplicationRule rule : policy.getRules()) {
                    xMsOr.put(policyId + "_" + rule.getRuleId(), rule.getStatus().toString());
                }
            }
        }

        internalHeaders.setXMsOr(xMsOr);
        return this;
    }

    /**
     * Get the contentLength property: The number of bytes present in the response body.
     *
     * @return the contentLength value.
     */
    public Long getContentLength() {
        return internalHeaders.getContentLength();
    }

    /**
     * Set the contentLength property: The number of bytes present in the response body.
     *
     * @param contentLength the contentLength value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setContentLength(Long contentLength) {
        internalHeaders.setContentLength(contentLength);
        return this;
    }

    /**
     * Get the contentType property: The media type of the body of the response. For Download Blob this is
     * 'application/octet-stream'.
     *
     * @return the contentType value.
     */
    public String getContentType() {
        return internalHeaders.getContentType();
    }

    /**
     * Set the contentType property: The media type of the body of the response. For Download Blob this is
     * 'application/octet-stream'.
     *
     * @param contentType the contentType value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setContentType(String contentType) {
        internalHeaders.setContentType(contentType);
        return this;
    }

    /**
     * Get the contentRange property: Indicates the range of bytes returned in the event that the client requested a
     * subset of the blob by setting the 'Range' request header.
     *
     * @return the contentRange value.
     */
    public String getContentRange() {
        return internalHeaders.getContentRange();
    }

    /**
     * Set the contentRange property: Indicates the range of bytes returned in the event that the client requested a
     * subset of the blob by setting the 'Range' request header.
     *
     * @param contentRange the contentRange value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setContentRange(String contentRange) {
        internalHeaders.setContentRange(contentRange);
        return this;
    }

    /**
     * Get the eTag property: The ETag contains a value that you can use to perform operations conditionally. If the
     * request version is 2011-08-18 or newer, the ETag value will be in quotes.
     *
     * @return the eTag value.
     */
    public String getETag() {
        return internalHeaders.getETag();
    }

    /**
     * Set the eTag property: The ETag contains a value that you can use to perform operations conditionally. If the
     * request version is 2011-08-18 or newer, the ETag value will be in quotes.
     *
     * @param eTag the eTag value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setETag(String eTag) {
        internalHeaders.setETag(eTag);
        return this;
    }

    /**
     * Get the contentMd5 property: If the blob has an MD5 hash and this operation is to read the full blob, this
     * response header is returned so that the client can check for message content integrity.
     *
     * @return the contentMd5 value.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(internalHeaders.getContentMD5());
    }

    /**
     * Set the contentMd5 property: If the blob has an MD5 hash and this operation is to read the full blob, this
     * response header is returned so that the client can check for message content integrity.
     *
     * @param contentMd5 the contentMd5 value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setContentMd5(byte[] contentMd5) {
        internalHeaders.setContentMD5(CoreUtils.clone(contentMd5));
        return this;
    }

    /**
     * Get the contentEncoding property: This header returns the value that was specified for the Content-Encoding
     * request header.
     *
     * @return the contentEncoding value.
     */
    public String getContentEncoding() {
        return internalHeaders.getContentEncoding();
    }

    /**
     * Set the contentEncoding property: This header returns the value that was specified for the Content-Encoding
     * request header.
     *
     * @param contentEncoding the contentEncoding value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setContentEncoding(String contentEncoding) {
        internalHeaders.setContentEncoding(contentEncoding);
        return this;
    }

    /**
     * Get the cacheControl property: This header is returned if it was previously specified for the blob.
     *
     * @return the cacheControl value.
     */
    public String getCacheControl() {
        return internalHeaders.getCacheControl();
    }

    /**
     * Set the cacheControl property: This header is returned if it was previously specified for the blob.
     *
     * @param cacheControl the cacheControl value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setCacheControl(String cacheControl) {
        internalHeaders.setCacheControl(cacheControl);
        return this;
    }

    /**
     * Get the contentDisposition property: This header returns the value that was specified for the
     * 'x-ms-blob-content-disposition' header. The Content-Disposition response header field conveys additional
     * information about how to process the response payload, and also can be used to attach additional metadata. For
     * example, if set to attachment, it indicates that the user-agent should not display the response, but instead show
     * a Save As dialog with a filename other than the blob name specified.
     *
     * @return the contentDisposition value.
     */
    public String getContentDisposition() {
        return internalHeaders.getContentDisposition();
    }

    /**
     * Set the contentDisposition property: This header returns the value that was specified for the
     * 'x-ms-blob-content-disposition' header. The Content-Disposition response header field conveys additional
     * information about how to process the response payload, and also can be used to attach additional metadata. For
     * example, if set to attachment, it indicates that the user-agent should not display the response, but instead show
     * a Save As dialog with a filename other than the blob name specified.
     *
     * @param contentDisposition the contentDisposition value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setContentDisposition(String contentDisposition) {
        internalHeaders.setContentDisposition(contentDisposition);
        return this;
    }

    /**
     * Get the contentLanguage property: This header returns the value that was specified for the Content-Language
     * request header.
     *
     * @return the contentLanguage value.
     */
    public String getContentLanguage() {
        return internalHeaders.getContentLanguage();
    }

    /**
     * Set the contentLanguage property: This header returns the value that was specified for the Content-Language
     * request header.
     *
     * @param contentLanguage the contentLanguage value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setContentLanguage(String contentLanguage) {
        internalHeaders.setContentLanguage(contentLanguage);
        return this;
    }

    /**
     * Get the blobSequenceNumber property: The current sequence number for a page blob. This header is not returned for
     * block blobs or append blobs.
     *
     * @return the blobSequenceNumber value.
     */
    public Long getBlobSequenceNumber() {
        return internalHeaders.getXMsBlobSequenceNumber();
    }

    /**
     * Set the blobSequenceNumber property: The current sequence number for a page blob. This header is not returned for
     * block blobs or append blobs.
     *
     * @param blobSequenceNumber the blobSequenceNumber value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setBlobSequenceNumber(Long blobSequenceNumber) {
        internalHeaders.setXMsBlobSequenceNumber(blobSequenceNumber);
        return this;
    }

    /**
     * Get the blobType property: The blob's type. Possible values include: 'BlockBlob', 'PageBlob', 'AppendBlob'.
     *
     * @return the blobType value.
     */
    public BlobType getBlobType() {
        return internalHeaders.getXMsBlobType();
    }

    /**
     * Set the blobType property: The blob's type. Possible values include: 'BlockBlob', 'PageBlob', 'AppendBlob'.
     *
     * @param blobType the blobType value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setBlobType(BlobType blobType) {
        internalHeaders.setXMsBlobType(blobType);
        return this;
    }

    /**
     * Get the copyCompletionTime property: Conclusion time of the last attempted Copy Blob operation where this blob
     * was the destination blob. This value can specify the time of a completed, aborted, or failed copy attempt. This
     * header does not appear if a copy is pending, if this blob has never been the destination in a Copy Blob
     * operation, or if this blob has been modified after a concluded Copy Blob operation using Set Blob Properties, Put
     * Blob, or Put Block List.
     *
     * @return the copyCompletionTime value.
     */
    public OffsetDateTime getCopyCompletionTime() {
        return internalHeaders.getXMsCopyCompletionTime();
    }

    /**
     * Set the copyCompletionTime property: Conclusion time of the last attempted Copy Blob operation where this blob
     * was the destination blob. This value can specify the time of a completed, aborted, or failed copy attempt. This
     * header does not appear if a copy is pending, if this blob has never been the destination in a Copy Blob
     * operation, or if this blob has been modified after a concluded Copy Blob operation using Set Blob Properties, Put
     * Blob, or Put Block List.
     *
     * @param copyCompletionTime the copyCompletionTime value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setCopyCompletionTime(OffsetDateTime copyCompletionTime) {
        internalHeaders.setXMsCopyCompletionTime(copyCompletionTime);
        return this;
    }

    /**
     * Get the copyStatusDescription property: Only appears when x-ms-copy-status is failed or pending. Describes the
     * cause of the last fatal or non-fatal copy operation failure. This header does not appear if this blob has never
     * been the destination in a Copy Blob operation, or if this blob has been modified after a concluded Copy Blob
     * operation using Set Blob Properties, Put Blob, or Put Block List.
     *
     * @return the copyStatusDescription value.
     */
    public String getCopyStatusDescription() {
        return internalHeaders.getXMsCopyStatusDescription();
    }

    /**
     * Set the copyStatusDescription property: Only appears when x-ms-copy-status is failed or pending. Describes the
     * cause of the last fatal or non-fatal copy operation failure. This header does not appear if this blob has never
     * been the destination in a Copy Blob operation, or if this blob has been modified after a concluded Copy Blob
     * operation using Set Blob Properties, Put Blob, or Put Block List.
     *
     * @param copyStatusDescription the copyStatusDescription value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setCopyStatusDescription(String copyStatusDescription) {
        internalHeaders.setXMsCopyStatusDescription(copyStatusDescription);
        return this;
    }

    /**
     * Get the copyId property: String identifier for this copy operation. Use with Get Blob Properties to check the
     * status of this copy operation, or pass to Abort Copy Blob to abort a pending copy.
     *
     * @return the copyId value.
     */
    public String getCopyId() {
        return internalHeaders.getXMsCopyId();
    }

    /**
     * Set the copyId property: String identifier for this copy operation. Use with Get Blob Properties to check the
     * status of this copy operation, or pass to Abort Copy Blob to abort a pending copy.
     *
     * @param copyId the copyId value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setCopyId(String copyId) {
        internalHeaders.setXMsCopyId(copyId);
        return this;
    }

    /**
     * Get the copyProgress property: Contains the number of bytes copied and the total bytes in the source in the last
     * attempted Copy Blob operation where this blob was the destination blob. Can show between 0 and Content-Length
     * bytes copied. This header does not appear if this blob has never been the destination in a Copy Blob operation,
     * or if this blob has been modified after a concluded Copy Blob operation using Set Blob Properties, Put Blob, or
     * Put Block List.
     *
     * @return the copyProgress value.
     */
    public String getCopyProgress() {
        return internalHeaders.getXMsCopyProgress();
    }

    /**
     * Set the copyProgress property: Contains the number of bytes copied and the total bytes in the source in the last
     * attempted Copy Blob operation where this blob was the destination blob. Can show between 0 and Content-Length
     * bytes copied. This header does not appear if this blob has never been the destination in a Copy Blob operation,
     * or if this blob has been modified after a concluded Copy Blob operation using Set Blob Properties, Put Blob, or
     * Put Block List.
     *
     * @param copyProgress the copyProgress value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setCopyProgress(String copyProgress) {
        internalHeaders.setXMsCopyProgress(copyProgress);
        return this;
    }

    /**
     * Get the copySource property: URL up to 2 KB in length that specifies the source blob or file used in the last
     * attempted Copy Blob operation where this blob was the destination blob. This header does not appear if this blob
     * has never been the destination in a Copy Blob operation, or if this blob has been modified after a concluded Copy
     * Blob operation using Set Blob Properties, Put Blob, or Put Block List.
     *
     * @return the copySource value.
     */
    public String getCopySource() {
        return internalHeaders.getXMsCopySource();
    }

    /**
     * Set the copySource property: URL up to 2 KB in length that specifies the source blob or file used in the last
     * attempted Copy Blob operation where this blob was the destination blob. This header does not appear if this blob
     * has never been the destination in a Copy Blob operation, or if this blob has been modified after a concluded Copy
     * Blob operation using Set Blob Properties, Put Blob, or Put Block List.
     *
     * @param copySource the copySource value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setCopySource(String copySource) {
        internalHeaders.setXMsCopySource(copySource);
        return this;
    }

    /**
     * Get the copyStatus property: State of the copy operation identified by x-ms-copy-id. Possible values include:
     * 'pending', 'success', 'aborted', 'failed'.
     *
     * @return the copyStatus value.
     */
    public CopyStatusType getCopyStatus() {
        return internalHeaders.getXMsCopyStatus();
    }

    /**
     * Set the copyStatus property: State of the copy operation identified by x-ms-copy-id. Possible values include:
     * 'pending', 'success', 'aborted', 'failed'.
     *
     * @param copyStatus the copyStatus value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setCopyStatus(CopyStatusType copyStatus) {
        internalHeaders.setXMsCopyStatus(copyStatus);
        return this;
    }

    /**
     * Get the leaseDuration property: When a blob is leased, specifies whether the lease is of infinite or fixed
     * duration. Possible values include: 'infinite', 'fixed'.
     *
     * @return the leaseDuration value.
     */
    public LeaseDurationType getLeaseDuration() {
        return internalHeaders.getXMsLeaseDuration();
    }

    /**
     * Set the leaseDuration property: When a blob is leased, specifies whether the lease is of infinite or fixed
     * duration. Possible values include: 'infinite', 'fixed'.
     *
     * @param leaseDuration the leaseDuration value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setLeaseDuration(LeaseDurationType leaseDuration) {
        internalHeaders.setXMsLeaseDuration(leaseDuration);
        return this;
    }

    /**
     * Get the leaseState property: Lease state of the blob. Possible values include: 'available', 'leased', 'expired',
     * 'breaking', 'broken'.
     *
     * @return the leaseState value.
     */
    public LeaseStateType getLeaseState() {
        return internalHeaders.getXMsLeaseState();
    }

    /**
     * Set the leaseState property: Lease state of the blob. Possible values include: 'available', 'leased', 'expired',
     * 'breaking', 'broken'.
     *
     * @param leaseState the leaseState value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setLeaseState(LeaseStateType leaseState) {
        internalHeaders.setXMsLeaseState(leaseState);
        return this;
    }

    /**
     * Get the leaseStatus property: The current lease status of the blob. Possible values include: 'locked',
     * 'unlocked'.
     *
     * @return the leaseStatus value.
     */
    public LeaseStatusType getLeaseStatus() {
        return internalHeaders.getXMsLeaseStatus();
    }

    /**
     * Set the leaseStatus property: The current lease status of the blob. Possible values include: 'locked',
     * 'unlocked'.
     *
     * @param leaseStatus the leaseStatus value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setLeaseStatus(LeaseStatusType leaseStatus) {
        internalHeaders.setXMsLeaseStatus(leaseStatus);
        return this;
    }

    /**
     * Get the clientRequestId property: If a client request id header is sent in the request, this header will be
     * present in the response with the same value.
     *
     * @return the clientRequestId value.
     */
    public String getClientRequestId() {
        return internalHeaders.getXMsClientRequestId();
    }

    /**
     * Set the clientRequestId property: If a client request id header is sent in the request, this header will be
     * present in the response with the same value.
     *
     * @param clientRequestId the clientRequestId value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setClientRequestId(String clientRequestId) {
        internalHeaders.setXMsClientRequestId(clientRequestId);
        return this;
    }

    /**
     * Get the requestId property: This header uniquely identifies the request that was made and can be used for
     * troubleshooting the request.
     *
     * @return the requestId value.
     */
    public String getRequestId() {
        return internalHeaders.getXMsRequestId();
    }

    /**
     * Set the requestId property: This header uniquely identifies the request that was made and can be used for
     * troubleshooting the request.
     *
     * @param requestId the requestId value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setRequestId(String requestId) {
        internalHeaders.setXMsRequestId(requestId);
        return this;
    }

    /**
     * Get the version property: Indicates the version of the Blob service used to execute the request. This header is
     * returned for requests made against version 2009-09-19 and above.
     *
     * @return the version value.
     */
    public String getVersion() {
        return internalHeaders.getXMsVersion();
    }

    /**
     * Set the version property: Indicates the version of the Blob service used to execute the request. This header is
     * returned for requests made against version 2009-09-19 and above.
     *
     * @param version the version value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setVersion(String version) {
        internalHeaders.setXMsVersion(version);
        return this;
    }

    /**
     * Get the versionId property: A DateTime value returned by the service that uniquely identifies the blob. The value
     * of this header indicates the blob version, and may be used in subsequent requests to access this version of the
     * blob.
     *
     * @return the versionId value.
     */
    public String getVersionId() {
        return internalHeaders.getXMsVersionId();
    }

    /**
     * Set the versionId property: A DateTime value returned by the service that uniquely identifies the blob. The value
     * of this header indicates the blob version, and may be used in subsequent requests to access this version of the
     * blob.
     *
     * @param versionId the versionId value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setVersionId(String versionId) {
        internalHeaders.setXMsVersionId(versionId);
        return this;
    }

    /**
     * Get the acceptRanges property: Indicates that the service supports requests for partial blob content.
     *
     * @return the acceptRanges value.
     */
    public String getAcceptRanges() {
        return internalHeaders.getAcceptRanges();
    }

    /**
     * Set the acceptRanges property: Indicates that the service supports requests for partial blob content.
     *
     * @param acceptRanges the acceptRanges value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setAcceptRanges(String acceptRanges) {
        internalHeaders.setAcceptRanges(acceptRanges);
        return this;
    }

    /**
     * Get the dateProperty property: UTC date/time value generated by the service that indicates the time at which the
     * response was initiated.
     *
     * @return the dateProperty value.
     */
    public OffsetDateTime getDateProperty() {
        return internalHeaders.getDate();
    }

    /**
     * Set the dateProperty property: UTC date/time value generated by the service that indicates the time at which the
     * response was initiated.
     *
     * @param dateProperty the dateProperty value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setDateProperty(OffsetDateTime dateProperty) {
        internalHeaders.setDate(dateProperty);
        return this;
    }

    /**
     * Get the blobCommittedBlockCount property: The number of committed blocks present in the blob. This header is
     * returned only for append blobs.
     *
     * @return the blobCommittedBlockCount value.
     */
    public Integer getBlobCommittedBlockCount() {
        return internalHeaders.getXMsBlobCommittedBlockCount();
    }

    /**
     * Set the blobCommittedBlockCount property: The number of committed blocks present in the blob. This header is
     * returned only for append blobs.
     *
     * @param blobCommittedBlockCount the blobCommittedBlockCount value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setBlobCommittedBlockCount(Integer blobCommittedBlockCount) {
        internalHeaders.setXMsBlobCommittedBlockCount(blobCommittedBlockCount);
        return this;
    }

    /**
     * Get the isServerEncrypted property: The value of this header is set to true if the blob data and application
     * metadata are completely encrypted using the specified algorithm. Otherwise, the value is set to false (when the
     * blob is unencrypted, or if only parts of the blob/application metadata are encrypted).
     *
     * @return the isServerEncrypted value.
     */
    public Boolean isServerEncrypted() {
        return internalHeaders.isXMsServerEncrypted();
    }

    /**
     * Set the isServerEncrypted property: The value of this header is set to true if the blob data and application
     * metadata are completely encrypted using the specified algorithm. Otherwise, the value is set to false (when the
     * blob is unencrypted, or if only parts of the blob/application metadata are encrypted).
     *
     * @param isServerEncrypted the isServerEncrypted value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setIsServerEncrypted(Boolean isServerEncrypted) {
        internalHeaders.setXMsServerEncrypted(isServerEncrypted);
        return this;
    }

    /**
     * Get the encryptionKeySha256 property: The SHA-256 hash of the encryption key used to encrypt the blob. This
     * header is only returned when the blob was encrypted with a customer-provided key.
     *
     * @return the encryptionKeySha256 value.
     */
    public String getEncryptionKeySha256() {
        return internalHeaders.getXMsEncryptionKeySha256();
    }

    /**
     * Set the encryptionKeySha256 property: The SHA-256 hash of the encryption key used to encrypt the blob. This
     * header is only returned when the blob was encrypted with a customer-provided key.
     *
     * @param encryptionKeySha256 the encryptionKeySha256 value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setEncryptionKeySha256(String encryptionKeySha256) {
        internalHeaders.setXMsEncryptionKeySha256(encryptionKeySha256);
        return this;
    }

    /**
     * Get the encryptionScope property: Returns the name of the encryption scope used to encrypt the blob contents and
     * application metadata.  Note that the absence of this header implies use of the default account encryption scope.
     *
     * @return the encryptionScope value.
     */
    public String getEncryptionScope() {
        return internalHeaders.getXMsEncryptionScope();
    }

    /**
     * Set the encryptionScope property: Returns the name of the encryption scope used to encrypt the blob contents and
     * application metadata.  Note that the absence of this header implies use of the default account encryption scope.
     *
     * @param encryptionScope the encryptionScope value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setEncryptionScope(String encryptionScope) {
        internalHeaders.setXMsEncryptionScope(encryptionScope);
        return this;
    }

    /**
     * Get the blobContentMD5 property: If the blob has a MD5 hash, and if request contains range header (Range or
     * x-ms-range), this response header is returned with the value of the whole blob's MD5 value. This value may or may
     * not be equal to the value returned in Content-MD5 header, with the latter calculated from the requested range.
     *
     * @return the blobContentMD5 value.
     */
    public byte[] getBlobContentMD5() {
        return CoreUtils.clone(internalHeaders.getXMsBlobContentMd5());
    }

    /**
     * Set the blobContentMD5 property: If the blob has a MD5 hash, and if request contains range header (Range or
     * x-ms-range), this response header is returned with the value of the whole blob's MD5 value. This value may or may
     * not be equal to the value returned in Content-MD5 header, with the latter calculated from the requested range.
     *
     * @param blobContentMD5 the blobContentMD5 value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setBlobContentMD5(byte[] blobContentMD5) {
        internalHeaders.setXMsBlobContentMd5(CoreUtils.clone(blobContentMD5));
        return this;
    }

    /**
     * Get the tagCount property: The number of tags associated with the blob.
     *
     * @return the tagCount value.
     */
    public Long getTagCount() {
        return internalHeaders.getXMsTagCount();
    }

    /**
     * Set the tagCount property: The number of tags associated with the blob.
     *
     * @param tagCount the tagCount value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setTagCount(Long tagCount) {
        internalHeaders.setXMsTagCount(tagCount);
        return this;
    }

    /**
     * Get the contentCrc64 property: If the request is to read a specified range and the x-ms-range-get-content-crc64
     * is set to true, then the request returns a crc64 for the range, as long as the range size is less than or equal
     * to 4 MB. If both x-ms-range-get-content-crc64 &amp; x-ms-range-get-content-md5 is specified in the same request,
     * it will fail with 400(Bad Request).
     *
     * @return the contentCrc64 value.
     */
    public byte[] getContentCrc64() {
        return CoreUtils.clone(internalHeaders.getXMsContentCrc64());
    }

    /**
     * Set the contentCrc64 property: If the request is to read a specified range and the x-ms-range-get-content-crc64
     * is set to true, then the request returns a crc64 for the range, as long as the range size is less than or equal
     * to 4 MB. If both x-ms-range-get-content-crc64 &amp; x-ms-range-get-content-md5 is specified in the same request,
     * it will fail with 400(Bad Request).
     *
     * @param contentCrc64 the contentCrc64 value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setContentCrc64(byte[] contentCrc64) {
        internalHeaders.setXMsContentCrc64(CoreUtils.clone(contentCrc64));
        return this;
    }

    /**
     * Get the errorCode property: The errorCode property.
     *
     * @return the errorCode value.
     */
    public String getErrorCode() {
        return this.errorCode;
    }

    /**
     * Set the errorCode property: The errorCode property.
     *
     * @param errorCode the errorCode value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    /**
     * Get the sealed property: The sealed property.
     *
     * @return Whether the blob is sealed  (marked as read only). This is only applicable for Append blobs.
     */
    public Boolean isSealed() {
        return internalHeaders.isXMsBlobSealed();
    }

    /**
     * Set the sealed property: The sealed property.
     *
     * @param sealed Whether the blob is sealed  (marked as read only). This is only applicable for Append blobs.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setSealed(Boolean sealed) {
        internalHeaders.setXMsBlobSealed(sealed);
        return this;
    }

    /**
     * Get the lastAccessedTime property: The lastAccessedTime property.
     *
     * @return the lastAccessedTime value.
     */
    public OffsetDateTime getLastAccessedTime() {
        return internalHeaders.getXMsLastAccessTime();
    }

    /**
     * Set the lastAccessedTime property: The lastAccessedTime property.
     *
     * @param lastAccessedTime the lastAccessedTime value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setLastAccessedTime(OffsetDateTime lastAccessedTime) {
        internalHeaders.setXMsLastAccessTime(lastAccessedTime);
        return this;
    }

    /**
     * Get the currentVersion property: The x-ms-is-current-version property.
     *
     * @return the currentVersion value.
     */
    public Boolean isCurrentVersion() {
        return internalHeaders.isXMsIsCurrentVersion();
    }

    /**
     * Set the currentVersion property: The x-ms-is-current-version property.
     *
     * @param currentVersion the currentVersion value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setCurrentVersion(Boolean currentVersion) {
        internalHeaders.setXMsIsCurrentVersion(currentVersion);
        return this;
    }

    /**
     * Get the immutabilityPolicy property: The  x-ms-immutability-policy-mode and x-ms-immutability-policy-until-date
     * property.
     *
     * @return the immutabilityPolicy value.
     */
    public BlobImmutabilityPolicy getImmutabilityPolicy() {
        return new BlobImmutabilityPolicy()
            .setPolicyMode(BlobImmutabilityPolicyMode.fromString(internalHeaders.getXMsImmutabilityPolicyMode()))
            .setExpiryTime(internalHeaders.getXMsImmutabilityPolicyUntilDate());
    }

    /**
     * Set the immutabilityPolicy property:  x-ms-immutability-policy-mode and x-ms-immutability-policy-until-date
     * property.
     *
     * @param immutabilityPolicy the immutabilityPolicy value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy) {
        if (immutabilityPolicy == null) {
            internalHeaders.setXMsImmutabilityPolicyMode(null);
            internalHeaders.setXMsImmutabilityPolicyUntilDate(null);
        } else {
            internalHeaders.setXMsImmutabilityPolicyMode(immutabilityPolicy.getPolicyMode().toString());
            internalHeaders.setXMsImmutabilityPolicyUntilDate(immutabilityPolicy.getExpiryTime());
        }
        return this;
    }

    /**
     * Get the hasLegalHold property: The x-ms-legal-hold property.
     *
     * @return the hasLegalHold value.
     */
    public Boolean hasLegalHold() {
        return internalHeaders.isXMsLegalHold();
    }

    /**
     * Set the hasLegalHold property: The x-ms-legal-hold property.
     *
     * @param hasLegalHold the xMsLegalHold value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setHasLegalHold(Boolean hasLegalHold) {
        internalHeaders.setXMsLegalHold(hasLegalHold);
        return this;
    }

    /**
     * Get the xMsCreationTime property: The x-ms-creation-time property.
     *
     * @return the creation time value.
     */
    public OffsetDateTime getCreationTime() {
        return internalHeaders.getXMsCreationTime();
    }

    /**
     * Set the xMsCreationTime property: The x-ms-creation-time property.
     *
     * @param creationTime the xMsCreationTime value to set.
     * @return the BlobDownloadHeaders object itself.
     */
    public BlobDownloadHeaders setCreationTime(OffsetDateTime creationTime) {
        internalHeaders.setXMsCreationTime(creationTime);
        return this;
    }
}
