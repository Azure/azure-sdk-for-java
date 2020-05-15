package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.FieldBase;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.FieldBase} and
 * {@link FieldBase} mismatch.
 */
public final class FieldBaseConverter {
    public static FieldBase convert(com.azure.search.documents.models.FieldBase obj) {
        return DefaultConverter.convert(obj, FieldBase.class);
    }

    public static com.azure.search.documents.models.FieldBase convert(FieldBase obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.FieldBase.class);
    }
}
