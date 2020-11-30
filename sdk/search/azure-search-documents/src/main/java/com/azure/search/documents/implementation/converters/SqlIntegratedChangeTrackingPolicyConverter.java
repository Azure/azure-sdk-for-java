// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SqlIntegratedChangeTrackingPolicy;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SqlIntegratedChangeTrackingPolicy} and
 * {@link SqlIntegratedChangeTrackingPolicy}.
 */
public final class SqlIntegratedChangeTrackingPolicyConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SqlIntegratedChangeTrackingPolicy} to
     * {@link SqlIntegratedChangeTrackingPolicy}.
     */
    public static SqlIntegratedChangeTrackingPolicy map(com.azure.search.documents.indexes.implementation.models.SqlIntegratedChangeTrackingPolicy obj) {
        if (obj == null) {
            return null;
        }
        SqlIntegratedChangeTrackingPolicy sqlIntegratedChangeTrackingPolicy = new SqlIntegratedChangeTrackingPolicy();
        return sqlIntegratedChangeTrackingPolicy;
    }

    /**
     * Maps from {@link SqlIntegratedChangeTrackingPolicy} to
     * {@link com.azure.search.documents.indexes.implementation.models.SqlIntegratedChangeTrackingPolicy}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SqlIntegratedChangeTrackingPolicy map(SqlIntegratedChangeTrackingPolicy obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.SqlIntegratedChangeTrackingPolicy sqlIntegratedChangeTrackingPolicy = new com.azure.search.documents.indexes.implementation.models.SqlIntegratedChangeTrackingPolicy();
        return sqlIntegratedChangeTrackingPolicy;
    }

    private SqlIntegratedChangeTrackingPolicyConverter() {
    }
}
