// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.SqlServerConnectionStringCredentialEntity;

public final class SqlServerConnectionStringCredentialEntityAccessor {
    private static Accessor accessor;

    private SqlServerConnectionStringCredentialEntityAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link SqlServerConnectionStringCredentialEntity} instance.
     */
    public interface Accessor {
        void setId(SqlServerConnectionStringCredentialEntity entity, String id);
        String getConnectionString(SqlServerConnectionStringCredentialEntity entity);
    }

    /**
     * The method called from {@link SqlServerConnectionStringCredentialEntity} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        SqlServerConnectionStringCredentialEntityAccessor.accessor = accessor;
    }

    public static void setId(SqlServerConnectionStringCredentialEntity entity, String id) {
        accessor.setId(entity, id);
    }

    public static String getConnectionString(SqlServerConnectionStringCredentialEntity entity) {
        return accessor.getConnectionString(entity);
    }
}
