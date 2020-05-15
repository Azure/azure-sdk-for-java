package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.RangeFacetResult;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.RangeFacetResult} and
 * {@link RangeFacetResult} mismatch.
 */
public final class RangeFacetResultConverter {
    public static RangeFacetResult convert(com.azure.search.documents.models.RangeFacetResult obj) {
        return DefaultConverter.convert(obj, RangeFacetResult.class);
    }

    public static com.azure.search.documents.models.RangeFacetResult convert(RangeFacetResult obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.RangeFacetResult.class);
    }
}
