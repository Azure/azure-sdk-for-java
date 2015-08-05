/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import java.util.Date;

import com.microsoft.azure.storage.AccessCondition;

/**
 * Represents the system properties for a blob.
 */
public final class BlobProperties {

    /**
     * Represents the number of committed blocks on the append blob.
     */
    private Integer appendBlobCommittedBlockCount;
    
    /**
     * Represents the type of the blob.
     */
    private BlobType blobType = BlobType.UNSPECIFIED;

    /**
     * Represents the cache-control value stored for the blob.
     */
    private String cacheControl;

    /**
     * Represents the content-disposition value stored for the blob. If this field has not been set for the blob, the
     * field returns <code>null</code>.
     */
    private String contentDisposition;

    /**
     * Represents the content-encoding value stored for the blob. If this field has not been set for the blob, the field
     * returns <code>null</code>.
     */
    private String contentEncoding;

    /**
     * Represents the content-language value stored for the blob. If this field has not been set for the blob, the field
     * returns <code>null</code>.
     */
    private String contentLanguage;

    /**
     * Represents the content MD5 value stored for the blob.
     */
    private String contentMD5;

    /**
     * Represents the content type value stored for the blob. If this field has not been set for the blob, the field
     * returns <code>null</code>.
     */
    private String contentType;

    /**
     * Represents the state of the most recent or pending copy operation.
     */
    private CopyState copyState;

    /**
     * Represents the blob's ETag value.
     */
    private String etag;

    /**
     * Represents the last-modified time for the blob.
     */
    private Date lastModified;

    /**
     * Represents the blob's lease status.
     */
    private LeaseStatus leaseStatus = LeaseStatus.UNLOCKED;

    /**
     * Represents the blob's lease state.
     */
    private LeaseState leaseState;

    /**
     * Represents the blob's lease duration.
     */
    private LeaseDuration leaseDuration;

    /**
     * Represents the size, in bytes, of the blob.
     */
    private long length;
    
    /**
     * Represents the page blob's current sequence number.
     */
    private Long pageBlobSequenceNumber;
    
    /**
     * Creates an instance of the <code>BlobProperties</code> class.
     */
    public BlobProperties() {
        // No op
    }

    /**
     * Creates an instance of the <code>BlobProperties</code> class by copying values from another instance of the
     * <code>BlobProperties</code> class.
     * 
     * @param other
     *        A <code>BlobProperties</code> object which represents the blob properties to copy.
     */
    public BlobProperties(final BlobProperties other) {
        this.blobType = other.blobType;
        this.contentDisposition = other.contentDisposition;
        this.contentEncoding = other.contentEncoding;
        this.contentLanguage = other.contentLanguage;
        this.contentType = other.contentType;
        this.copyState = other.copyState;
        this.etag = other.etag;
        this.leaseStatus = other.leaseStatus;
        this.leaseState = other.leaseState;
        this.leaseDuration = other.leaseDuration;
        this.length = other.length;
        this.lastModified = other.lastModified;
        this.contentMD5 = other.contentMD5;
        this.cacheControl = other.cacheControl;
        this.pageBlobSequenceNumber = other.pageBlobSequenceNumber;
        this.appendBlobCommittedBlockCount = other.appendBlobCommittedBlockCount;
    }

    /**
     * Creates an instance of the <code>BlobProperties</code> class.
     * 
     * @param type
     *        A <code>BlobType</code> object which represents the blob type.
     */
    public BlobProperties(final BlobType type) {
        this.blobType = type;
    }

    /**
     * If the blob is an append blob, gets the number of committed blocks.
     * 
     * @return A <code>Integer</code> value that represents the number of committed blocks.
     */
    public Integer getAppendBlobCommittedBlockCount() {
        return this.appendBlobCommittedBlockCount;
    }
    
    /**
     * Gets the blob type for the blob.
     * 
     * @return A {@link BlobType} value that represents the blob type.
     */
    public BlobType getBlobType() {
        return this.blobType;
    }

    /**
     * Gets the cache control value for the blob.
     * 
     * @return A <code>String</code> which represents the content cache control value for the blob.
     */
    public String getCacheControl() {
        return this.cacheControl;
    }

    /**
     * Gets the content disposition value for the blob.
     * 
     * @return A <code>String</code> which represents the content disposition, or <code>null</code> if content disposition has not been set
     *         on the blob.
     */
    public String getContentDisposition() {
        return this.contentDisposition;
    }

    /**
     * Gets the content encoding value for the blob.
     * 
     * @return A <code>String</code> which represents the content encoding, or <code>null</code> if content encoding has not been set
     *         on the blob.
     */
    public String getContentEncoding() {
        return this.contentEncoding;
    }

    /**
     * Gets the content language value for the blob.
     * 
     * @return A <code>String</code> which represents the content language, or <code>null</code> if content language has not been set on
     *         the blob.
     */
    public String getContentLanguage() {
        return this.contentLanguage;
    }

    /**
     * Gets the content MD5 value for the blob.
     * 
     * @return A <code>String</code> which represents the content MD5 value.
     */
    public String getContentMD5() {
        return this.contentMD5;
    }

    /**
     * Gets the content type value for the blob.
     * 
     * @return A <code>String</code> which represents the content type, or <code>null</code> if the content type has not be set for the blob.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Gets the blob's copy state.
     * 
     * @return A {@link CopyState} object which represents the copy state of the blob.
     */
    public CopyState getCopyState() {
        return this.copyState;
    }

    /**
     * Gets the ETag value for the blob.
     * <p>
     * The ETag value is a unique identifier that is updated when a write operation is performed against the container.
     * It may be used to perform operations conditionally, providing concurrency control and improved efficiency.
     * <p>
     * The {@link AccessCondition#generateIfMatchCondition(String)} and
     * {@link AccessCondition#generateIfNoneMatchCondition(String)} methods take an ETag value and return an
     * {@link AccessCondition} object that may be specified on the request.
     * 
     * @return A <code>String</code> which represents the ETag value.
     */
    public String getEtag() {
        return this.etag;
    }

    /**
     * Gets the last modified time for the blob.
     * 
     * @return A {@link java.util.Date} object which represents the last modified time. 
     */
    public Date getLastModified() {
        return this.lastModified;
    }

    /**
     * Gets the lease status for the blob.
     * 
     * @return A {@link LeaseStatus} object which represents the lease status. 
     */
    public LeaseStatus getLeaseStatus() {
        return this.leaseStatus;
    }

    /**
     * Gets the lease state for the blob.
     * 
     * @return A {@link LeaseState} object which represents the lease state. 
     */
    public LeaseState getLeaseState() {
        return this.leaseState;
    }

    /**
     * Gets the lease duration for the blob.
     * 
     * @return A {@link LeaseDuration} object which represents the lease duration. 
     */
    public LeaseDuration getLeaseDuration() {
        return this.leaseDuration;
    }

    /**
     * Gets the size, in bytes, of the blob.
     * 
     * @return A <code>long</code> which represents the length of the blob.
     */
    public long getLength() {
        return this.length;
    }
    
    /**
     * If the blob is a page blob, gets the page blob's current sequence number.
     * 
     * @return A <code>Long</code> containing the page blob's current sequence number.
     */
    public Long getPageBlobSequenceNumber() {
        return this.pageBlobSequenceNumber;
    }
    
    /**
     * Sets the cache control value for the blob.
     * 
     * @param cacheControl
     *        A <code>String</code> which specifies the cache control value to set.
     */
    public void setCacheControl(final String cacheControl) {
        this.cacheControl = cacheControl;
    }

    /**
     * Sets the content disposition value for the blob.
     * 
     * @param contentDisposition
     *        A <code>String</code> which specifies the content disposition value to set.
     */
    public void setContentDisposition(final String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    /**
     * Sets the content encoding value for the blob.
     * 
     * @param contentEncoding
     *        A <code>String</code> which specifies the content encoding value to set.
     */
    public void setContentEncoding(final String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     * Sets the content language for the blob.
     * 
     * @param contentLanguage
     *        A <code>String</code> which specifies the content language value to set.
     */
    public void setContentLanguage(final String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    /**
     * Sets the content MD5 value for the blob.
     * 
     * @param contentMD5
     *        A <code>String</code> which specifies the content MD5 value to set.
     */
    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    /**
     * Sets the content type value for the blob.
     * 
     * @param contentType
     *        A <code>String</code> which specifies the content type value to set.
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * If the blob is an append blob, sets the number of committed blocks.
     * 
     * @param appendBlobCommittedBlockCount
     *        A <code>Integer</code> value that represents the number of committed blocks.
     */
    protected void setAppendBlobCommittedBlockCount(final Integer appendBlobCommittedBlockCount) {
        this.appendBlobCommittedBlockCount  = appendBlobCommittedBlockCount;
    }
    
    /**
     * Sets the blob type.
     * 
     * @param blobType
     *        A {@link BlobType} object which specifies the blob type to set.
     */
    protected void setBlobType(final BlobType blobType) {
        this.blobType = blobType;
    }

    /**
     * Sets the copy state value for the blob
     * 
     * @param copyState
     *        A {@link CopyState} object which specifies the copy state value to set.
     */
    protected void setCopyState(final CopyState copyState) {
        this.copyState = copyState;
    }

    /**
     * Sets the ETag value for the blob.
     * 
     * @param etag
     *        A <code>String</code> which specifies the ETag value to set.
     */
    protected void setEtag(final String etag) {
        this.etag = etag;
    }

    /**
     * Sets the last modified time for the blob.
     * 
     * @param lastModified
     *        A {@link java.util.Date} object which specifies the last modified time to set.
     */
    protected void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Sets the lease status for the blob.
     * 
     * @param leaseStatus
     *        A {@link LeaseStatus} object which specifies the lease status value to set.
     */
    protected void setLeaseStatus(final LeaseStatus leaseStatus) {
        this.leaseStatus = leaseStatus;
    }

    /**
     * Sets the lease state for the blob.
     * 
     * @param leaseState
     *        A {@link LeaseState} object which specifies the lease state value to set.
     */
    protected void setLeaseState(final LeaseState leaseState) {
        this.leaseState = leaseState;
    }

    /**
     * Sets the lease duration for the blob.
     * 
     * @param leaseDuration
     *        A {@link LeaseDuration} object which specifies the lease duration value to set.
     */
    protected void setLeaseDuration(final LeaseDuration leaseDuration) {
        this.leaseDuration = leaseDuration;
    }

    /**
     * Sets the content length, in bytes, for the blob.
     * 
     * @param length
     *        A <code>long</code> which specifies the length to set.
     */
    protected void setLength(final long length) {
        this.length = length;
    }
    
    /**
     * If the blob is a page blob, sets the blob's current sequence number.
     * 
     * @param pageBlobSequenceNumber
     *        A long containing the blob's current sequence number.
     */
    protected void setPageBlobSequenceNumber(final Long pageBlobSequenceNumber) {
    	this.pageBlobSequenceNumber = pageBlobSequenceNumber;
    }
}
