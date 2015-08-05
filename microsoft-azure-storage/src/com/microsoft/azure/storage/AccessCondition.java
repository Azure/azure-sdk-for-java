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
package com.microsoft.azure.storage;

import java.net.HttpURLConnection;
import java.util.Date;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents a set of access conditions to be used for operations against the storage services.
 */
public final class AccessCondition {
    /**
     * Generates a new empty AccessCondition.
     * <p>
     * For more information, see <a href= 'http://go.microsoft.com/fwlink/?LinkID=224642'>Specifying Conditional Headers
     * for Blob Service Operations</a>.
     * 
     * @return An <code>AccessCondition</code> object that has no conditions set.
     */
    public static AccessCondition generateEmptyCondition() {
        return new AccessCondition();
    }
    
    /**
     * Returns an access condition such that an operation will be performed only if the resource's ETag value matches
     * the specified ETag value.
     * <p>
     * Setting this access condition modifies the request to include the HTTP <i>If-Match</i> conditional header. If
     * this access condition is set, the operation is performed only if the ETag of the resource matches the specified
     * ETag.
     * <p>
     * For more information, see <a href= 'http://go.microsoft.com/fwlink/?LinkID=224642'>Specifying Conditional Headers
     * for Blob Service Operations</a>.
     * 
     * @param etag
     *            A <code>String</code> that represents the ETag value to check.
     * 
     * @return An <code>AccessCondition</code> object that represents the <i>If-Match</i> condition.
     */
    public static AccessCondition generateIfMatchCondition(final String etag) {
        AccessCondition retCondition = new AccessCondition();
        retCondition.setIfMatch(etag);
        return retCondition;
    }
    
    /**
     * Returns an access condition such that an operation will be performed only if the resource has been modified since
     * the specified time.
     * <p>
     * Setting this access condition modifies the request to include the HTTP <i>If-Modified-Since</i> conditional
     * header. If this access condition is set, the operation is performed only if the resource has been modified since
     * the specified time.
     * <p>
     * For more information, see <a href= 'http://go.microsoft.com/fwlink/?LinkID=224642'>Specifying Conditional Headers
     * for Blob Service Operations</a>.
     * 
     * @param lastMotified
     *            A <code>java.util.Date</code> object that represents the last-modified time to check for the resource.
     * 
     * @return An <code>AccessCondition</code> object that represents the <i>If-Modified-Since</i> condition.
     */
    public static AccessCondition generateIfModifiedSinceCondition(final Date lastMotified) {
        AccessCondition retCondition = new AccessCondition();
        retCondition.ifModifiedSinceDate = lastMotified;
        return retCondition;
    }

    /**
     * Returns an access condition such that an operation will be performed only if the resource's ETag value does not
     * match the specified ETag value.
     * <p>
     * Setting this access condition modifies the request to include the HTTP <i>If-None-Match</i> conditional header.
     * If this access condition is set, the operation is performed only if the ETag of the resource does not match the
     * specified ETag.
     * <p>
     * For more information, see <a href= 'http://go.microsoft.com/fwlink/?LinkID=224642'>Specifying Conditional Headers
     * for Blob Service Operations</a>.
     * 
     * @param etag
     *            A <code>String</code> that represents the ETag value to check.
     * 
     * @return An <code>AccessCondition</code> object that represents the <i>If-None-Match</i> condition.
     */
    public static AccessCondition generateIfNoneMatchCondition(final String etag) {
        AccessCondition retCondition = new AccessCondition();
        retCondition.setIfNoneMatch(etag);
        return retCondition;
    }

    /**
     * Returns an access condition such that an operation will be performed only if the resource has not been modified
     * since the specified time.
     * <p>
     * Setting this access condition modifies the request to include the HTTP <i>If-Unmodified-Since</i> conditional
     * header. If this access condition is set, the operation is performed only if the resource has not been modified
     * since the specified time.
     * <p>
     * For more information, see <a href= 'http://go.microsoft.com/fwlink/?LinkID=224642'>Specifying Conditional Headers
     * for Blob Service Operations</a>.
     * 
     * @param lastMotified
     *            A <code>java.util.Date</code> object that represents the last-modified time to check for the resource.
     * 
     * @return An <code>AccessCondition</code> object that represents the <i>If-Unmodified-Since</i> condition.
     */
    public static AccessCondition generateIfNotModifiedSinceCondition(final Date lastMotified) {
        AccessCondition retCondition = new AccessCondition();
        retCondition.ifUnmodifiedSinceDate = lastMotified;
        return retCondition;
    }
    
    /**
     * Returns an access condition such that an operation will be performed only if resource's current sequence
     * number is less than or equal to the specified value. This condition only applies to page blobs.
     * 
     * @param sequenceNumber
     *            The value to compare to the current sequence number.
     * 
     * @return An <code>AccessCondition</code> object that represents the <i>If-Sequence-Number-LE</i> condition.
     */
    public static AccessCondition generateIfSequenceNumberLessThanOrEqualCondition(long sequenceNumber) {
        AccessCondition retCondition = new AccessCondition();
        retCondition.ifSequenceNumberLessThanOrEqual = sequenceNumber;
        return retCondition;
    }

    /**
     * Returns an access condition such that an operation will be performed only if resource's current sequence
     * number is less than the specified value. This condition only applies to page blobs.
     * 
     * @param sequenceNumber
     *            The value to compare to the current sequence number.
     * 
     * @return An <code>AccessCondition</code> object that represents the <i>If-Sequence-Number-LT</i> condition.
     */
    public static AccessCondition generateIfSequenceNumberLessThanCondition(long sequenceNumber) {
        AccessCondition retCondition = new AccessCondition();
        retCondition.ifSequenceNumberLessThan = sequenceNumber;
        return retCondition;
    }

    /**
     * Returns an access condition such that an operation will be performed only if resource's current sequence
     * number is equal to the specified value. This condition only applies to page blobs.
     * 
     * @param sequenceNumber
     *            The value to compare to the current sequence number.
     * 
     * @return An <code>AccessCondition</code> object that represents the <i>If-Sequence-Number-EQ</i> condition.
     */
    public static AccessCondition generateIfSequenceNumberEqualCondition(long sequenceNumber) {
        AccessCondition retCondition = new AccessCondition();
        retCondition.ifSequenceNumberEqual = sequenceNumber;
        return retCondition;
    }
    
    /**
     * Returns an access condition such that an operation will be performed only if the resource is accessible under the
     * specified lease ID.
     * <p>
     * For more information, see <a href= 'http://go.microsoft.com/fwlink/?LinkID=224642'>Specifying Conditional Headers
     * for Blob Service Operations</a>.
     * 
     * @param leaseID
     *            The lease ID to specify.
     * 
     * @return An <code>AccessCondition</code> object that represents the lease condition.
     */
    public static AccessCondition generateLeaseCondition(final String leaseID) {
        AccessCondition retCondition = new AccessCondition();
        retCondition.leaseID = leaseID;
        return retCondition;
    }
    
    /**
     * Returns an access condition such that an operation will be performed only if the resource exists on the service.
     * <p>
     * Setting this access condition modifies the request to include the HTTP <i>If-Match</i> conditional header.
     * <p>
     * For more information, see <a href= 'http://go.microsoft.com/fwlink/?LinkID=224642'>Specifying Conditional Headers
     * for Blob Service Operations</a>.
     * 
     * @return An <code>AccessCondition</code> object that represents the if exists condition.
     */
    public static AccessCondition generateIfExistsCondition() {
        AccessCondition retCondition = new AccessCondition();
        retCondition.setIfMatch("*");
        return retCondition;
    }
    
    /**
     * Returns an access condition such that an operation will be performed only if the resource does not exist on the 
     * service.
     * <p>
     * Setting this access condition modifies the request to include the HTTP <i>If-None-Match</i> conditional header.
     * <p>
     * For more information, see <a href= 'http://go.microsoft.com/fwlink/?LinkID=224642'>Specifying Conditional Headers
     * for Blob Service Operations</a>.
     * 
     * @return An <code>AccessCondition</code> object that represents the if not exists condition.
     */
    public static AccessCondition generateIfNotExistsCondition() {
        AccessCondition retCondition = new AccessCondition();
        retCondition.setIfNoneMatch("*");
        return retCondition;
    }

    private String leaseID = null;

    /**
     * Represents the ETag of the resource for if match conditions
     */
    private String ifMatchETag = null;
    
    /**
     * Represents the ETag of the resource for if none match conditions
     */
    private String ifNoneMatchETag = null;

    /**
     * Represents the date for ifModifiedSinceDate conditions.
     */
    private Date ifModifiedSinceDate = null;

    /**
     * Represents the date for ifUnmodifiedSinceDate conditions.
     */
    private Date ifUnmodifiedSinceDate = null;
    
    /**
     * Represents the ifSequenceNumberLessThanOrEqual type. Used only for page blob operations.
     */
    private Long ifSequenceNumberLessThanOrEqual = null;
    
    /**
     * Represents the ifSequenceNumberLessThan type. Used only for page blob operations.
     */
    private Long ifSequenceNumberLessThan = null;
    
    /**
     * Represents the ifSequenceNumberEqual type. Used only for page blob operations.
     */
    private Long ifSequenceNumberEqual = null;
    
    /**
     * Represents the ifMaxSizeLessThanOrEqual type. Used only for append blob operations.
     */
    private Long ifMaxSizeLessThanOrEqual = null;
    
    /**
     * Represents the ifAppendPositionEqual type. Used only for append blob operations.
     */
    private Long ifAppendPositionEqual = null;
    
    /**
     * Creates an instance of the <code>AccessCondition</code> class.
     */
    public AccessCondition() {
        // Empty default constructor.
    }

    /**
     * RESERVED FOR INTERNAL USE. Applies the access conditions to the request.
     * 
     * @param request
     *            A <code>java.net.HttpURLConnection</code> object that represents the request 
     *            to which the condition is being applied.
     */
    public void applyConditionToRequest(final HttpURLConnection request) {
        applyLeaseConditionToRequest(request);

        if (this.ifModifiedSinceDate != null) {
            request.setRequestProperty(Constants.HeaderConstants.IF_MODIFIED_SINCE,
                    Utility.getGMTTime(this.ifModifiedSinceDate));
        }

        if (this.ifUnmodifiedSinceDate != null) {
            request.setRequestProperty(Constants.HeaderConstants.IF_UNMODIFIED_SINCE,
                    Utility.getGMTTime(this.ifUnmodifiedSinceDate));
        }

        if (!Utility.isNullOrEmpty(this.ifMatchETag)) {
            request.setRequestProperty(Constants.HeaderConstants.IF_MATCH, this.ifMatchETag);
        }
        
        if (!Utility.isNullOrEmpty(this.ifNoneMatchETag)) {
            request.setRequestProperty(Constants.HeaderConstants.IF_NONE_MATCH, this.ifNoneMatchETag);
        }
    }

    /**
     * RESERVED FOR INTERNAL USE. Applies the source access conditions to the request.
     * 
     * @param request
     *            A <code>java.net.HttpURLConnection</code> object that represents the request 
     *            to which the condition is being applied.
     */
    public void applySourceConditionToRequest(final HttpURLConnection request) {    
        if (!Utility.isNullOrEmpty(this.leaseID)) {
            // Unsupported
            throw new IllegalArgumentException(SR.LEASE_CONDITION_ON_SOURCE);
        }

        if (this.ifModifiedSinceDate != null) {
            request.setRequestProperty(
                    Constants.HeaderConstants.SOURCE_IF_MODIFIED_SINCE_HEADER,
                    Utility.getGMTTime(this.ifModifiedSinceDate));
        }

        if (this.ifUnmodifiedSinceDate != null) {
            request.setRequestProperty(Constants.HeaderConstants.SOURCE_IF_UNMODIFIED_SINCE_HEADER,
                    Utility.getGMTTime(this.ifUnmodifiedSinceDate));
        }

        if (!Utility.isNullOrEmpty(this.ifMatchETag)) {
            request.setRequestProperty(
                    Constants.HeaderConstants.SOURCE_IF_MATCH_HEADER,
                    this.ifMatchETag);
        }

        if (!Utility.isNullOrEmpty(this.ifNoneMatchETag)) {
            request.setRequestProperty(
                    Constants.HeaderConstants.SOURCE_IF_NONE_MATCH_HEADER,
                    this.ifNoneMatchETag);
        }
    }
    
    /**
     * RESERVED FOR INTERNAL USE. Applies the access condition to the request.
     * 
     * @param request
     *            A <code>java.net.HttpURLConnection</code> object that represents the request to which the condition is
     *            being applied.
     * 
     * @throws StorageException
     *             If there is an error parsing the date value of the access condition.
     */
    public void applyAppendConditionToRequest(final HttpURLConnection request) {
        if (this.ifMaxSizeLessThanOrEqual != null) {
            request.setRequestProperty(Constants.HeaderConstants.IF_MAX_SIZE_LESS_THAN_OR_EQUAL,
                    this.ifMaxSizeLessThanOrEqual.toString());
        }

        if (this.ifAppendPositionEqual != null) {
            request.setRequestProperty(Constants.HeaderConstants.IF_APPEND_POSITION_EQUAL_HEADER,
                    this.ifAppendPositionEqual.toString());
        }
    }
    
    /**
     * RESERVED FOR INTERNAL USE. Applies the lease access condition to the request.
     * 
     * @param request
     *            A <code>java.net.HttpURLConnection</code> object that represents the request 
     *            to which the condition is being applied.
     */
    public void applyLeaseConditionToRequest(final HttpURLConnection request) {
        if (!Utility.isNullOrEmpty(this.leaseID)) {
            request.setRequestProperty(Constants.HeaderConstants.LEASE_ID_HEADER, this.leaseID);
        }
    }
    
    /**
     * RESERVED FOR INTERNAL USE. Applies the sequence number access conditions to the request.
     * 
     * @param request
     *            A <code>java.net.HttpURLConnection</code> object that represents the request 
     *            to which the condition is being applied.
     */
    public void applySequenceConditionToRequest(final HttpURLConnection request) {
        if (this.ifSequenceNumberLessThanOrEqual != null) {
            request.setRequestProperty(
                    Constants.HeaderConstants.IF_SEQUENCE_NUMBER_LESS_THAN_OR_EQUAL,
                    this.ifSequenceNumberLessThanOrEqual.toString());
        }

        if (this.ifSequenceNumberLessThan != null) {
            request.setRequestProperty(
                    Constants.HeaderConstants.IF_SEQUENCE_NUMBER_LESS_THAN,
                    this.ifSequenceNumberLessThan.toString());
        }
        
        if (this.ifSequenceNumberEqual != null) {
            request.setRequestProperty(
                    Constants.HeaderConstants.IF_SEQUENCE_NUMBER_EQUAL,
                    this.ifSequenceNumberEqual.toString());
        }
    }
    
    /**
     * Gets the value for a conditional header used only for append operations. A number indicating the byte offset to check for.
     * The append will succeed only if the end position is equal to this number. 
     * 
     * @return The append position number, or <code>null</code> if no condition exists.
     */
    public Long getIfAppendPositionEqual()
    {
        return ifAppendPositionEqual;
    } 
    
    /**
     * Gets the ETag when the <i>If-Match</i> condition is set.
     * 
     * @return The ETag when the <i>If-Match</i> condition is set; otherwise, null.
     */
    public String getIfMatch() {
        return this.ifMatchETag;
    }
    
    /**
     * Gets the value for a conditional header used only for append operations. A number that indicates the maximum length in 
     * bytes to restrict the blob to when committing the block. 
     * 
     * @return The maximum size, or <code>null</code> if no condition exists.
     */
    public Long getIfMaxSizeLessThanOrEqual()
    {
        return ifMaxSizeLessThanOrEqual;
    } 
    
    /**
     * Gets the <i>If-Modified-Since</i> date.
     * 
     * @return A <code>java.util.Date</code> object that represents the <i>If-Modified-Since</i> date.
     */
    public Date getIfModifiedSinceDate() {
        return this.ifModifiedSinceDate;
    }

    /**
     * Gets the ETag when the If-None-Match condition is set.
     * 
     * @return The ETag when the If-None-Match condition is set; otherwise, null.
     */
    public String getIfNoneMatch() {
        return this.ifNoneMatchETag;
    }

    /**
     * Gets the <i>If-Unmodified-Since</i> date.
     * 
     * @return A <code>java.util.Date</code> object that represents the <i>If-Unmodified-Since</i> date.
     */
    public Date getIfUnmodifiedSinceDate() {
        return this.ifUnmodifiedSinceDate;
    }

    /**
     * Gets the lease ID.
     * 
     * @return The lease ID.
     */
    public String getLeaseID() {
        return this.leaseID;
    }
    
    /**
     * Gets the sequence number when the sequence number less than or equal condition is set. This condition
     * is only applicable to page blobs.
     * 
     * @return The sequence number when the ifSequenceNumberLessThanOrEqual condition is set; otherwise, <code>null</code>
     */
    public Long getIfSequenceNumberLessThanOrEqual() {
        return this.ifSequenceNumberLessThanOrEqual;
    }

    /**
     * Gets the sequence number when the sequence number less than condition is set. This condition
     * is only applicable to page blobs.
     * 
     * @return The sequence number when the ifSequenceNumberLessThan condition is set; otherwise, <code>null</code>
     */
    public Long getIfSequenceNumberLessThan() {
        return this.ifSequenceNumberLessThan;
    }
    
    /**
     * Gets the sequence number when the sequence number equal condition is set. This condition
     * is only applicable to page blobs.
     * 
     * @return The sequence number when the ifSequenceNumberEqual condition is set; otherwise, <code>null</code>
     */
    public Long getIfSequenceNumberEqual() {
        return this.ifSequenceNumberEqual;
    }

    /**
     * Sets the value for a conditional header used only for append operations. A number indicating the byte offset to check for.
     * The append will succeed only if the end position is equal to this number. 
     * 
     * @param ifAppendPositionEqual
     *            The append position number, or <code>null</code> if no condition exists.
     */
    public void setIfAppendPositionEqual(Long ifAppendPositionEqual)
    {
        this.ifAppendPositionEqual = ifAppendPositionEqual;
    }  

    /**
     * Sets the ETag for the <i>If-Match</i> condition.
     * 
     * @param etag
     *            The ETag to set for the <i>If-Match</i> condition.
     */
    public void setIfMatch(String etag) {
        this.ifMatchETag = normalizeEtag(etag);
    }
    
    /**
     * Sets the value for a conditional header used only for append operations. A number that indicates the maximum length in 
     * bytes to restrict the blob to when committing the block. 
     * 
     * @param ifMaxSizeLessThanOrEqual
     *            The maximum size, or <code>null</code> if no condition exists.
     */
    public void setIfMaxSizeLessThanOrEqual(Long ifMaxSizeLessThanOrEqual)
    {
        this.ifMaxSizeLessThanOrEqual = ifMaxSizeLessThanOrEqual;
    }

    /**
     * Sets the <i>If-Modified-Since</i> date.
     * 
     * @param ifModifiedSinceDate
     *            A <code>java.util.Date</code> object that represents the <i>If-Modified-Since</i> to set.
     */
    public void setIfModifiedSinceDate(Date ifModifiedSinceDate) {
        this.ifModifiedSinceDate = ifModifiedSinceDate;
    }

    /**
     * Sets the ETag for the <i>If-None-Match</i> condition.
     * 
     * @param etag
     *            The ETag to set for the <i>If-None-Match</i> condition.
     */
    public void setIfNoneMatch(String etag) {
        this.ifNoneMatchETag = normalizeEtag(etag);
    }

    /**
     * Sets the <i>If-Unmodified-Since</i> date.
     * 
     * @param ifUnmodifiedSinceDate
     *            A <code>java.util.Date</code> object that represents the <i>If-Unmodified-Since</i> to set.
     */
    public void setIfUnmodifiedSinceDate(Date ifUnmodifiedSinceDate) {
        this.ifUnmodifiedSinceDate = ifUnmodifiedSinceDate;
    }

    /**
     * Gets the lease ID.
     * 
     * @param leaseID
     *            The lease ID to set.
     */
    public void setLeaseID(String leaseID) {
        this.leaseID = leaseID;
    }

    /**
     * Sets the sequence number for the sequence number less than or equal to condition. This condition
     * is only applicable to page blobs.
     * 
     * @param sequenceNumber
     *            The sequence number to set the if sequence number less than or equal condition to.
     */
    public void setIfSequenceNumberLessThanOrEqual(Long sequenceNumber) {
        this.ifSequenceNumberLessThanOrEqual = sequenceNumber;
    }

    /**
     * Sets the sequence number for the sequence number less than condition. This condition
     * is only applicable to page blobs.
     * 
     * @param sequenceNumber
     *            The sequence number to set the if sequence number less than condition to.
     */
    public void setIfSequenceNumberLessThan(Long sequenceNumber) {
        this.ifSequenceNumberLessThan = sequenceNumber;
    }
    
    /**
     * Sets the sequence number for the sequence number equal to condition. This condition
     * is only applicable to page blobs.
     * 
     * @param sequenceNumber
     *            The sequence number to set the if sequence number equal condition to.
     */
    public void setIfSequenceNumberEqual(Long sequenceNumber) {
        this.ifSequenceNumberEqual = sequenceNumber;
    }
    
    /**
     * RESERVED FOR INTERNAL USE. Verifies the condition is satisfied.
     * 
     * @param etag
     *            A <code>String</code> that represents the ETag to check.
     * @param lastModified
     *            A <code>java.util.Date</code> object that represents the last modified date/time.
     * 
     * @return <code>true</code> if the condition is satisfied; otherwise, <code>false</code>.
     * 
     */
    public boolean verifyConditional(final String etag, final Date lastModified) {
        if (this.ifModifiedSinceDate != null) {
            // The IfModifiedSince has a special helper in HttpURLConnection, use it instead of manually setting the
            // header.
            if (!lastModified.after(this.ifModifiedSinceDate)) {
                return false;
            }
        }

        if (this.ifUnmodifiedSinceDate != null) {
            if (lastModified.after(this.ifUnmodifiedSinceDate)) {
                return false;
            }
        }
        
        if (!Utility.isNullOrEmpty(this.ifMatchETag)) {
            if (!this.ifMatchETag.equals(etag) && !this.ifMatchETag.equals("*")) {
                return false;
            }
        }
        
        if (!Utility.isNullOrEmpty(this.ifNoneMatchETag)) {
            if (this.ifNoneMatchETag.equals(etag)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Normalizes an ETag to be quoted, unless it is *.
     * 
     * @param inTag
     *            The ETag to normalize.
     * @return The quoted ETag.
     */
    private static String normalizeEtag(String inTag) {
        if (Utility.isNullOrEmpty(inTag) || inTag.equals("*")) {
            return inTag;
        }
        else if (inTag.startsWith("\"") && inTag.endsWith("\"")) {
            return inTag;
        }
        else {
            return String.format("\"%s\"", inTag);
        }
    }
}
