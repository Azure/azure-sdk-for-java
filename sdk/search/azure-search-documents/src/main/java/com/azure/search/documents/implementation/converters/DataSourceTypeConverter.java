package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.DataSourceType;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.DataSourceType} and
 * {@link DataSourceType} mismatch.
 */
public final class DataSourceTypeConverter {
    public static DataSourceType convert(com.azure.search.documents.models.DataSourceType obj) {
        return DefaultConverter.convert(obj, DataSourceType.class);
    }

    public static com.azure.search.documents.models.DataSourceType convert(DataSourceType obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.DataSourceType.class);
    }
}
