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
package com.microsoft.windowsazure.storage.blob;

import com.microsoft.windowsazure.storage.core.Utility;

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
     *            The set of blob properties to clone.
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
     * @return the cacheControl
     */
    public final String getCacheControl() {
        return cacheControl;
    }

    /**
     * @param cacheControl
     *            The cacheControl to set.
     */
    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    /**
     * @return the contentDisposition
     */
    public final String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * @param contentDisposition
     *            The contentDisposition to set.
     */
    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    /**
     * @return the contentEncoding
     */
    public final String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * @param contentEncoding
     *            The contentEncoding to set.
     */
    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     * @return the contentLanguage
     */
    public final String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * @param contentLanguage
     *            The contentLanguage to set.
     */
    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    /**
     * @return the contentType
     */
    public final String getContentType() {
        return contentType;
    }

    /**
     * @param contentType
     *            The contentType to set.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
