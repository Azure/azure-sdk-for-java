// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.implementation.util.ImplUtils;

import java.time.OffsetDateTime;

/**
 * This class contains the properties about an append blob.
 */
public class AppendBlobItem {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final byte[] contentMD5;
    private final boolean isServerEncrypted;
    private final String encryptionKeySha256;
    private final String blobAppendOffset;
    private final Integer blobCommittedBlockCount;

    public AppendBlobItem(AppendBlobCreateHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
        this.contentMD5 = generatedHeaders.getContentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.getEncryptionKeySha256();
        this.blobAppendOffset = null;
        this.blobCommittedBlockCount = null;
    }

    public AppendBlobItem(AppendBlobAppendBlockHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
        this.contentMD5 = generatedHeaders.getContentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.getEncryptionKeySha256();
        this.blobAppendOffset = generatedHeaders.getBlobAppendOffset();
        this.blobCommittedBlockCount = generatedHeaders.getBlobCommittedBlockCount();
    }

    public AppendBlobItem(AppendBlobAppendBlockFromUrlHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
        this.contentMD5 = generatedHeaders.getContentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.getEncryptionKeySha256();
        this.blobAppendOffset = generatedHeaders.getBlobAppendOffset();
        this.blobCommittedBlockCount = generatedHeaders.getBlobCommittedBlockCount();
    }

    /**
     * @return the eTag of the append blob
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the time this append blob was last modified
     */
    public OffsetDateTime getLastModified() {
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
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * @return the calculated MD5 of the append blob
     */
    public byte[] getContentMD5() {
        return ImplUtils.clone(contentMD5);
    }

    /**
     * @return the offset of the append blob
     */
    public String getBlobAppendOffset() {
        return blobAppendOffset;
    }

    /**
     * @return the number of committed blocks in the append blob
     */
    public Integer getBlobCommittedBlockCount() {
        return blobCommittedBlockCount;
    }
}
