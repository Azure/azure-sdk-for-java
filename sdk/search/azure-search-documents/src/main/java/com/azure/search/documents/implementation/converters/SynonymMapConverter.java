package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SynonymMap;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SynonymMap} and
 * {@link SynonymMap} mismatch.
 */
public final class SynonymMapConverter {
    public static SynonymMap convert(com.azure.search.documents.models.SynonymMap obj) {
        return DefaultConverter.convert(obj, SynonymMap.class);
    }

    public static com.azure.search.documents.models.SynonymMap convert(SynonymMap obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SynonymMap.class);
    }
}
