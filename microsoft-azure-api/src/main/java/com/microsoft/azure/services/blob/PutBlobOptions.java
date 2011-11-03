package com.microsoft.azure.services.blob;

import java.util.HashMap;

public class PutBlobOptions {
    private String contentType;
    private String contentEncoding;
    private String contentLanguage;
    private String contentMD5;
    private String cacheControl;
    private String blobContentType;
    private String blobContentEncoding;
    private String blobContentLanguage;
    private String blobContentMD5;
    private String blobCacheControl;
    private HashMap<String, String> metadata = new HashMap<String, String>();
    private String leaseId;
    private Long sequenceNumber;

    public String getContentType() {
        return contentType;
    }

    public PutBlobOptions setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public PutBlobOptions setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    public String getContentLanguage() {
        return contentLanguage;
    }

    public PutBlobOptions setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
        return this;
    }

    public String getContentMD5() {
        return contentMD5;
    }

    public PutBlobOptions setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
        return this;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public PutBlobOptions setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }

    public String getBlobContentType() {
        return blobContentType;
    }

    public PutBlobOptions setBlobContentType(String blobContentType) {
        this.blobContentType = blobContentType;
        return this;
    }

    public String getBlobContentEncoding() {
        return blobContentEncoding;
    }

    public PutBlobOptions setBlobContentEncoding(String blobContentEncoding) {
        this.blobContentEncoding = blobContentEncoding;
        return this;
    }

    public String getBlobContentLanguage() {
        return blobContentLanguage;
    }

    public PutBlobOptions setBlobContentLanguage(String blobContentLanguage) {
        this.blobContentLanguage = blobContentLanguage;
        return this;
    }

    public String getBlobContentMD5() {
        return blobContentMD5;
    }

    public PutBlobOptions setBlobContentMD5(String blobContentMD5) {
        this.blobContentMD5 = blobContentMD5;
        return this;
    }

    public String getBlobCacheControl() {
        return blobCacheControl;
    }

    public PutBlobOptions setBlobCacheControl(String blobCacheControl) {
        this.blobCacheControl = blobCacheControl;
        return this;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public PutBlobOptions setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public PutBlobOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public PutBlobOptions setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }
}
