package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.PatternCaptureTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.PatternCaptureTokenFilter} and
 * {@link PatternCaptureTokenFilter} mismatch.
 */
public final class PatternCaptureTokenFilterConverter {
    public static PatternCaptureTokenFilter convert(com.azure.search.documents.models.PatternCaptureTokenFilter obj) {
        return DefaultConverter.convert(obj, PatternCaptureTokenFilter.class);
    }

    public static com.azure.search.documents.models.PatternCaptureTokenFilter convert(PatternCaptureTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.PatternCaptureTokenFilter.class);
    }
}
