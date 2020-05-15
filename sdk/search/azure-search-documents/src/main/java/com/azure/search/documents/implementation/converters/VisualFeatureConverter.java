package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.VisualFeature;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.VisualFeature} and
 * {@link VisualFeature} mismatch.
 */
public final class VisualFeatureConverter {
    public static VisualFeature convert(com.azure.search.documents.models.VisualFeature obj) {
        return DefaultConverter.convert(obj, VisualFeature.class);
    }

    public static com.azure.search.documents.models.VisualFeature convert(VisualFeature obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.VisualFeature.class);
    }
}
