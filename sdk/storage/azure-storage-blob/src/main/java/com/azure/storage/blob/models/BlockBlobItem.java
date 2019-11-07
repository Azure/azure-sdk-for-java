// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;

import java.time.OffsetDateTime;

/**
 * This class contains the properties about a block blob.
 */
@Immutable
public class BlockBlobItem {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final byte[] contentMd5;
    private final Boolean isServerEncrypted;
    private final String encryptionKeySha256;

    /**
     * Constructs a {@link BlockBlobItem}.
     *
     * @param eTag ETag of the block blob.
     * @param lastModified Last modified time of the block blob.
     * @param contentMd5 Content MD5 of the block blob.
     * @param isServerEncrypted Flag indicating if the page blob is encrypted on the server.
     * @param encryptionKeySha256 The encryption key used to encrypt the page blob.
     */
    public BlockBlobItem(final String eTag, final OffsetDateTime lastModified, final byte[] contentMd5,
        final boolean isServerEncrypted, final String encryptionKeySha256) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.contentMd5 = CoreUtils.clone(contentMd5);
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
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }
}
