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
 * Represents the base class for options that may be set on Blob Service REST API operations invoked through the
 * {@link BlobContract} interface. This class defines a server request timeout, which can be applied to all operations.
 */
public class BlobServiceOptions {
    // Nullable because it is optional
    private Integer timeout;

    /**
     * Gets the server request timeout value associated with this {@link BlobServiceOptions} instance.
     * <p>
     * The timeout value only affects calls made on methods where this {@link BlobServiceOptions} instance is passed as
     * a parameter.
     * 
     * @return
     *         The server request timeout value in milliseconds.
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Sets the server request timeout value associated with this {@link BlobServiceOptions} instance.
     * <p>
     * The timeout value only affects calls made on methods where this {@link BlobServiceOptions} instance is passed as
     * a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return
     *         A reference to this {@link BlobServiceOptions} instance.
     */
    public BlobServiceOptions setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }
}
