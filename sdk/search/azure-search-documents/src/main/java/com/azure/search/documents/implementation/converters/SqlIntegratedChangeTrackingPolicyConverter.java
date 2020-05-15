package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SqlIntegratedChangeTrackingPolicy;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SqlIntegratedChangeTrackingPolicy} and
 * {@link SqlIntegratedChangeTrackingPolicy} mismatch.
 */
public final class SqlIntegratedChangeTrackingPolicyConverter {
    public static SqlIntegratedChangeTrackingPolicy convert(com.azure.search.documents.models.SqlIntegratedChangeTrackingPolicy obj) {
        return DefaultConverter.convert(obj, SqlIntegratedChangeTrackingPolicy.class);
    }

    public static com.azure.search.documents.models.SqlIntegratedChangeTrackingPolicy convert(SqlIntegratedChangeTrackingPolicy obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SqlIntegratedChangeTrackingPolicy.class);
    }
}
