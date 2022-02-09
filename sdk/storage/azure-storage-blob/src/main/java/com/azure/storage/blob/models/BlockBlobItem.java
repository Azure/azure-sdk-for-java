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
    private final String encryptionScope;
    private final String versionId;

    /**
     * Constructs a {@link BlockBlobItem}.
     *
     * @param eTag ETag of the block blob.
     * @param lastModified Last modified time of the block blob.
     * @param contentMd5 Content MD5 of the block blob.
     * @param isServerEncrypted Flag indicating if the block blob is encrypted on the server.
     * @param encryptionKeySha256 The encryption key used to encrypt the block blob.
     */
    public BlockBlobItem(final String eTag, final OffsetDateTime lastModified, final byte[] contentMd5,
        final boolean isServerEncrypted, final String encryptionKeySha256) {
        this(eTag, lastModified, contentMd5, isServerEncrypted, encryptionKeySha256, null);
    }

    /**
     * Constructs a {@link BlockBlobItem}.
     *
     * @param eTag ETag of the block blob.
     * @param lastModified Last modified time of the block blob.
     * @param contentMd5 Content MD5 of the block blob.
     * @param isServerEncrypted Flag indicating if the block blob is encrypted on the server.
     * @param encryptionKeySha256 The encryption key used to encrypt the block blob.
     * @param encryptionScope The encryption scope used to encrypt the block blob.
     */
    public BlockBlobItem(final String eTag, final OffsetDateTime lastModified, final byte[] contentMd5,
        final boolean isServerEncrypted, final String encryptionKeySha256, final String encryptionScope) {
        this(eTag, lastModified, contentMd5, isServerEncrypted, encryptionKeySha256, encryptionScope, null);
    }

    /**
     * Constructs a {@link BlockBlobItem}.
     *
     * @param eTag ETag of the block blob.
     * @param lastModified Last modified time of the block blob.
     * @param contentMd5 Content MD5 of the block blob.
     * @param isServerEncrypted Flag indicating if the block blob is encrypted on the server.
     * @param encryptionKeySha256 The encryption key used to encrypt the block blob.
     * @param encryptionScope The encryption scope used to encrypt the block blob.
     * @param versionId The version identifier of the block blob.
     */
    public BlockBlobItem(final String eTag, final OffsetDateTime lastModified, final byte[] contentMd5,
                         final boolean isServerEncrypted, final String encryptionKeySha256,
                         final String encryptionScope, final String versionId) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.contentMd5 = CoreUtils.clone(contentMd5);
        this.isServerEncrypted = isServerEncrypted;
        this.encryptionKeySha256 = encryptionKeySha256;
        this.encryptionScope = encryptionScope;
        this.versionId = versionId;
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
     * @return the encryption scope used to encrypt the block blob
     */
    public String getEncryptionScope() {
        return encryptionScope;
    }

    /**
     * @return the MD5 of the block blob's comment
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * @return the version identifier of the block blob
     */
    public String getVersionId() {
        return versionId;
    }
}
