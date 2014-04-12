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

import com.microsoft.windowsazure.core.utils.AccessConditionHeader;

/**
 * Represents the options that may be set on a
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#getBlob(String, String, GetBlobOptions) getBlob} request.
 * These options include an optional server timeout for the operation, a
 * snapshot timestamp to specify a snapshot, a blob lease ID to get a blob with
 * an active lease, an optional start and end range for blob content to return,
 * and any access conditions to satisfy.
 */
public class GetBlobOptions extends BlobServiceOptions {
    private String snapshot;
    private String leaseId;
    private boolean computeRangeMD5;
    private Long rangeStart;
    private Long rangeEnd;
    private AccessConditionHeader accessCondition;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link GetBlobOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link GetBlobOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link GetBlobOptions} instance.
     */
    @Override
    public GetBlobOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the snapshot timestamp value set in this {@link GetBlobOptions}
     * instance.
     * 
     * @return A {@link String} containing the snapshot timestamp value of the
     *         blob snapshot to get.
     */
    public String getSnapshot() {
        return snapshot;
    }

    /**
     * Sets an optional snapshot timestamp value used to identify the particular
     * snapshot of the blob to get properties, metadata, and content for. The
     * snapshot timestamp value is an opaque value returned by the server to
     * identify a snapshot. When this option is set, only the properties,
     * metadata, and content of the snapshot are returned.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link GetBlobOptions} instance is passed as a parameter.
     * 
     * @param snapshot
     *            A {@link String} containing the snapshot timestamp value of
     *            the blob snapshot to get.
     * @return A reference to this {@link GetBlobOptions} instance.
     */
    public GetBlobOptions setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * Gets the lease ID to match for the blob set in this
     * {@link GetBlobOptions} instance.
     * 
     * @return A {@link String} containing the lease ID set, if any.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets an optional lease ID value to match when getting the blob. If set,
     * the lease must be active and the value must match the lease ID set on the
     * leased blob for the operation to succeed.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link GetBlobOptions} instance is passed as a parameter.
     * 
     * @param leaseId
     *            A {@link String} containing the lease ID to set.
     * @return A reference to this {@link GetBlobOptions} instance.
     */
    public GetBlobOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Reserved for future use. Gets a flag indicating whether to return an MD5
     * hash for the specified range of blob content set in this
     * {@link GetBlobOptions} instance.
     * 
     * @return A flag value of <code>true</code> to get the MD5 hash value for
     *         the specified range, otherwise <code>false</code>.
     */
    public boolean isComputeRangeMD5() {
        return computeRangeMD5;
    }

    /**
     * Reserved for future use. Sets a flag indicating whether to return an MD5
     * hash for the specified range of blob content.
     * <p>
     * When the <em>computeRangeMD5</em> parameter is set to <code>true</code>
     * and specified together with a range less than or equal to 4 MB in size,
     * the get blob operation response includes the MD5 hash for the range. If
     * the <em>computeRangeMD5</em> parameter is set to <code>true</code> and no
     * range is specified or the range exceeds 4 MB in size, a
     * {@link com.microsoft.windowsazure.exception.ServiceException} is thrown.
     * 
     * @param computeRangeMD5
     *            Reserved for future use. Set a flag value of <code>true</code>
     *            to get the MD5 hash value for the specified range, otherwise
     *            <code>false</code>.
     * @return A reference to this {@link GetBlobOptions} instance.
     */
    public GetBlobOptions setComputeRangeMD5(boolean computeRangeMD5) {
        this.computeRangeMD5 = computeRangeMD5;
        return this;
    }

    /**
     * Gets the beginning byte offset value of the blob content range to return
     * set in this {@link GetBlobOptions} instance.
     * 
     * @return The beginning offset value in bytes for the blob content range to
     *         return.
     */
    public Long getRangeStart() {
        return rangeStart;
    }

    /**
     * Sets an optional beginning byte offset value of the blob content range to
     * return for the request, inclusive.
     * <p>
     * When this value is set, the blob content beginning at the byte offset
     * specified by the <em>rangeStart</em> value and ending at the range end
     * value, inclusive, is returned in the server response to the get blob
     * operation. If the range end is not set, the response includes blob
     * content from the <em>rangeStart</em> value to the end of the blob.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link GetBlobOptions} instance is passed as a parameter.
     * 
     * @param rangeStart
     *            The beginning offset value in bytes for the blob content range
     *            to return, inclusive.
     * @return A reference to this {@link GetBlobOptions} instance.
     */
    public GetBlobOptions setRangeStart(Long rangeStart) {
        this.rangeStart = rangeStart;
        return this;
    }

    /**
     * Gets the ending byte offset value for the blob content range to return
     * set in this {@link GetBlobOptions} instance.
     * 
     * @return The ending offset value in bytes for the blob content range to
     *         return.
     */
    public Long getRangeEnd() {
        return rangeEnd;
    }

    /**
     * Sets an optional ending byte offset value of the blob content range to
     * return for the request, inclusive.
     * <p>
     * If the range start is not set, this value is ignored and the response
     * includes content from the entire blob.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link GetBlobOptions} instance is passed as a parameter.
     * 
     * @param rangeEnd
     *            The ending offset value in bytes for the blob content range to
     *            return, inclusive.
     * @return A reference to this {@link GetBlobOptions} instance.
     */
    public GetBlobOptions setRangeEnd(Long rangeEnd) {
        this.rangeEnd = rangeEnd;
        return this;
    }

    /**
     * Gets the access conditions set in this {@link GetBlobOptions} instance.
     * 
     * @return An {@link AccessCondition} containing the access conditions set,
     *         if any.
     */
    public AccessConditionHeader getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets optional access conditions for getting the blob. The operation will
     * return an error if the access conditions are not met.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link GetBlobOptions} instance is passed as a parameter.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} containing the access conditions to
     *            set.
     * @return A reference to this {@link GetBlobOptions} instance.
     */
    public GetBlobOptions setAccessCondition(
            AccessConditionHeader accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
