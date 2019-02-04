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
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#getBlobProperties(String, String, GetBlobPropertiesOptions)
 * getBlobProperties} request. These options include an optional server timeout
 * for the operation, the lease ID if the blob has an active lease, the snapshot
 * timestamp to get the properties of a snapshot, and any access conditions for
 * the request.
 */
public class GetBlobPropertiesOptions extends BlobServiceOptions {
    private String snapshot;
    private String leaseId;
    private AccessConditionHeader accessCondition;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link GetBlobPropertiesOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link GetBlobPropertiesOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link GetBlobPropertiesOptions} instance.
     */
    @Override
    public GetBlobPropertiesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the snapshot timestamp value set in this
     * {@link GetBlobPropertiesOptions} instance.
     * 
     * @return A {@link String} containing the snapshot timestamp value of the
     *         blob snapshot to get properties for.
     */
    public String getSnapshot() {
        return snapshot;
    }

    /**
     * Sets the snapshot timestamp value used to identify the particular
     * snapshot of the blob to get properties for. The snapshot timestamp value
     * is an opaque value returned by the server to identify a snapshot. When
     * this option is set, only the properties of the snapshot are returned.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link GetBlobPropertiesOptions} instance is passed as a parameter.
     * 
     * @param snapshot
     *            A {@link String} containing the snapshot timestamp value of
     *            the blob snapshot to get properties for.
     * @return A reference to this {@link GetBlobPropertiesOptions} instance.
     */
    public GetBlobPropertiesOptions setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * Gets the lease ID to match for the blob set in this
     * {@link GetBlobPropertiesOptions} instance.
     * 
     * @return A {@link String} containing the lease ID set, if any.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets a lease ID value to match when getting the properties of the blob.
     * If set, the lease must be active and the value must match the lease ID
     * set on the leased blob for the operation to succeed.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link GetBlobPropertiesOptions} instance is passed as a parameter.
     * 
     * @param leaseId
     *            A {@link String} containing the lease ID to set.
     * @return A reference to this {@link GetBlobPropertiesOptions} instance.
     */
    public GetBlobPropertiesOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the access conditions set in this {@link GetBlobPropertiesOptions}
     * instance.
     * 
     * @return An {@link AccessCondition} containing the access conditions set,
     *         if any.
     */
    public AccessConditionHeader getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets the access conditions for getting the properties of the blob. The
     * operation will return an error if the access conditions are not met.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} containing the access conditions to
     *            set.
     * @return A reference to this {@link GetBlobPropertiesOptions} instance.
     */
    public GetBlobPropertiesOptions setAccessCondition(
            AccessConditionHeader accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
