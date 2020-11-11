// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;

import java.time.OffsetDateTime;

/**
 * This class contains the properties about an append blob.
 */
@Immutable
public class AppendBlobItem {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final byte[] contentMd5;
    private final boolean isServerEncrypted;
    private final String encryptionKeySha256;
    private final String encryptionScope;
    private final String blobAppendOffset;
    private final Integer blobCommittedBlockCount;
    private final String versionId;

    /**
     * Constructs an {@link AppendBlobItem}.
     *
     * @param eTag ETag of the append blob.
     * @param lastModified Last modified time of the append blob.
     * @param contentMd5 Content MD5 of the append blob.
     * @param isServerEncrypted Flag indicating if the page blob is encrypted on the server.
     * @param encryptionKeySha256 The encryption key used to encrypt the page blob.
     * @param blobAppendOffset The offset at which the block was committed to the block blob.
     * @param blobCommittedBlockCount The number of committed blocks in the block blob.
     */
    public AppendBlobItem(final String eTag, final OffsetDateTime lastModified, final byte[] contentMd5,
        final boolean isServerEncrypted, final String encryptionKeySha256, final String blobAppendOffset,
        final Integer blobCommittedBlockCount) {
        this(eTag, lastModified, contentMd5, isServerEncrypted, encryptionKeySha256, null, blobAppendOffset,
            blobCommittedBlockCount);
    }

    /**
     * Constructs an {@link AppendBlobItem}.
     *
     * @param eTag ETag of the append blob.
     * @param lastModified Last modified time of the append blob.
     * @param contentMd5 Content MD5 of the append blob.
     * @param isServerEncrypted Flag indicating if the page blob is encrypted on the server.
     * @param encryptionKeySha256 The encryption key used to encrypt the append blob.
     * @param encryptionScope The encryption scope used to encrypt the append blob.
     * @param blobAppendOffset The offset at which the block was committed to the append blob.
     * @param blobCommittedBlockCount The number of committed blocks in the append blob.
     */
    public AppendBlobItem(final String eTag, final OffsetDateTime lastModified, final byte[] contentMd5,
        final boolean isServerEncrypted, final String encryptionKeySha256, final String encryptionScope,
        final String blobAppendOffset, final Integer blobCommittedBlockCount) {
        this(eTag, lastModified, contentMd5, isServerEncrypted, encryptionKeySha256, encryptionScope, blobAppendOffset,
            blobCommittedBlockCount, null);
    }

    /**
     * Constructs an {@link AppendBlobItem}.
     *
     * @param eTag ETag of the append blob.
     * @param lastModified Last modified time of the append blob.
     * @param contentMd5 Content MD5 of the append blob.
     * @param isServerEncrypted Flag indicating if the page blob is encrypted on the server.
     * @param encryptionKeySha256 The encryption key used to encrypt the append blob.
     * @param encryptionScope The encryption scope used to encrypt the append blob.
     * @param blobAppendOffset The offset at which the block was committed to the append blob.
     * @param blobCommittedBlockCount The number of committed blocks in the append blob.
     * @param versionId The version identifier of the append blob.
     */
    public AppendBlobItem(final String eTag, final OffsetDateTime lastModified, final byte[] contentMd5,
                          final boolean isServerEncrypted, final String encryptionKeySha256,
                          final String encryptionScope, final String blobAppendOffset,
                          final Integer blobCommittedBlockCount, final String versionId) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.contentMd5 = CoreUtils.clone(contentMd5);
        this.isServerEncrypted = isServerEncrypted;
        this.encryptionKeySha256 = encryptionKeySha256;
        this.encryptionScope = encryptionScope;
        this.blobAppendOffset = blobAppendOffset;
        this.blobCommittedBlockCount = blobCommittedBlockCount;
        this.versionId = versionId;
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
     * @return the encryption scope that was used to encrypt the append blob
     */
    public String getEncryptionScope() {
        return encryptionScope;
    }

    /**
     * @return the calculated MD5 of the append blob
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
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

    /**
     * @return the version identifier of the append blob
     */
    public String getVersionId() {
        return versionId;
    }
}
