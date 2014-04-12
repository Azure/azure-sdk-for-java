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
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#setContainerMetadata(String, java.util.HashMap, SetContainerMetadataOptions)}
 * request. These options include a server response timeout for the request and
 * access conditions that specify whether to perform the operation or not
 * depending on the values of the Etag or last modified time of the container.
 * Options that are not set will not be passed to the server with a request.
 */
public class SetContainerMetadataOptions extends BlobServiceOptions {
    private AccessConditionHeader accessCondition;

    /**
     * Sets the server request timeout value associated with this
     * {@link SetContainerMetadataOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link SetContainerMetadataOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link SetContainerMetadataOptions} instance.
     */
    @Override
    public SetContainerMetadataOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the access conditions associated with this
     * {@link SetContainerMetadataOptions} instance.
     * 
     * @return An {@link AccessCondition} reference containing the Etag and last
     *         modified time conditions for performing the set container
     *         metadata operation, or <code>null</code> if not set.
     */
    public AccessConditionHeader getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets the access conditions associated with this
     * {@link SetContainerMetadataOptions} instance. By default, the set
     * container metadata operation will set the container metadata
     * unconditionally. Use this method to specify conditions on the Etag or
     * last modified time value for performing the set container metadata
     * operation.
     * <p>
     * The <em>accessCondition</em> value only affects calls made on methods
     * where this {@link SetContainerMetadataOptions} instance is passed as a
     * parameter.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} reference containing the Etag and
     *            last modified time conditions for performing the set container
     *            metadata operation. Specify <code>null</code> to make the
     *            operation unconditional.
     * @return A reference to this {@link SetContainerMetadataOptions} instance.
     */
    public SetContainerMetadataOptions setAccessCondition(
            AccessConditionHeader accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
