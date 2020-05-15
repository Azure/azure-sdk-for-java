package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.DataContainer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.DataContainer} and
 * {@link DataContainer} mismatch.
 */
public final class DataContainerConverter {
    public static DataContainer convert(com.azure.search.documents.models.DataContainer obj) {
        return DefaultConverter.convert(obj, DataContainer.class);
    }

    public static com.azure.search.documents.models.DataContainer convert(DataContainer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.DataContainer.class);
    }
}
