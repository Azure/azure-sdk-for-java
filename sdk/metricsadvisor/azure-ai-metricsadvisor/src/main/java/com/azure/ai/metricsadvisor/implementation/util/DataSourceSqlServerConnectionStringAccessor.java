// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.DatasourceSqlServerConnectionString;

public final class DataSourceSqlServerConnectionStringAccessor {
    private static Accessor accessor;

    private DataSourceSqlServerConnectionStringAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link DatasourceSqlServerConnectionString} instance.
     */
    public interface Accessor {
        void setId(DatasourceSqlServerConnectionString entity, String id);
        String getConnectionString(DatasourceSqlServerConnectionString entity);
    }

    /**
     * The method called from {@link DatasourceSqlServerConnectionString} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        DataSourceSqlServerConnectionStringAccessor.accessor = accessor;
    }

    public static void setId(DatasourceSqlServerConnectionString entity, String id) {
        accessor.setId(entity, id);
    }

    public static String getConnectionString(DatasourceSqlServerConnectionString entity) {
        return accessor.getConnectionString(entity);
    }
}
