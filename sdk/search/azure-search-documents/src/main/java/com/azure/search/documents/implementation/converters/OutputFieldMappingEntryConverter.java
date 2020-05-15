package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.OutputFieldMappingEntry;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.OutputFieldMappingEntry} and
 * {@link OutputFieldMappingEntry} mismatch.
 */
public final class OutputFieldMappingEntryConverter {
    public static OutputFieldMappingEntry convert(com.azure.search.documents.models.OutputFieldMappingEntry obj) {
        return DefaultConverter.convert(obj, OutputFieldMappingEntry.class);
    }

    public static com.azure.search.documents.models.OutputFieldMappingEntry convert(OutputFieldMappingEntry obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.OutputFieldMappingEntry.class);
    }
}
