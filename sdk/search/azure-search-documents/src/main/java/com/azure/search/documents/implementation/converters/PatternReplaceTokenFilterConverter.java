package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.PatternReplaceTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.PatternReplaceTokenFilter} and
 * {@link PatternReplaceTokenFilter} mismatch.
 */
public final class PatternReplaceTokenFilterConverter {
    public static PatternReplaceTokenFilter convert(com.azure.search.documents.models.PatternReplaceTokenFilter obj) {
        return DefaultConverter.convert(obj, PatternReplaceTokenFilter.class);
    }

    public static com.azure.search.documents.models.PatternReplaceTokenFilter convert(PatternReplaceTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.PatternReplaceTokenFilter.class);
    }
}
