// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.storage.blob.models.BlockBlobItem;

import java.time.OffsetDateTime;

/**
 * Helper class to access private values of {@link BlockBlobItem} across package boundaries.
 */
public final class BlockBlobItemConstructorProxy {
    private static BlockBlobItemConstructorAccessor accessor;

    private BlockBlobItemConstructorProxy() {
    }

    /**
     * Type defining the methods to set the non-public properties of a {@link BlockBlobItem}.
     */
    public interface BlockBlobItemConstructorAccessor {
        /**
         * Creates a new instance of {@link BlockBlobItem}.
         *
         * @param eTag ETag of the block blob.
         * @param lastModified Last modified time of the block blob.
         * @param contentMd5 Content MD5 of the block blob.
         * @param isServerEncrypted Flag indicating if the block blob is encrypted on the server.
         * @param encryptionKeySha256 The encryption key used to encrypt the block blob.
         * @param encryptionScope The encryption scope used to encrypt the block blob.
         * @param versionId The version identifier of the block blob.
         * @param contentCrc64 Content CRC64 of the block blob.
         * @return A new instance of {@link BlockBlobItem}.
         */
        BlockBlobItem create(String eTag, OffsetDateTime lastModified, byte[] contentMd5, Boolean isServerEncrypted,
            String encryptionKeySha256, String encryptionScope, String versionId, byte[] contentCrc64);
    }

    /**
     * The method called from {@link BlockBlobItem} to set its accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final BlockBlobItemConstructorAccessor accessor) {
        BlockBlobItemConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link BlockBlobItem}.
     *
     * @param eTag ETag of the block blob.
     * @param lastModified Last modified time of the block blob.
     * @param contentMd5 Content MD5 of the block blob.
     * @param isServerEncrypted Flag indicating if the block blob is encrypted on the server.
     * @param encryptionKeySha256 The encryption key used to encrypt the block blob.
     * @param encryptionScope The encryption scope used to encrypt the block blob.
     * @param versionId The version identifier of the block blob.
     * @param contentCrc64 Content CRC64 of the block blob.
     * @return A new instance of {@link BlockBlobItem}.
     */
    public static BlockBlobItem create(String eTag, OffsetDateTime lastModified, byte[] contentMd5,
        Boolean isServerEncrypted, String encryptionKeySha256, String encryptionScope, String versionId,
        byte[] contentCrc64) {

        if (accessor == null) {
            new BlockBlobItem(null, null, null, (Boolean) null, null, null, null);
        }

        assert accessor != null;
        return accessor.create(eTag, lastModified, contentMd5, isServerEncrypted, encryptionKeySha256, encryptionScope,
            versionId, contentCrc64);
    }
}
