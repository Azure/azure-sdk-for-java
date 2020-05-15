package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.EncryptionKey;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.EncryptionKey} and
 * {@link EncryptionKey} mismatch.
 */
public final class EncryptionKeyConverter {
    public static EncryptionKey convert(com.azure.search.documents.models.EncryptionKey obj) {
        return DefaultConverter.convert(obj, EncryptionKey.class);
    }

    public static com.azure.search.documents.models.EncryptionKey convert(EncryptionKey obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.EncryptionKey.class);
    }
}
