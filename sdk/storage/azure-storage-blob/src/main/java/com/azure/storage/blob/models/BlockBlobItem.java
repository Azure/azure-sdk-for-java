// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.implementation.util.ImplUtils;

import java.time.OffsetDateTime;

/**
 * This class contains the properties about a block blob.
 */
public class BlockBlobItem {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final byte[] contentMD5;
    private final Boolean isServerEncrypted;
    private final String encryptionKeySha256;

    /**
     * Constructs a {@link BlockBlobItem}.
     *
     * @param eTag ETag of the block blob.
     * @param lastModified Last modified time of the block blob.
     * @param contentMD5 Content MD5 of the block blob.
     * @param isServerEncrypted Flag indicating if the page blob is encrypted on the server.
     * @param encryptionKeySha256 The encryption key used to encrypt the page blob.
     */
    public BlockBlobItem(String eTag, OffsetDateTime lastModified, byte[] contentMD5, boolean isServerEncrypted,
        String encryptionKeySha256) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.contentMD5 = contentMD5;
        this.isServerEncrypted = isServerEncrypted;
        this.encryptionKeySha256 = encryptionKeySha256;
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
