package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchableField;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchableField} and
 * {@link SearchableField} mismatch.
 */
public final class SearchableFieldConverter {
    public static SearchableField convert(com.azure.search.documents.models.SearchableField obj) {
        return DefaultConverter.convert(obj, SearchableField.class);
    }

    public static com.azure.search.documents.models.SearchableField convert(SearchableField obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchableField.class);
    }
}
