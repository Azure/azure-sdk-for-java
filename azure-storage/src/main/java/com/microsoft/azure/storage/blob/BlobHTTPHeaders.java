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
 * Most often used when creating a blob or setting its properties, this class contains fields for typical HTTP
 * properties, which, if specified, will be attached to the target blob. Null may be passed to any API which takes this
 * type to indicate that no properties should be set.
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
     *      The cache-control value stored for the blob.
     * @param contentDisposition
     *      The content-disposition value stored for the blob.
     * @param contentEncoding
     *      The content-encoding value stored for the blob.
     * @param contentLanguage
     *      The content-language value stored for the blob.
     * @param contentMD5
     *      The content-MD5 value stored for the blob.
     * @param contentType
     *      The content-type value stored for the blob.
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
     *      The cache-control value stored for the blob.
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * @return
     *      The content-disposition value stored for the blob.
     */
    public String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * @return
     *      The content-encoding value stored for the blob.
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * @return
     *      The content-language value stored for the blob.
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * @return
     *      The content-MD5 value stored for the blob.
     */
    public String getContentMD5() {
        return contentMD5;
    }

    /**
     * @return
     *      The content-type value stored for the blob.
     */
    public String getContentType() {
        return contentType;
    }

}
