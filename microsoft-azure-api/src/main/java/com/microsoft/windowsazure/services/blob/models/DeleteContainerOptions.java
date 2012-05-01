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
package com.microsoft.windowsazure.services.blob.models;

import com.microsoft.windowsazure.services.blob.BlobContract;

/**
 * Represents the options that may be set on a {@link BlobContract#deleteContainer(String, DeleteContainerOptions)
 * deleteContainer} request. These options include a server response timeout for the request and access conditions that
 * specify whether to perform the operation or not depending on the values of the Etag or last modified time of the
 * container. Options that are not set will not be passed to the server with a request.
 */
public class DeleteContainerOptions extends BlobServiceOptions {
    private AccessCondition accessCondition;

    /**
     * Sets the server request timeout value associated with this {@link DeleteContainerOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this {@link DeleteContainerOptions} instance
     * is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return
     *         A reference to this {@link DeleteContainerOptions} instance.
     */
    @Override
    public DeleteContainerOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the access conditions associated with this {@link DeleteContainerOptions} instance.
     * 
     * @return
     *         An {@link AccessCondition} reference containing the Etag and last modified time conditions for performing
     *         the delete container operation, or <code>null</code> if not set.
     */
    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets the access conditions associated with this {@link DeleteContainerOptions} instance. By default, the delete
     * container operation will delete the container unconditionally. Use this method to specify conditions on the Etag
     * or last modified time value for performing the delete container operation.
     * <p>
     * The <em>accessCondition</em> value only affects calls made on methods where this {@link DeleteContainerOptions}
     * instance is passed as a parameter.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} reference containing the Etag and last modified time conditions for
     *            performing the delete container operation. Specify <code>null</code> to make the operation
     *            unconditional.
     * @return
     *         A reference to this {@link DeleteContainerOptions} instance.
     */
    public DeleteContainerOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
