// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.Metadata;

public class BlobProperties {

    private final BlobType blobType;

    private final Metadata metadata;

    private final long blobSize;

    private final byte[] contentMD5;

    private final String contentEncoding;

    private final String contentDisposition;

    private final String contentLanguage;

    private final String cacheControl;

    //todo decide datetime representation for last modified time


    BlobProperties(BlobGetPropertiesHeaders generatedHeaders) {
        this.blobType = generatedHeaders.blobType();
        this.metadata = new Metadata(generatedHeaders.metadata());
        this.blobSize = generatedHeaders.contentLength() == null ? 0 : generatedHeaders.contentLength();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.contentEncoding = generatedHeaders.contentEncoding();
        this.contentDisposition = generatedHeaders.contentDisposition();
        this.contentLanguage = generatedHeaders.contentLanguage();
        this.cacheControl = generatedHeaders.cacheControl();
    }


    /**
     * @return the blob type
     */
    public BlobType blobType() {
        return blobType;
    }

    /**
     * @return the metadata associated with this blob
     */
    public Metadata metadata() {
        return metadata;
    }

    /**
     * @return the size of the blob in bytes
     */
    public long blobSize() {
        return blobSize;
    }

    /**
     * @return the MD5 of the blob's content
     */
    public byte[] contentMD5() {
        return contentMD5;
    }

    /**
     * @return the encoding of the blob's content
     */
    public String contentEncoding() {
        return contentEncoding;
    }

    /**
     * @return the disposition of the blob's content
     */
    public String contentDisposition() {
        return contentDisposition;
    }

    /**
     * @return the language of the blob's content
     */
    public String contentLanguage() {
        return contentLanguage;
    }

    /**
     * @return the caching control for the blob
     */
    public String cacheControl() {
        return cacheControl;
    }
}
