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
package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.core.Utility;

/**
 * Represents the optional headers that can be returned with blobs accessed using SAS.
 */
public final class SharedAccessBlobHeaders {

    /**
     * The cache-control header returned with the blob.
     */
    private String cacheControl;

    /**
     * The content-disposition header returned with the blob.
     */
    private String contentDisposition;

    /**
     * The content-encoding header returned with the blob.
     */
    private String contentEncoding;

    /**
     * The content-language header returned with the blob.
     */
    private String contentLanguage;

    /**
     * The content-type header returned with the blob.
     */
    private String contentType;

    /**
     * Initializes a new instance of the {@link SharedAccessBlobHeaders} class.
     */
    public SharedAccessBlobHeaders() {

    }

    /**
     * Initializes a new instance of the {@link SharedAccessBlobHeaders} class based on an existing instance.
     * 
     * @param other
     *        A {@link SharedAccessBlobHeaders} object which specifies the set of blob properties to clone.
     */
    public SharedAccessBlobHeaders(SharedAccessBlobHeaders other) {
        Utility.assertNotNull("other", other);

        this.contentType = other.contentType;
        this.contentDisposition = other.contentDisposition;
        this.contentEncoding = other.contentEncoding;
        this.contentLanguage = other.contentLanguage;
        this.cacheControl = other.cacheControl;
    }

    /**
     * Gets the cache control header.
     * 
     * @return A <code>String</code> which represents the cache control header.
     */
    public final String getCacheControl() {
        return cacheControl;
    }

    /**
     * Sets the cache control header.
     * 
     * @param cacheControl
     *        A <code>String</code> which specifies the cache control header.
     */
    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    /**
     * Gets the content disposition header.
     * 
     * @return A <code>String</code> which represents the content disposition header.
     */
    public final String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * Sets the content disposition header.
     * 
     * @param contentDisposition
     *        A <code>String</code> which specifies the content disposition header.
     */
    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    /**
     * Gets the content encoding header.
     * 
     * @return A <code>String</code> which represents the content encoding header.
     */
    public final String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Sets the content encoding header.
     * 
     * @param contentEncoding
     *        A <code>String</code> which specifies the content encoding header.
     */
    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     * Gets the content language header.
     * 
     * @return A <code>String</code> which represents the content language header.
     */
    public final String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * Sets the content language header.
     * 
     * @param contentLanguage
     *        A <code>String</code> which specifies the content language header.
     */
    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    /**
     * Gets the content type header.
     * 
     * @return A <code>String</code> which represents the content type header.
     */
    public final String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type header.
     * 
     * @param contentType
     *        A <code>String</code> which specifies the content type header.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
