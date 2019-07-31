package com.azure.storage.blob.models;

import com.azure.core.implementation.util.ImplUtils;

import java.time.OffsetDateTime;

public class PageBlobItem {

    private OffsetDateTime lastModified;

    private byte[] contentMD5;

    private Boolean isServerEncrypted;

    private String encryptionKeySha256;

    private Long blobSequenceNumber;

    public PageBlobItem(PageBlobCreateHeaders generatedHeaders) {
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.encryptionKeySha256();
    }

    public PageBlobItem(PageBlobUploadPagesHeaders generatedHeaders) {
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.encryptionKeySha256();
        this.blobSequenceNumber = generatedHeaders.blobSequenceNumber();
    }

    public PageBlobItem(PageBlobUploadPagesFromURLHeaders generatedHeaders) {
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.blobSequenceNumber = generatedHeaders.blobSequenceNumber();
    }

    public PageBlobItem(PageBlobClearPagesHeaders generatedHeaders) {
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.blobSequenceNumber = generatedHeaders.blobSequenceNumber();
    }

    public PageBlobItem(PageBlobResizeHeaders generatedHeaders) {
        this.lastModified = generatedHeaders.lastModified();
        this.blobSequenceNumber = generatedHeaders.blobSequenceNumber();
    }

    public PageBlobItem(PageBlobUpdateSequenceNumberHeaders generatedHeaders) {
        this.lastModified = generatedHeaders.lastModified();
        this.blobSequenceNumber = generatedHeaders.blobSequenceNumber();
    }

    public OffsetDateTime lastModified() {
        return lastModified;
    };

    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    public String encryptionKeySha256() {
        return encryptionKeySha256;
    }

    public byte[] contentMD5() {
        return ImplUtils.clone(contentMD5);
    }

    public Long blobSequenceNumber() {
        return blobSequenceNumber;
    }
}
