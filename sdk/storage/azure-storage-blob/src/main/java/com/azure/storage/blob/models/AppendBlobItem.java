// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.implementation.util.ImplUtils;

import java.time.OffsetDateTime;

public class AppendBlobItem {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final byte[] contentMD5;
    private final boolean isServerEncrypted;
    private final String encryptionKeySha256;

    private String blobAppendOffset;
    private Integer blobCommittedBlockCount;

    public AppendBlobItem(AppendBlobCreateHeaders generatedHeaders) {
        this.eTag = generatedHeaders.eTag();
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.encryptionKeySha256();
    }

    public AppendBlobItem(AppendBlobAppendBlockHeaders generatedHeaders) {
        this.eTag = generatedHeaders.eTag();
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.encryptionKeySha256();
        this.blobAppendOffset = generatedHeaders.blobAppendOffset();
        this.blobCommittedBlockCount = generatedHeaders.blobCommittedBlockCount();
    }

    public AppendBlobItem(AppendBlobAppendBlockFromUrlHeaders generatedHeaders, String isServerEncryptedHeader) {
        this.eTag = generatedHeaders.eTag();
        this.lastModified = generatedHeaders.lastModified();
        this.contentMD5 = generatedHeaders.contentMD5();
        this.isServerEncrypted = Boolean.parseBoolean(isServerEncryptedHeader);
        this.encryptionKeySha256 = generatedHeaders.encryptionKeySha256();
        this.blobAppendOffset = generatedHeaders.blobAppendOffset();
        this.blobCommittedBlockCount = generatedHeaders.blobCommittedBlockCount();
    }

    /**
     * @return the eTag of the append blob
     */
    public String eTag() {
        return eTag;
    }

    /**
     * @return the time this append blob was last modified
     */
    public OffsetDateTime lastModified() {
        return lastModified;
    };

    /**
     * @return the encryption status of the append blob on the server
     */
    public boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * @return the key that was used to encrypt the append blob
     */
    public String encryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * @return the calculated MD5 of the append blob
     */
    public byte[] contentMD5() {
        return ImplUtils.clone(contentMD5);
    }

    /**
     * @return the offset of the append blob
     */
    public String blobAppendOffset() {
        return blobAppendOffset;
    }

    /**
     * @return the number of committed blocks in the append blob
     */
    public Integer blobCommittedBlockCount() {
        return blobCommittedBlockCount;
    }
}
