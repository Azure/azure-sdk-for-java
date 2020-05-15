package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ValueFacetResult;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ValueFacetResult} and
 * {@link ValueFacetResult} mismatch.
 */
public final class ValueFacetResultConverter {
    public static ValueFacetResult convert(com.azure.search.documents.models.ValueFacetResult obj) {
        return DefaultConverter.convert(obj, ValueFacetResult.class);
    }

    public static com.azure.search.documents.models.ValueFacetResult convert(ValueFacetResult obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ValueFacetResult.class);
    }
}
