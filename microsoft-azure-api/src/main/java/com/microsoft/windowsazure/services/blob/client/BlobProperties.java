/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.blob.client;

import java.util.Date;

import com.microsoft.windowsazure.services.core.storage.AccessCondition;
import com.microsoft.windowsazure.services.core.storage.LeaseDuration;
import com.microsoft.windowsazure.services.core.storage.LeaseState;
import com.microsoft.windowsazure.services.core.storage.LeaseStatus;

/**
 * Represents the system properties for a blob.
 */
public final class BlobProperties {

    /**
     * Represents the type of the blob.
     */
    private BlobType blobType = BlobType.UNSPECIFIED;

    /**
     * Represents the cache-control value stored for the blob.
     */
    private String cacheControl;

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
     * Represents the blob's ETag value.
     * <p>
     * The ETag value is a unique identifier that is updated when a write operation is performed against the container.
     * It may be used to perform operations conditionally, providing concurrency control and improved efficiency.
     * <p>
     * The {@link AccessCondition#ifMatch} and {@link AccessCondition#ifNoneMatch} methods take an ETag value and return
     * an {@link AccessCondition} object that may be specified on the request.
     */
    private String etag;

    /**
     * Represents the last-modified time for the blob.
     */
    private Date lastModified;

    /**
     * Represents the blob's lease status.
     */
    private LeaseStatus leaseStatus = com.microsoft.windowsazure.services.core.storage.LeaseStatus.UNLOCKED;

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
     *            A <code>BlobProperties</code> object that represents the blob properties to copy.
     */
    public BlobProperties(final BlobProperties other) {
        this.blobType = other.blobType;
        this.contentEncoding = other.contentEncoding;
        this.contentLanguage = other.contentLanguage;
        this.contentType = other.contentType;
        this.etag = other.etag;
        this.leaseStatus = other.leaseStatus;
        this.leaseState = other.leaseState;
        this.leaseDuration = other.leaseDuration;
        this.length = other.length;
        this.lastModified = other.lastModified;
        this.contentMD5 = other.contentMD5;
        this.cacheControl = other.cacheControl;
    }

    /**
     * Creates an instance of the <code>BlobProperties</code> class.
     */
    public BlobProperties(final BlobType type) {
        this.blobType = type;
    }

    /**
     * Returns the blob type for the blob.
     * 
     * @return A {@link BlobType} value that represents the blob type.
     */
    public BlobType getBlobType() {
        return this.blobType;
    }

    /**
     * @return the cacheControl
     */
    public String getCacheControl() {
        return this.cacheControl;
    }

    /**
     * @return the contentEncoding
     */
    public String getContentEncoding() {
        return this.contentEncoding;
    }

    /**
     * @return the contentLanguage
     */
    public String getContentLanguage() {
        return this.contentLanguage;
    }

    /**
     * @return the contentMD5
     */
    public String getContentMD5() {
        return this.contentMD5;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * @return the etag
     */
    public String getEtag() {
        return this.etag;
    }

    /**
     * @return the lastModified
     */
    public Date getLastModified() {
        return this.lastModified;
    }

    /**
     * Reserved for internal use.
     * 
     * @return the leaseStatus
     */
    public LeaseStatus getLeaseStatus() {
        return this.leaseStatus;
    }

    /**
     * @return the leaseState
     */
    public LeaseState getLeaseState() {
        return this.leaseState;
    }

    /**
     * @return the leaseDuration
     */
    public LeaseDuration getLeaseDuration() {
        return this.leaseDuration;
    }

    /**
     * @return the length
     */
    public long getLength() {
        return this.length;
    }

    /**
     * Reserved for internal use.
     * 
     * @param blobType
     *            the blobType to set
     */
    protected void setBlobType(final BlobType blobType) {
        this.blobType = blobType;
    }

    /**
     * @param cacheControl
     *            the cacheControl to set
     */
    public void setCacheControl(final String cacheControl) {
        this.cacheControl = cacheControl;
    }

    /**
     * @param contentEncoding
     *            the contentEncoding to set
     */
    public void setContentEncoding(final String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     * @param contentLanguage
     *            the contentLanguage to set
     */
    public void setContentLanguage(final String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    /**
     * @param contentMD5
     *            the contentMD5 to set
     */
    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    /**
     * @param contentType
     *            the contentType to set
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * Reserved for internal use.
     * 
     * @param etag
     *            the etag to set
     */
    public void setEtag(final String etag) {
        this.etag = etag;
    }

    /**
     * Reserved for internal use.
     * 
     * @param lastModified
     *            the lastModified to set
     */
    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Reserved for internal use.
     * 
     * @param leaseStatus
     *            the leaseStatus to set
     */
    public void setLeaseStatus(final LeaseStatus leaseStatus) {
        this.leaseStatus = leaseStatus;
    }

    /**
     * Reserved for internal use.
     * 
     * @param LeaseState
     *            the LeaseState to set
     */
    public void setLeaseState(final LeaseState leaseState) {
        this.leaseState = leaseState;
    }

    /**
     * Reserved for internal use.
     * 
     * @param LeaseDuration
     *            the LeaseDuration to set
     */
    public void setLeaseDuration(final LeaseDuration leaseDuration) {
        this.leaseDuration = leaseDuration;
    }

    /**
     * Reserved for internal use.
     * 
     * @param length
     *            the length to set
     */
    public void setLength(final long length) {
        this.length = length;
    }

}
