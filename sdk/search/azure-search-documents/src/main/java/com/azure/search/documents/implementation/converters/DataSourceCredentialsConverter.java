// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.DataSourceCredentials;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.DataSourceCredentials} and
 * {@link DataSourceCredentials}.
 */
public final class DataSourceCredentialsConverter {
    private static final ClientLogger LOGGER = new ClientLogger(DataSourceCredentialsConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.DataSourceCredentials} to
     * {@link DataSourceCredentials}.
     */
    public static DataSourceCredentials map(com.azure.search.documents.implementation.models.DataSourceCredentials obj) {
        if (obj == null) {
            return null;
        }
        DataSourceCredentials dataSourceCredentials = new DataSourceCredentials();

        String _connectionString = obj.getConnectionString();
        dataSourceCredentials.setConnectionString(_connectionString);
        return dataSourceCredentials;
    }

    /**
     * Maps from {@link DataSourceCredentials} to
     * {@link com.azure.search.documents.implementation.models.DataSourceCredentials}.
     */
    public static com.azure.search.documents.implementation.models.DataSourceCredentials map(DataSourceCredentials obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.DataSourceCredentials dataSourceCredentials =
            new com.azure.search.documents.implementation.models.DataSourceCredentials();

        String _connectionString = obj.getConnectionString();
        dataSourceCredentials.setConnectionString(_connectionString);
        return dataSourceCredentials;
    }
}
