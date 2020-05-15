package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SimpleField;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SimpleField} and
 * {@link SimpleField} mismatch.
 */
public final class SimpleFieldConverter {
    public static SimpleField convert(com.azure.search.documents.models.SimpleField obj) {
        return DefaultConverter.convert(obj, SimpleField.class);
    }

    public static com.azure.search.documents.models.SimpleField convert(SimpleField obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SimpleField.class);
    }
}
