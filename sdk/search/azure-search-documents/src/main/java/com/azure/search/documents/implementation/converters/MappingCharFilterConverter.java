package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.MappingCharFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.MappingCharFilter} and
 * {@link MappingCharFilter} mismatch.
 */
public final class MappingCharFilterConverter {
    public static MappingCharFilter convert(com.azure.search.documents.models.MappingCharFilter obj) {
        return DefaultConverter.convert(obj, MappingCharFilter.class);
    }

    public static com.azure.search.documents.models.MappingCharFilter convert(MappingCharFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.MappingCharFilter.class);
    }
}
