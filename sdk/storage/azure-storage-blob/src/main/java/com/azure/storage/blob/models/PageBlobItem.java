// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.implementation.util.ImplUtils;

import java.time.OffsetDateTime;

public class PageBlobItem {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private byte[] contentMD5;
    private Boolean isServerEncrypted;
    private String encryptionKeySha256;
    private Long blobSequenceNumber;

    public PageBlobItem(PageBlobCreateHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
        this.contentMD5 = generatedHeaders.getContentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.getEncryptionKeySha256();
    }

    public PageBlobItem(PageBlobUploadPagesHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
        this.contentMD5 = generatedHeaders.getContentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.getEncryptionKeySha256();
        this.blobSequenceNumber = generatedHeaders.getBlobSequenceNumber();
    }

    public PageBlobItem(PageBlobUploadPagesFromURLHeaders generatedHeaders, String encryptionKeySha256Header) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
        this.contentMD5 = generatedHeaders.getContentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = encryptionKeySha256Header;
        this.blobSequenceNumber = generatedHeaders.getBlobSequenceNumber();
    }

    public PageBlobItem(PageBlobClearPagesHeaders generatedHeaders, String isServerEncryptedHeader,
        String encryptionKeySha256Header) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
        this.contentMD5 = generatedHeaders.getContentMD5();
        this.isServerEncrypted = Boolean.parseBoolean(isServerEncryptedHeader);
        this.encryptionKeySha256 = encryptionKeySha256Header;
        this.blobSequenceNumber = generatedHeaders.getBlobSequenceNumber();
    }

    public PageBlobItem(PageBlobResizeHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
        this.blobSequenceNumber = generatedHeaders.getBlobSequenceNumber();
    }

    public PageBlobItem(PageBlobUpdateSequenceNumberHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
        this.blobSequenceNumber = generatedHeaders.getBlobSequenceNumber();
    }

    /**
     * @return the eTag of the page blob
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the time this page blob was last modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return the encryption status of the page blob on the server
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * @return the key used to encrypt the page blob
     */
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * @return the MD5 of the page blob's content
     */
    public byte[] getContentMD5() {
        return ImplUtils.clone(contentMD5);
    }

    /**
     * @return the current sequence number of the page blob
     */
    public Long getBlobSequenceNumber() {
        return blobSequenceNumber;
    }
}
