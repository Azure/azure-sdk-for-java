// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.DataSourceSqlServerConnectionString;

public final class DataSourceSqlServerConnectionStringAccessor {
    private static Accessor accessor;

    private DataSourceSqlServerConnectionStringAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link DataSourceSqlServerConnectionString} instance.
     */
    public interface Accessor {
        void setId(DataSourceSqlServerConnectionString entity, String id);
        String getConnectionString(DataSourceSqlServerConnectionString entity);
    }

    /**
     * The method called from {@link DataSourceSqlServerConnectionString} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        DataSourceSqlServerConnectionStringAccessor.accessor = accessor;
    }

    public static void setId(DataSourceSqlServerConnectionString entity, String id) {
        accessor.setId(entity, id);
    }

    public static String getConnectionString(DataSourceSqlServerConnectionString entity) {
        return accessor.getConnectionString(entity);
    }
}
