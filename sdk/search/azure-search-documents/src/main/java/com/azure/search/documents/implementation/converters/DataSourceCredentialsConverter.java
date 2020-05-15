package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.DataSourceCredentials;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.DataSourceCredentials} and
 * {@link DataSourceCredentials} mismatch.
 */
public final class DataSourceCredentialsConverter {
    public static DataSourceCredentials convert(com.azure.search.documents.models.DataSourceCredentials obj) {
        return DefaultConverter.convert(obj, DataSourceCredentials.class);
    }

    public static com.azure.search.documents.models.DataSourceCredentials convert(DataSourceCredentials obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.DataSourceCredentials.class);
    }
}
