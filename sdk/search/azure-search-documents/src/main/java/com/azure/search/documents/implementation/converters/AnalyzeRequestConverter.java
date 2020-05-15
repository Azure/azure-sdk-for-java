package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.AnalyzeRequest;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.AnalyzeRequest} and
 * {@link AnalyzeRequest} mismatch.
 */
public final class AnalyzeRequestConverter {
    public static AnalyzeRequest convert(com.azure.search.documents.models.AnalyzeRequest obj) {
        return DefaultConverter.convert(obj, AnalyzeRequest.class);
    }

    public static com.azure.search.documents.models.AnalyzeRequest convert(AnalyzeRequest obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.AnalyzeRequest.class);
    }
}
