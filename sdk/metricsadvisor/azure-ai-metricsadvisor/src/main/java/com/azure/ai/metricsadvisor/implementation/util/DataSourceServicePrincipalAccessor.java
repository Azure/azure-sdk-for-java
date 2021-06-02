// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.DatasourceServicePrincipal;

public final class DataSourceServicePrincipalAccessor {
    private static Accessor accessor;

    private DataSourceServicePrincipalAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link DatasourceServicePrincipal} instance.
     */
    public interface Accessor {
        void setId(DatasourceServicePrincipal entity, String id);
        String getClientSecret(DatasourceServicePrincipal entity);
    }

    /**
     * The method called from {@link DatasourceServicePrincipal} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        DataSourceServicePrincipalAccessor.accessor = accessor;
    }

    public static void setId(DatasourceServicePrincipal entity, String id) {
        accessor.setId(entity, id);
    }

    public static String getClientSecret(DatasourceServicePrincipal entity) {
        return accessor.getClientSecret(entity);
    }
}
