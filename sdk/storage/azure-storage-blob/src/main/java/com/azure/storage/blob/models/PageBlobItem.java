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
        this.eTag = generatedHeaders.eTag();
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.encryptionKeySha256();
    }

    public PageBlobItem(PageBlobUploadPagesHeaders generatedHeaders) {
        this.eTag = generatedHeaders.eTag();
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.encryptionKeySha256();
        this.blobSequenceNumber = generatedHeaders.blobSequenceNumber();
    }

    public PageBlobItem(PageBlobUploadPagesFromURLHeaders generatedHeaders, String encryptionKeySha256Header) {
        this.eTag = generatedHeaders.eTag();
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = encryptionKeySha256Header;
        this.blobSequenceNumber = generatedHeaders.blobSequenceNumber();
    }

    public PageBlobItem(PageBlobClearPagesHeaders generatedHeaders, String isServerEncryptedHeader,
        String encryptionKeySha256Header) {
        this.eTag = generatedHeaders.eTag();
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = Boolean.parseBoolean(isServerEncryptedHeader);
        this.encryptionKeySha256 = encryptionKeySha256Header;
        this.blobSequenceNumber = generatedHeaders.blobSequenceNumber();
    }

    public PageBlobItem(PageBlobResizeHeaders generatedHeaders) {
        this.eTag = generatedHeaders.eTag();
        this.lastModified = generatedHeaders.lastModified();
        this.blobSequenceNumber = generatedHeaders.blobSequenceNumber();
    }

    public PageBlobItem(PageBlobUpdateSequenceNumberHeaders generatedHeaders) {
        this.eTag = generatedHeaders.eTag();
        this.lastModified = generatedHeaders.lastModified();
        this.blobSequenceNumber = generatedHeaders.blobSequenceNumber();
    }

    /**
     * @return the eTag of the page blob
     */
    public String eTag() {
        return eTag;
    }

    /**
     * @return the time this page blob was last modified
     */
    public OffsetDateTime lastModified() {
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
    public String encryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * @return the MD5 of the page blob's content
     */
    public byte[] contentMD5() {
        return ImplUtils.clone(contentMD5);
    }

    /**
     * @return the current sequence number of the page blob
     */
    public Long blobSequenceNumber() {
        return blobSequenceNumber;
    }
}
