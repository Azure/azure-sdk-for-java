package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.FieldMapping;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.FieldMapping} and
 * {@link FieldMapping} mismatch.
 */
public final class FieldMappingConverter {
    public static FieldMapping convert(com.azure.search.documents.models.FieldMapping obj) {
        return DefaultConverter.convert(obj, FieldMapping.class);
    }

    public static com.azure.search.documents.models.FieldMapping convert(FieldMapping obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.FieldMapping.class);
    }
}
