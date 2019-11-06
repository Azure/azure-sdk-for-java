// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;

import java.time.OffsetDateTime;

/**
 * This class contains the properties about a page blob.
 */
@Immutable
public class PageBlobItem {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final byte[] contentMd5;
    private final Boolean isServerEncrypted;
    private final String encryptionKeySha256;
    private final Long blobSequenceNumber;

    /**
     * Constructs a {@link PageBlobItem}.
     *
     * @param eTag ETag of the page blob.
     * @param lastModified Last modified time of the page blob.
     * @param contentMd5 Content MD5 of the page blob.
     * @param isServerEncrypted Flag indicating if the page blob is encrypted on the server.
     * @param encryptionKeySha256 The encryption key used to encrypt the page blob.
     * @param blobSequenceNumber The current sequence number for the page blob.
     */
    public PageBlobItem(final String eTag, final OffsetDateTime lastModified, final byte[] contentMd5,
        final Boolean isServerEncrypted, final String encryptionKeySha256, final Long blobSequenceNumber) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.contentMd5 = CoreUtils.clone(contentMd5);
        this.isServerEncrypted = isServerEncrypted;
        this.encryptionKeySha256 = encryptionKeySha256;
        this.blobSequenceNumber = blobSequenceNumber;
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
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * @return the current sequence number of the page blob
     */
    public Long getBlobSequenceNumber() {
        return blobSequenceNumber;
    }
}
