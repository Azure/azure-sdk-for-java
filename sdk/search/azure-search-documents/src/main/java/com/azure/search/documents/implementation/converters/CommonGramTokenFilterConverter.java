package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.CommonGramTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.CommonGramTokenFilter} and
 * {@link CommonGramTokenFilter} mismatch.
 */
public final class CommonGramTokenFilterConverter {
    public static CommonGramTokenFilter convert(com.azure.search.documents.models.CommonGramTokenFilter obj) {
        return DefaultConverter.convert(obj, CommonGramTokenFilter.class);
    }

    public static com.azure.search.documents.models.CommonGramTokenFilter convert(CommonGramTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.CommonGramTokenFilter.class);
    }
}
