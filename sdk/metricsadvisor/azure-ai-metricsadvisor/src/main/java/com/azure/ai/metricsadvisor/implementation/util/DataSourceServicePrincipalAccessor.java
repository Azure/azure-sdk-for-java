// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.DataSourceServicePrincipal;

public final class DataSourceServicePrincipalAccessor {
    private static Accessor accessor;

    private DataSourceServicePrincipalAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link DataSourceServicePrincipal} instance.
     */
    public interface Accessor {
        void setId(DataSourceServicePrincipal entity, String id);
        String getClientSecret(DataSourceServicePrincipal entity);
    }

    /**
     * The method called from {@link DataSourceServicePrincipal} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        DataSourceServicePrincipalAccessor.accessor = accessor;
    }

    public static void setId(DataSourceServicePrincipal entity, String id) {
        accessor.setId(entity, id);
    }

    public static String getClientSecret(DataSourceServicePrincipal entity) {
        return accessor.getClientSecret(entity);
    }
}
