package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.QueryType;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.QueryType} and
 * {@link QueryType} mismatch.
 */
public final class QueryTypeConverter {
    public static QueryType convert(com.azure.search.documents.models.QueryType obj) {
        return DefaultConverter.convert(obj, QueryType.class);
    }

    public static com.azure.search.documents.models.QueryType convert(QueryType obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.QueryType.class);
    }
}
