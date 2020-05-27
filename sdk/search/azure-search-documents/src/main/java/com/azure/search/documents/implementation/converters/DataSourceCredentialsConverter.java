// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.DataSourceCredentials;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.DataSourceCredentials} and
 * {@link DataSourceCredentials}.
 */
public final class DataSourceCredentialsConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.DataSourceCredentials} to
     * {@link DataSourceCredentials}.
     */
    public static DataSourceCredentials map(com.azure.search.documents.indexes.implementation.models.DataSourceCredentials obj) {
        if (obj == null) {
            return null;
        }
        DataSourceCredentials dataSourceCredentials = new DataSourceCredentials();

        String connectionString = obj.getConnectionString();
        dataSourceCredentials.setConnectionString(connectionString);
        return dataSourceCredentials;
    }

    /**
     * Maps from {@link DataSourceCredentials} to
     * {@link com.azure.search.documents.indexes.implementation.models.DataSourceCredentials}.
     */
    public static com.azure.search.documents.indexes.implementation.models.DataSourceCredentials map(DataSourceCredentials obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.DataSourceCredentials dataSourceCredentials =
            new com.azure.search.documents.indexes.implementation.models.DataSourceCredentials();

        String connectionString = obj.getConnectionString();
        dataSourceCredentials.setConnectionString(connectionString);
        return dataSourceCredentials;
    }

    private DataSourceCredentialsConverter() {
    }
}
