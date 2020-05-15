package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.InputFieldMappingEntry;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.InputFieldMappingEntry} and
 * {@link InputFieldMappingEntry} mismatch.
 */
public final class InputFieldMappingEntryConverter {
    public static InputFieldMappingEntry convert(com.azure.search.documents.models.InputFieldMappingEntry obj) {
        return DefaultConverter.convert(obj, InputFieldMappingEntry.class);
    }

    public static com.azure.search.documents.models.InputFieldMappingEntry convert(InputFieldMappingEntry obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.InputFieldMappingEntry.class);
    }
}
