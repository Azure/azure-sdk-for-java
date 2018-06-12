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
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#setBlobMetadata(String, String, java.util.HashMap, SetBlobMetadataOptions)
 * setBlobMetadata} request. These options include an optional server timeout
 * for the operation, a blob lease ID, and any access conditions for the
 * operation.
 */
public class SetBlobMetadataOptions extends BlobServiceOptions {
    private String leaseId;
    private AccessConditionHeader accessCondition;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link SetBlobMetadataOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link SetBlobMetadataOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link SetBlobMetadataOptions} instance.
     */
    @Override
    public SetBlobMetadataOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the lease ID to match for the blob set in this
     * {@link SetBlobMetadataOptions} instance.
     * 
     * @return A {@link String} containing the lease ID set, if any.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets an optional lease ID value to match when setting metadata of the
     * blob. If set, the lease must be active and the value must match the lease
     * ID set on the leased blob for the operation to succeed.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link SetBlobMetadataOptions} instance is passed as a parameter.
     * 
     * @param leaseId
     *            A {@link String} containing the lease ID to set.
     * @return A reference to this {@link SetBlobMetadataOptions} instance.
     */
    public SetBlobMetadataOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the access conditions set in this {@link SetBlobMetadataOptions}
     * instance.
     * 
     * @return An {@link AccessCondition} containing the access conditions set,
     *         if any.
     */
    public AccessConditionHeader getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets the access conditions for setting the metadata of a blob. By
     * default, the set blob metadata operation will set the metadata
     * unconditionally. Use this method to specify conditions on the ETag or
     * last modified time value for performing the operation.
     * <p>
     * The <em>accessCondition</em> value only affects calls made on methods
     * where this {@link SetBlobMetadataOptions} instance is passed as a
     * parameter.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} containing the access conditions to
     *            set.
     * @return A reference to this {@link SetBlobMetadataOptions} instance.
     */
    public SetBlobMetadataOptions setAccessCondition(
            AccessConditionHeader accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
