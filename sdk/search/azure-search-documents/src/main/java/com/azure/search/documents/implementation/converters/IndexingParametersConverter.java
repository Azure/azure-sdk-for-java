package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.IndexingParameters;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.IndexingParameters} and
 * {@link IndexingParameters} mismatch.
 */
public final class IndexingParametersConverter {
    public static IndexingParameters convert(com.azure.search.documents.models.IndexingParameters obj) {
        return DefaultConverter.convert(obj, IndexingParameters.class);
    }

    public static com.azure.search.documents.models.IndexingParameters convert(IndexingParameters obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.IndexingParameters.class);
    }
}
