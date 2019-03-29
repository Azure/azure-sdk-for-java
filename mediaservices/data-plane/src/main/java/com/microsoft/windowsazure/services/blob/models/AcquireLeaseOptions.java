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
 * Represents the options that may be set on an
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#acquireLease(String, String, AcquireLeaseOptions)
 * acquireLease} request. These options include an optional server timeout for
 * the operation and any access conditions for the operation.
 */
public class AcquireLeaseOptions extends BlobServiceOptions {
    private AccessConditionHeader accessCondition;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link AcquireLeaseOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link AcquireLeaseOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link AcquireLeaseOptions} instance.
     */
    @Override
    public AcquireLeaseOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the access conditions set in this {@link AcquireLeaseOptions}
     * instance.
     * 
     * @return An {@link AccessCondition} containing the access conditions set,
     *         if any.
     */
    public AccessConditionHeader getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets the access conditions for acquiring a lease on a blob. By default,
     * the operation will acquire the lease unconditionally. Use this method to
     * specify conditions on the ETag or last modified time value for performing
     * the operation.
     * <p>
     * The <em>accessCondition</em> value only affects calls made on methods
     * where this {@link AcquireLeaseOptions} instance is passed as a parameter.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} containing the access conditions to
     *            set.
     * @return A reference to this {@link AcquireLeaseOptions} instance.
     */
    public AcquireLeaseOptions setAccessCondition(
            AccessConditionHeader accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
