package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchField;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchField} and
 * {@link SearchField} mismatch.
 */
public final class SearchFieldConverter {
    public static SearchField convert(com.azure.search.documents.models.SearchField obj) {
        return DefaultConverter.convert(obj, SearchField.class)
            .setRetrievable(obj.isHidden() == null ? null : !obj.isHidden());
    }

    public static com.azure.search.documents.models.SearchField convert(SearchField obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchField.class)
            .setHidden(obj.isRetrievable() == null ? null : !obj.isRetrievable());
    }
}
