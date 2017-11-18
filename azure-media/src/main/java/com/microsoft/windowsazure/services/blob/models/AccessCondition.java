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
package com.microsoft.windowsazure.services.blob.models;

import java.net.HttpURLConnection;
import java.util.Date;

import com.microsoft.windowsazure.core.utils.Utility;

/**
 * Represents a set of access conditions to be used for operations against the
 * storage services.
 */
public final class AccessCondition {
    /**
     * Generates a new empty AccessCondition.
     * <p>
     * For more information, see <a href=
     * 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     * 
     * @return An <code>AccessCondition</code> object that has no conditions
     *         set.
     */
    public static AccessCondition generateEmptyCondition() {
        return new AccessCondition();
    }

    /**
     * Returns an access condition such that an operation will be performed only
     * if the resource's ETag value matches the specified ETag value.
     * <p>
     * Setting this access condition modifies the request to include the HTTP
     * <i>If-Match</i> conditional header. If this access condition is set, the
     * operation is performed only if the ETag of the resource matches the
     * specified ETag.
     * <p>
     * For more information, see <a href=
     * 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     * 
     * @param etag
     *            A <code>String</code> that represents the ETag value to check.
     * 
     * @return An <code>AccessCondition</code> object that represents the
     *         <i>If-Match</i> condition.
     */
    public static AccessCondition generateIfMatchCondition(final String etag) {
        AccessCondition retCondition = new AccessCondition();
        retCondition.setIfMatch(etag);
        return retCondition;
    }

    /**
     * Returns an access condition such that an operation will be performed only
     * if the resource has been modified since the specified time.
     * <p>
     * Setting this access condition modifies the request to include the HTTP
     * <i>If-Modified-Since</i> conditional header. If this access condition is
     * set, the operation is performed only if the resource has been modified
     * since the specified time.
     * <p>
     * For more information, see <a href=
     * 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     * 
     * @param lastMotified
     *            A <code>java.util.Date</code> object that represents the
     *            last-modified time to check for the resource.
     * 
     * @return An <code>AccessCondition</code> object that represents the
     *         <i>If-Modified-Since</i> condition.
     */
    public static AccessCondition generateIfModifiedSinceCondition(
            final Date lastMotified) {
        AccessCondition retCondition = new AccessCondition();
        retCondition.ifModifiedSinceDate = lastMotified;
        return retCondition;
    }

    /**
     * Returns an access condition such that an operation will be performed only
     * if the resource's ETag value does not match the specified ETag value.
     * <p>
     * Setting this access condition modifies the request to include the HTTP
     * <i>If-None-Match</i> conditional header. If this access condition is set,
     * the operation is performed only if the ETag of the resource does not
     * match the specified ETag.
     * <p>
     * For more information, see <a href=
     * 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     * 
     * @param etag
     *            A <code>String</code> that represents the ETag value to check.
     * 
     * @return An <code>AccessCondition</code> object that represents the
     *         <i>If-None-Match</i> condition.
     */
    public static AccessCondition generateIfNoneMatchCondition(final String etag) {
        AccessCondition retCondition = new AccessCondition();
        retCondition.setIfNoneMatch(etag);
        return retCondition;
    }

    /**
     * Returns an access condition such that an operation will be performed only
     * if the resource has not been modified since the specified time.
     * <p>
     * Setting this access condition modifies the request to include the HTTP
     * <i>If-Unmodified-Since</i> conditional header. If this access condition
     * is set, the operation is performed only if the resource has not been
     * modified since the specified time.
     * <p>
     * For more information, see <a href=
     * 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     * 
     * @param lastMotified
     *            A <code>java.util.Date</code> object that represents the
     *            last-modified time to check for the resource.
     * 
     * @return An <code>AccessCondition</code> object that represents the
     *         <i>If-Unmodified-Since</i> condition.
     */
    public static AccessCondition generateIfNotModifiedSinceCondition(
            final Date lastMotified) {
        AccessCondition retCondition = new AccessCondition();
        retCondition.ifUnmodifiedSinceDate = lastMotified;
        return retCondition;
    }

    /**
     * Returns an access condition such that an operation will be performed only
     * if the resource is accessible under the specified lease id.
     * <p>
     * Setting this access condition modifies the request to include the HTTP
     * <i>If-Unmodified-Since</i> conditional header. If this access condition
     * is set, the operation is performed only if the resource has not been
     * modified since the specified time.
     * <p>
     * For more information, see <a href=
     * 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     * 
     * @param leaseID
     *            The lease id to specify.
     * 
     */
    public static AccessCondition generateLeaseCondition(final String leaseID) {
        AccessCondition retCondition = new AccessCondition();
        retCondition.leaseID = leaseID;
        return retCondition;
    }

    private String leaseID = null;

    /**
     * Represents the etag of the resource for if [none] match conditions
     */
    private String etag = null;

    /**
     * Represents the date for IfModifiedSince conditions.
     */
    private Date ifModifiedSinceDate = null;

    /**
     * Represents the date for IfUn,odifiedSince conditions.
     */
    private Date ifUnmodifiedSinceDate = null;

    /**
     * Represents the ifMatchHeaderType type.
     */
    private String ifMatchHeaderType = null;

    /**
     * Creates an instance of the <code>AccessCondition</code> class.
     */
    public AccessCondition() {
        // Empty Default Ctor
    }

    /**
     * RESERVED FOR INTERNAL USE. Applies the access condition to the request.
     * 
     * @param request
     *            A <code>java.net.HttpURLConnection</code> object that
     *            represents the request to which the condition is being
     *            applied.
     * 
     * @throws StorageException
     *             If there is an error parsing the date value of the access
     *             condition.
     */
    public void applyConditionToRequest(final HttpURLConnection request) {
        applyConditionToRequest(request, false);
    }

    /**
     * RESERVED FOR INTERNAL USE. Applies the access condition to the request.
     * 
     * @param request
     *            A <code>java.net.HttpURLConnection</code> object that
     *            represents the request to which the condition is being
     *            applied.
     * @param useSourceAccessHeaders
     *            If true will use the Source_ headers for the conditions,
     *            otherwise standard headers are used.
     * @throws StorageException
     *             If there is an error parsing the date value of the access
     *             condition.
     */
    public void applyConditionToRequest(final HttpURLConnection request,
            boolean useSourceAccessHeaders) {
        // When used as a source access condition
        if (useSourceAccessHeaders) {
            if (!Utility.isNullOrEmpty(this.leaseID)) {
                request.setRequestProperty(
                        Constants.HeaderConstants.SOURCE_LEASE_ID_HEADER,
                        this.leaseID);
            }

            if (this.ifModifiedSinceDate != null) {
                request.setRequestProperty(
                        Constants.HeaderConstants.SOURCE_IF_MODIFIED_SINCE_HEADER,
                        Utility.getGMTTime(this.ifModifiedSinceDate));
            }

            if (this.ifUnmodifiedSinceDate != null) {
                request.setRequestProperty(
                        Constants.HeaderConstants.SOURCE_IF_UNMODIFIED_SINCE_HEADER,
                        Utility.getGMTTime(this.ifUnmodifiedSinceDate));
            }

            if (!Utility.isNullOrEmpty(this.etag)) {
                if (this.ifMatchHeaderType
                        .equals(Constants.HeaderConstants.IF_MATCH)) {
                    request.setRequestProperty(
                            Constants.HeaderConstants.SOURCE_IF_MATCH_HEADER,
                            this.etag);
                } else if (this.ifMatchHeaderType
                        .equals(Constants.HeaderConstants.IF_NONE_MATCH)) {
                    request.setRequestProperty(
                            Constants.HeaderConstants.SOURCE_IF_NONE_MATCH_HEADER,
                            this.etag);
                }
            }
        } else {
            if (!Utility.isNullOrEmpty(this.leaseID)) {
                addOptionalHeader(request, "x-ms-lease-id", this.leaseID);

            }

            if (this.ifModifiedSinceDate != null) {
                // The IfModifiedSince has a special helper in
                // HttpURLConnection, use it instead of manually setting the
                // header.
                request.setIfModifiedSince(this.ifModifiedSinceDate.getTime());
            }

            if (this.ifUnmodifiedSinceDate != null) {
                request.setRequestProperty(
                        Constants.HeaderConstants.IF_UNMODIFIED_SINCE,
                        Utility.getGMTTime(this.ifUnmodifiedSinceDate));
            }

            if (!Utility.isNullOrEmpty(this.etag)) {
                request.setRequestProperty(this.ifMatchHeaderType, this.etag);
            }
        }
    }

    /**
     * @return the etag when the If-Match condition is set.
     */
    public String getIfMatch() {
        return this.ifMatchHeaderType
                .equals(Constants.HeaderConstants.IF_MATCH) ? this.etag : null;
    }

    /**
     * @return the ifModifiedSinceDate
     */
    public Date getIfModifiedSinceDate() {
        return this.ifModifiedSinceDate;
    }

    /**
     * @return the etag when the If-None-Match condition is set.
     */
    public String getIfNoneMatch() {
        return this.ifMatchHeaderType
                .equals(Constants.HeaderConstants.IF_NONE_MATCH) ? this.etag
                : null;
    }

    /**
     * @return the ifUnmodifiedSinceDate
     */
    public Date getIfUnmodifiedSinceDate() {
        return this.ifUnmodifiedSinceDate;
    }

    /**
     * @return the leaseID
     */
    public String getLeaseID() {
        return this.leaseID;
    }

    /**
     * @param etag
     *            the etag to set
     */
    public void setIfMatch(String etag) {
        this.etag = normalizeEtag(etag);
        this.ifMatchHeaderType = Constants.HeaderConstants.IF_MATCH;
    }

    /**
     * @param ifModifiedSinceDate
     *            the ifModifiedSinceDate to set
     */
    public void setIfModifiedSinceDate(Date ifModifiedSinceDate) {
        this.ifModifiedSinceDate = ifModifiedSinceDate;
    }

    /**
     * @param etag
     *            the etag to set
     */
    public void setIfNoneMatch(String etag) {
        this.etag = normalizeEtag(etag);
        this.ifMatchHeaderType = Constants.HeaderConstants.IF_NONE_MATCH;
    }

    /**
     * @param ifUnmodifiedSinceDate
     *            the ifUnmodifiedSinceDate to set
     */
    public void setIfUnmodifiedSinceDate(Date ifUnmodifiedSinceDate) {
        this.ifUnmodifiedSinceDate = ifUnmodifiedSinceDate;
    }

    /**
     * @param leaseID
     *            the leaseID to set
     */
    public void setLeaseID(String leaseID) {
        this.leaseID = leaseID;
    }

    /**
     * Reserved for internal use. Verifies the condition is satisfied.
     * 
     * @param etag
     *            A <code>String</code> that represents the ETag to check.
     * @param lastModified
     *            A <code>java.util.Date</code> object that represents the last
     *            modified date/time.
     * 
     * @return <code>true</code> if the condition is satisfied; otherwise,
     *         <code>false</code>.
     * 
     */
    public boolean verifyConditional(final String etag, final Date lastModified) {
        if (this.ifModifiedSinceDate != null) {
            // The IfModifiedSince has a special helper in HttpURLConnection,
            // use it instead of manually setting the
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

        if (!Utility.isNullOrEmpty(this.etag)) {
            if (this.ifMatchHeaderType
                    .equals(Constants.HeaderConstants.IF_MATCH)) {
                if (!this.etag.equals(etag) && !this.etag.equals("*")) {
                    return false;
                }
            } else if (this.ifMatchHeaderType
                    .equals(Constants.HeaderConstants.IF_NONE_MATCH)) {
                if (this.etag.equals(etag)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Normalizes an Etag to be quoted, unless it is *
     * 
     * @param inTag
     *            the etag to normalize
     * @return the quoted etag
     */
    private static String normalizeEtag(String inTag) {
        if (Utility.isNullOrEmpty(inTag) || inTag.equals("*")) {
            return inTag;
        } else if (inTag.startsWith("\"") && inTag.endsWith("\"")) {
            return inTag;
        } else {
            return String.format("\"%s\"", inTag);
        }
    }

    /**
     * Adds the optional header.
     * 
     * @param request
     *            a HttpURLConnection for the operation.
     * @param name
     *            the metadata name.
     * @param value
     *            the metadata value.
     */
    public static void addOptionalHeader(final HttpURLConnection request,
            final String name, final String value) {
        if (value != null && !value.equals(Constants.EMPTY_STRING)) {
            request.setRequestProperty(name, value);
        }
    }
}
