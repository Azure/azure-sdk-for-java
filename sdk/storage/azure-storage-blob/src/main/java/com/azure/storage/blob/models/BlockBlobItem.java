// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.blob.implementation.BlockBlobCommitBlockListHeaders;
import com.azure.storage.blob.implementation.BlockBlobUploadHeaders;

import java.time.OffsetDateTime;

public class BlockBlobItem {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final byte[] contentMD5;
    private final Boolean isServerEncrypted;
    private final String encryptionKeySha256;

    public BlockBlobItem(BlockBlobUploadHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
        this.contentMD5 = generatedHeaders.getContentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.getEncryptionKeySha256();
    }

    public BlockBlobItem(BlockBlobCommitBlockListHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModified = generatedHeaders.getLastModified();
        this.contentMD5 = generatedHeaders.getContentMD5();
        this.isServerEncrypted = generatedHeaders.isServerEncrypted();
        this.encryptionKeySha256 = generatedHeaders.getEncryptionKeySha256();
    }

    /**
     * @return the eTag of the block blob
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the last time the block blob was modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return the encryption status of the block blob on the server
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * @return the key used to encrypt the block blob
     */
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * @return the MD5 of the block blob's comment
     */
    public byte[] getContentMD5() {
        return ImplUtils.clone(contentMD5);
    }
}
