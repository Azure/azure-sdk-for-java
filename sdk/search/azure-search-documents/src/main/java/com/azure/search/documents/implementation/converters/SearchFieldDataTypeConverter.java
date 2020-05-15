package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchFieldDataType;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchFieldDataType} and
 * {@link SearchFieldDataType} mismatch.
 */
public final class SearchFieldDataTypeConverter {
    public static SearchFieldDataType convert(com.azure.search.documents.models.SearchFieldDataType obj) {
        return DefaultConverter.convert(obj, SearchFieldDataType.class);
    }

    public static com.azure.search.documents.models.SearchFieldDataType convert(SearchFieldDataType obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchFieldDataType.class);
    }
}
