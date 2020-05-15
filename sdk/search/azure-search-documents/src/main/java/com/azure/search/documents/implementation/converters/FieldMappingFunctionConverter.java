package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.FieldMappingFunction;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.FieldMappingFunction} and
 * {@link FieldMappingFunction} mismatch.
 */
public final class FieldMappingFunctionConverter {
    public static FieldMappingFunction convert(com.azure.search.documents.models.FieldMappingFunction obj) {
        return DefaultConverter.convert(obj, FieldMappingFunction.class);
    }

    public static com.azure.search.documents.models.FieldMappingFunction convert(FieldMappingFunction obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.FieldMappingFunction.class);
    }
}
