package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.RequestOptions;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.RequestOptions} and
 * {@link RequestOptions} mismatch.
 */
public final class RequestOptionsConverter {
    public static RequestOptions convert(com.azure.search.documents.models.RequestOptions obj) {
        return DefaultConverter.convert(obj, RequestOptions.class);
    }

    public static com.azure.search.documents.models.RequestOptions convert(RequestOptions obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.RequestOptions.class);
    }
}
