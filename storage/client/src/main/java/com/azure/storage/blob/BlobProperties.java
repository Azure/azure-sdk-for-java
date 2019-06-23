package com.azure.storage.blob;

import com.azure.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.storage.blob.models.BlobType;

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


    public BlobType blobType() {
        return blobType;
    }

    public Metadata metadata() {
        return metadata;
    }

    public long blobSize() {
        return blobSize;
    }

    public byte[] contentMD5() {
        return contentMD5;
    }

    public String contentEncoding() {
        return contentEncoding;
    }

    public String contentDisposition() {
        return contentDisposition;
    }

    public String contentLanguage() {
        return contentLanguage;
    }

    public String cacheControl() {
        return cacheControl;
    }
}
