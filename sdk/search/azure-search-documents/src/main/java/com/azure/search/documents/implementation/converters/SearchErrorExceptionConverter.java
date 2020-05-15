package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchErrorException;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchErrorException} and
 * {@link SearchErrorException} mismatch.
 */
public final class SearchErrorExceptionConverter {
    public static SearchErrorException convert(com.azure.search.documents.models.SearchErrorException obj) {
        return DefaultConverter.convert(obj, SearchErrorException.class);
    }

    public static com.azure.search.documents.models.SearchErrorException convert(SearchErrorException obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchErrorException.class);
    }
}
