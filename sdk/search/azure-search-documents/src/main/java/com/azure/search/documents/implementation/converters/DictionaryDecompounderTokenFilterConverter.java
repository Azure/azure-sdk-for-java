package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.DictionaryDecompounderTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.DictionaryDecompounderTokenFilter} and
 * {@link DictionaryDecompounderTokenFilter} mismatch.
 */
public final class DictionaryDecompounderTokenFilterConverter {
    public static DictionaryDecompounderTokenFilter convert(com.azure.search.documents.models.DictionaryDecompounderTokenFilter obj) {
        return DefaultConverter.convert(obj, DictionaryDecompounderTokenFilter.class);
    }

    public static com.azure.search.documents.models.DictionaryDecompounderTokenFilter convert(DictionaryDecompounderTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.DictionaryDecompounderTokenFilter.class);
    }
}
