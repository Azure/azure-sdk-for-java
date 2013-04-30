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
     * Returns the cache control value for the blob.
     * 
     * @return A string that represents the cache control value for the blob.
     */
    public String getCacheControl() {
        return this.cacheControl;
    }

    /**
     * Gets the content encoding value for the blob.
     * 
     * @return A string containing the content encoding, or <code>null</code> if content encoding has not been set 
     * on the blob.
     */
    public String getContentEncoding() {
        return this.contentEncoding;
    }

    /**
     * Gets the content language value for the blob.
     * 
     * @return A string containing the content language, or <code>null</code> if content language has not been set on 
     * the blob.
     */
    public String getContentLanguage() {
        return this.contentLanguage;
    }

    /**
     * Gets the content MD5 value for the blob.
     * 
     * @return A string containing the content MD5 value.
     */
    public String getContentMD5() {
        return this.contentMD5;
    }

    /**
     * Gets the content type value for the blob.
     * 
     * @return A string containing content type, or <code>null</code> if the content type has not be set for the blob.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Gets the ETag value for the blob.
     * 
     * @return A string containing the ETag value.
     */
    public String getEtag() {
        return this.etag;
    }

    /**
     * Gets the last modified time for the blob.
     * 
     * @return A <code>Date</code> containing the last modified time for the blob.
     */
    public Date getLastModified() {
        return this.lastModified;
    }

    /**
     * Gets the lease status for the blob. Reserved for internal use.
     * 
     * @return A <code>LeaseStatus</code> object representing the lease status.
     */
    public LeaseStatus getLeaseStatus() {
        return this.leaseStatus;
    }

    /**
     * Gets the lease state for the blob.
     * 
     * @return A <code>LeaseState</code> object representing the lease state.
     */
    public LeaseState getLeaseState() {
        return this.leaseState;
    }

    /**
     * Gets the lease duration for the blob.
     * 
     * @return A <code>LeaseDuration</code> object representing the lease duration.
     */
    public LeaseDuration getLeaseDuration() {
        return this.leaseDuration;
    }

    /**
     * Gets the size, in bytes, of the blob.
     * 
     * @return The length of the blob.
     */
    public long getLength() {
        return this.length;
    }

    /**
     * Sets the blob type. Reserved for internal use.
     * 
     * @param blobType
     *            The blob type to set, represented by a <code>BlobType</code> object.
     */
    protected void setBlobType(final BlobType blobType) {
        this.blobType = blobType;
    }

    /**
     * Sets the cache control value for the blob.
     * 
     * @param cacheControl
     *            The cache control value to set.
     */
    public void setCacheControl(final String cacheControl) {
        this.cacheControl = cacheControl;
    }

    /**
     * Sets the content encoding value for the blob.
     * 
     * @param contentEncoding
     *            The content encoding value to set.
     */
    public void setContentEncoding(final String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     * Sets the content language for the blob.
     * 
     * @param contentLanguage
     *            The content language value to set.
     */
    public void setContentLanguage(final String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    /**
     * Sets the content MD5 value for the blob.
     * 
     * @param contentMD5
     *            The content MD5 value to set.
     */
    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    /**
     * Sets the content type value for the blob.
     * 
     * @param contentType
     *            The content type value to set.
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * Sets the ETag value for the blob. Reserved for internal use.
     * 
     * @param etag
     *            The ETag value to set.
     */
    public void setEtag(final String etag) {
        this.etag = etag;
    }

    /**
     * Sets the last modified time for the blob. Reserved for internal use.
     * 
     * @param lastModified
     *            The last modified time to set.
     */
    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Sets the lease status for the blob. Reserved for internal use.
     * 
     * @param leaseStatus
     *            The lease status to set, represented by a <code>LeaseStatus</code> object.
     */
    public void setLeaseStatus(final LeaseStatus leaseStatus) {
        this.leaseStatus = leaseStatus;
    }

    /**
     * Sets the lease state for the blob. Reserved for internal use.
     * 
     * @param leaseState
     *            The lease state to set, represented by a <code>LeaseState</code> object.
     */
    public void setLeaseState(final LeaseState leaseState) {
        this.leaseState = leaseState;
    }

    /**
     * Sets the lease duration for the blob. Reserved for internal use.
     * 
     * @param leaseDuration
     *            The lease duration value to set, represented by a <code>LeaseDuration</code> object.
     */
    public void setLeaseDuration(final LeaseDuration leaseDuration) {
        this.leaseDuration = leaseDuration;
    }

    /**
     * Sets the content length, in bytes, for the blob. Reserved for internal use.
     * 
     * @param length
     *            The length to set.
     */
    public void setLength(final long length) {
        this.length = length;
    }

}
