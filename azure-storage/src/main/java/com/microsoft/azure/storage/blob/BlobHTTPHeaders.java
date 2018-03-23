/*
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

/**
 * Blob HTTP headers for getting and setting blob properties.
 */
public final class BlobHTTPHeaders {

    /**
     * An object representing no blob properties.
     */
    public static final BlobHTTPHeaders NONE = new BlobHTTPHeaders(null, null,
            null,null, null, null);

    private final String cacheControl;

    private final String contentDisposition;

    private final String contentEncoding;

    private final String contentLanguage;

    private final String contentMD5;

    private final String contentType;

    /**
     * A {@link BlobHTTPHeaders} object.
     *
     * @param cacheControl
     *      A {@code String} representing the cache-control value stored for the blob.
     *      If this field has not been set for the blob, this field returns {@code null}.
     * @param contentDisposition
     *      A {@code String} representing the content-disposition value stored for the blob.
     *      If this field has not been set for the blob, the field returns {@code null}.
     * @param contentEncoding
     *      A {@code String} the content-encoding value stored for the blob.
     *      If this field has not been set for the blob, the field returns {@code null}.
     * @param contentLanguage
     *      A {@code String} representing the content-language value stored for the blob.
     *      If this field has not been set for the blob, the field returns {@code null}.
     * @param contentMD5
     *      A {@code String} representing the content MD5 value stored for the blob.
     *      If this field has not been set for the blob, the field returns {@code null}.
     * @param contentType
     *      A {@code String} representing the content type value stored for the blob.
     *      If this field has not been set for the blob, the field returns {@code null}.
     */
    public BlobHTTPHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                           String contentLanguage, String contentMD5, String contentType) {
        this.cacheControl = cacheControl;
        this.contentDisposition = contentDisposition;
        this.contentEncoding = contentEncoding;
        this.contentLanguage = contentLanguage;
        this.contentMD5 = contentMD5;
        this.contentType = contentType;
    }

    /**
     * @return
     *      A {@code String} representing the cache-control value stored for the blob.
     *      If this field has not been set for the blob, the field returns {@code null}.
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * @return
     *      A {@code String} representing the content-disposition value stored for the blob.
     *      If this field has not been set for the blob, the field returns {@code null}.
     */
    public String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * @return
     *      A {@code String} the content-encoding value stored for the blob.
     *      If this field has not been set for the blob, the field returns {@code null}.
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * @return
     *      A {@code String} representing the content-language value stored for the blob.
     *      If this field has not been set for the blob, the field returns {@code null}.
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * @return
     *      A {@code String} representing the content MD5 value stored for the blob.
     *      If this field has not been set for the blob, the field returns {@code null}.
     */
    public String getContentMD5() {
        return contentMD5;
    }

    /**
     * @return
     *      A {@code String} representing the content type value stored for the blob.
     *      If this field has not been set for the blob, the field returns {@code null}.
     */
    public String getContentType() {
        return contentType;
    }

}
