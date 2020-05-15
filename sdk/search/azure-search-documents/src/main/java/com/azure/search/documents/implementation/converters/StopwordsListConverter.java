package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.StopwordsList;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.StopwordsList} and
 * {@link StopwordsList} mismatch.
 */
public final class StopwordsListConverter {
    public static StopwordsList convert(com.azure.search.documents.models.StopwordsList obj) {
        return DefaultConverter.convert(obj, StopwordsList.class);
    }

    public static com.azure.search.documents.models.StopwordsList convert(StopwordsList obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.StopwordsList.class);
    }
}
