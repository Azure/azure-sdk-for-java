package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.CorsOptions;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.CorsOptions} and
 * {@link CorsOptions} mismatch.
 */
public final class CorsOptionsConverter {
    public static CorsOptions convert(com.azure.search.documents.models.CorsOptions obj) {
        return DefaultConverter.convert(obj, CorsOptions.class);
    }

    public static com.azure.search.documents.models.CorsOptions convert(CorsOptions obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.CorsOptions.class);
    }
}
