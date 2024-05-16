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
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#listBlobRegions(String, String, ListBlobRegionsOptions)
 * listBlobRegions} request. These options include an optional server timeout
 * for the operation, the lease ID if the blob has an active lease, the snapshot
 * timestamp to get the valid page ranges of a snapshot, the start offset and/or
 * end offset to use to narrow the returned valid page range results, and any
 * access conditions for the request.
 */
public class ListBlobRegionsOptions extends BlobServiceOptions {
    private String leaseId;
    private String snapshot;
    private Long rangeStart;
    private Long rangeEnd;
    private AccessConditionHeader accessCondition;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link ListBlobRegionsOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link ListBlobRegionsOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link ListBlobRegionsOptions} instance.
     */
    @Override
    public ListBlobRegionsOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the lease ID to match for the blob set in this
     * {@link ListBlobRegionsOptions} instance.
     * 
     * @return A {@link String} containing the lease ID set, if any.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets an optional lease ID value to match when getting the valid page
     * ranges of the blob. If set, the lease must be active and the value must
     * match the lease ID set on the leased blob for the operation to succeed.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link ListBlobRegionsOptions} instance is passed as a parameter.
     * 
     * @param leaseId
     *            A {@link String} containing the lease ID to set.
     * @return A reference to this {@link ListBlobRegionsOptions} instance.
     */
    public ListBlobRegionsOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the snapshot timestamp value set in this
     * {@link ListBlobRegionsOptions} instance.
     * 
     * @return A {@link String} containing the snapshot timestamp value of the
     *         blob snapshot to get valid page ranges for.
     */
    public String getSnapshot() {
        return snapshot;
    }

    /**
     * Sets an optional snapshot timestamp value used to identify the particular
     * snapshot of the blob to get valid page ranges for. The snapshot timestamp
     * value is an opaque value returned by the server to identify a snapshot.
     * When this option is set, only the valid page ranges of the snapshot are
     * returned.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link ListBlobRegionsOptions} instance is passed as a parameter.
     * 
     * @param snapshot
     *            A {@link String} containing the snapshot timestamp value of
     *            the blob snapshot to get valid page ranges for.
     * @return A reference to this {@link ListBlobRegionsOptions} instance.
     */
    public ListBlobRegionsOptions setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * Gets the beginning byte offset value of the valid page ranges to return
     * set in this {@link ListBlobRegionsOptions} instance.
     * 
     * @return The beginning offset value in bytes for the valid page ranges to
     *         return.
     */
    public Long getRangeStart() {
        return rangeStart;
    }

    /**
     * Sets an optional beginning byte offset value of the valid page ranges to
     * return for the request, inclusive.
     * <p>
     * If the range end is not set, the response includes valid page ranges from
     * the <em>rangeStart</em> value to the end of the blob.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link ListBlobRegionsOptions} instance is passed as a parameter.
     * 
     * @param rangeStart
     *            The beginning offset value in bytes for the valid page ranges
     *            to return, inclusive.
     * @return A reference to this {@link ListBlobRegionsOptions} instance.
     */
    public ListBlobRegionsOptions setRangeStart(Long rangeStart) {
        this.rangeStart = rangeStart;
        return this;
    }

    /**
     * Gets the ending byte offset value for the valid page ranges to return set
     * in this {@link ListBlobRegionsOptions} instance.
     * 
     * @return The ending offset value in bytes for the valid page ranges to
     *         return.
     */
    public Long getRangeEnd() {
        return rangeEnd;
    }

    /**
     * Sets an optional ending byte offset value of the valid page ranges to
     * return for the request, inclusive.
     * <p>
     * If the range start is not set, this value is ignored and the response
     * includes valid page ranges from the entire blob.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link ListBlobRegionsOptions} instance is passed as a parameter.
     * 
     * @param rangeEnd
     *            The ending offset value in bytes for the valid page ranges to
     *            return, inclusive.
     * @return A reference to this {@link ListBlobRegionsOptions} instance.
     */
    public ListBlobRegionsOptions setRangeEnd(Long rangeEnd) {
        this.rangeEnd = rangeEnd;
        return this;
    }

    /**
     * Gets the access conditions set in this {@link ListBlobRegionsOptions}
     * instance.
     * 
     * @return An {@link AccessCondition} containing the access conditions set,
     *         if any.
     */
    public AccessConditionHeader getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets optional access conditions for getting the valid page ranges of the
     * blob. The operation will return an error if the access conditions are not
     * met.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link ListBlobRegionsOptions} instance is passed as a parameter.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} containing the access conditions to
     *            set.
     * @return A reference to this {@link ListBlobRegionsOptions} instance.
     */
    public ListBlobRegionsOptions setAccessCondition(
            AccessConditionHeader accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
