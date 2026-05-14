// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.storage.blob.models.AppendBlobItem;

import java.time.OffsetDateTime;

/**
 * Helper class to access private values of {@link AppendBlobItem} across package boundaries.
 */
public final class AppendBlobItemConstructorProxy {
    private static AppendBlobItemConstructorAccessor accessor;

    private AppendBlobItemConstructorProxy() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link AppendBlobItem}.
     */
    public interface AppendBlobItemConstructorAccessor {
        /**
         * Creates a new instance of {@link AppendBlobItem}.
         *
         * @param eTag ETag of the append blob.
         * @param lastModified Last modified time of the append blob.
         * @param contentMd5 Content MD5 of the append blob.
         * @param isServerEncrypted Flag indicating if the append blob is encrypted on the server.
         * @param encryptionKeySha256 The encryption key used to encrypt the append blob.
         * @param encryptionScope The encryption scope used to encrypt the append blob.
         * @param blobAppendOffset The offset at which the block was committed to the append blob.
         * @param blobCommittedBlockCount The number of committed blocks in the append blob.
         * @param versionId The version identifier of the append blob.
         * @param contentCrc64 Content CRC64 of the append blob.
         * @return A new instance of {@link AppendBlobItem}.
         */
        AppendBlobItem create(String eTag, OffsetDateTime lastModified, byte[] contentMd5, boolean isServerEncrypted,
            String encryptionKeySha256, String encryptionScope, String blobAppendOffset,
            Integer blobCommittedBlockCount, String versionId, byte[] contentCrc64);
    }

    /**
     * The method called from {@link AppendBlobItem} to set its accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final AppendBlobItemConstructorAccessor accessor) {
        AppendBlobItemConstructorProxy.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link AppendBlobItem}.
     *
     * @param eTag ETag of the append blob.
     * @param lastModified Last modified time of the append blob.
     * @param contentMd5 Content MD5 of the append blob.
     * @param isServerEncrypted Flag indicating if the append blob is encrypted on the server.
     * @param encryptionKeySha256 The encryption key used to encrypt the append blob.
     * @param encryptionScope The encryption scope used to encrypt the append blob.
     * @param blobAppendOffset The offset at which the block was committed to the append blob.
     * @param blobCommittedBlockCount The number of committed blocks in the append blob.
     * @param versionId The version identifier of the append blob.
     * @param contentCrc64 Content CRC64 of the append blob.
     * @return A new instance of {@link AppendBlobItem}.
     */
    public static AppendBlobItem create(String eTag, OffsetDateTime lastModified, byte[] contentMd5,
        boolean isServerEncrypted, String encryptionKeySha256, String encryptionScope, String blobAppendOffset,
        Integer blobCommittedBlockCount, String versionId, byte[] contentCrc64) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses AppendBlobItem which triggers the accessor to be configured. So, if the accessor is null
        // this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new AppendBlobItem(null, null, null, false, null, null, null, null, null);
        }

        assert accessor != null;
        return accessor.create(eTag, lastModified, contentMd5, isServerEncrypted, encryptionKeySha256, encryptionScope,
            blobAppendOffset, blobCommittedBlockCount, versionId, contentCrc64);
    }
}
