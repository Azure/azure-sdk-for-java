// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.storage.blob.models.PageBlobItem;

import java.time.OffsetDateTime;

/**
 * Helper class to access private values of {@link PageBlobItem} across package boundaries.
 */
public final class PageBlobItemConstructorProxy {
    private static PageBlobItemConstructorAccessor accessor;

    private PageBlobItemConstructorProxy() {
    }

    /**
     * Type defining the methods to set the non-public properties of a {@link PageBlobItem}.
     */
    public interface PageBlobItemConstructorAccessor {
        /**
         * Creates a new instance of {@link PageBlobItem}.
         *
         * @param eTag ETag of the page blob.
         * @param lastModified Last modified time of the page blob.
         * @param contentMd5 Content MD5 of the page blob.
         * @param isServerEncrypted Flag indicating if the page blob is encrypted on the server.
         * @param encryptionKeySha256 The encryption key used to encrypt the page blob.
         * @param encryptionScope The encryption scope used to encrypt the page blob.
         * @param blobSequenceNumber The current sequence number for the page blob.
         * @param versionId The version identifier of the page blob.
         * @param contentCrc64 Content CRC64 of the page blob.
         * @return A new instance of {@link PageBlobItem}.
         */
        PageBlobItem create(String eTag, OffsetDateTime lastModified, byte[] contentMd5, Boolean isServerEncrypted,
            String encryptionKeySha256, String encryptionScope, Long blobSequenceNumber, String versionId,
            byte[] contentCrc64);
    }

    /**
     * The method called from {@link PageBlobItem} to set its accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final PageBlobItemConstructorAccessor accessor) {
        PageBlobItemConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link PageBlobItem}.
     *
     * @param eTag ETag of the page blob.
     * @param lastModified Last modified time of the page blob.
     * @param contentMd5 Content MD5 of the page blob.
     * @param isServerEncrypted Flag indicating if the page blob is encrypted on the server.
     * @param encryptionKeySha256 The encryption key used to encrypt the page blob.
     * @param encryptionScope The encryption scope used to encrypt the page blob.
     * @param blobSequenceNumber The current sequence number for the page blob.
     * @param versionId The version identifier of the page blob.
     * @param contentCrc64 Content CRC64 of the page blob.
     * @return A new instance of {@link PageBlobItem}.
     */
    public static PageBlobItem create(String eTag, OffsetDateTime lastModified, byte[] contentMd5,
        Boolean isServerEncrypted, String encryptionKeySha256, String encryptionScope, Long blobSequenceNumber,
        String versionId, byte[] contentCrc64) {

        if (accessor == null) {
            new PageBlobItem(null, null, null, false, null, null, null, null);
        }

        assert accessor != null;
        return accessor.create(eTag, lastModified, contentMd5, isServerEncrypted, encryptionKeySha256, encryptionScope,
            blobSequenceNumber, versionId, contentCrc64);
    }
}
