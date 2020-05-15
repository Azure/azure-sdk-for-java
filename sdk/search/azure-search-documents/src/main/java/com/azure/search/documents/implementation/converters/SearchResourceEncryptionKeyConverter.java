package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchResourceEncryptionKey;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchResourceEncryptionKey} and
 * {@link SearchResourceEncryptionKey} mismatch.
 */
public final class SearchResourceEncryptionKeyConverter {
    public static SearchResourceEncryptionKey convert(com.azure.search.documents.models.SearchResourceEncryptionKey obj) {
        return DefaultConverter.convert(obj, SearchResourceEncryptionKey.class);
    }

    public static com.azure.search.documents.models.SearchResourceEncryptionKey convert(SearchResourceEncryptionKey obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchResourceEncryptionKey.class);
    }
}
