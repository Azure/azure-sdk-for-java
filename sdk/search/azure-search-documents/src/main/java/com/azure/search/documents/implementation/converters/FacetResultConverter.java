package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.FacetResult;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.FacetResult} and
 * {@link FacetResult} mismatch.
 */
public final class FacetResultConverter {
    public static FacetResult convert(com.azure.search.documents.models.FacetResult obj) {
        return DefaultConverter.convert(obj, FacetResult.class);
    }

    public static com.azure.search.documents.models.FacetResult convert(FacetResult obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.FacetResult.class);
    }
}
