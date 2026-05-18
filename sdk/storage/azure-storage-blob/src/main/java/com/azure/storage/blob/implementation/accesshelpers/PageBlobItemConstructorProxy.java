package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.storage.blob.models.PageBlobItem;

import java.time.OffsetDateTime;

public class PageBlobItemConstructorProxy {
    private static PageBlobItemConstructorAccessor accessor;

    public interface PageBlobItemConstructorAccessor {
        PageBlobItem create(final String eTag, final OffsetDateTime lastModified, final byte[] contentMd5,
            final Boolean isServerEncrypted, final String encryptionKeySha256, final String encryptionScope,
            final Long blobSequenceNumber, final String versionId, final byte[] contentCrc64);
    }

    public static void setAccessor(final PageBlobItemConstructorAccessor accessor) {
        PageBlobItemConstructorProxy.accessor = accessor;
    }

    public static PageBlobItem create(final String eTag, final OffsetDateTime lastModified, final byte[] contentMd5,
        final Boolean isServerEncrypted, final String encryptionKeySha256, final String encryptionScope,
        final Long blobSequenceNumber, final String versionId, final byte[] contentCrc64) {
        if (accessor == null) {
            new PageBlobItem(null, null, null, false, null, null, null, null);
        }

        assert accessor != null;
        return accessor.create(eTag, lastModified, contentMd5, isServerEncrypted, encryptionKeySha256, encryptionScope,
            blobSequenceNumber, versionId, contentCrc64);
    }
}
