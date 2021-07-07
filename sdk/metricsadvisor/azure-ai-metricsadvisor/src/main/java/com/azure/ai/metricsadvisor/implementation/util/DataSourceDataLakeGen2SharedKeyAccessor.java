// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.DataSourceDataLakeGen2SharedKey;

public final class DataSourceDataLakeGen2SharedKeyAccessor {
    private static Accessor accessor;

    private DataSourceDataLakeGen2SharedKeyAccessor() {
    }

    /**
     * Type defining the methods to set the non-public properties of
     * an {@link DataSourceDataLakeGen2SharedKey} instance.
     */
    public interface Accessor {
        void setId(DataSourceDataLakeGen2SharedKey entity, String id);
        String getSharedKey(DataSourceDataLakeGen2SharedKey entity);
    }

    /**
     * The method called from {@link DataSourceDataLakeGen2SharedKey} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final Accessor accessor) {
        DataSourceDataLakeGen2SharedKeyAccessor.accessor = accessor;
    }

    public static void setId(DataSourceDataLakeGen2SharedKey entity, String id) {
        accessor.setId(entity, id);
    }

    public static String getSharedKey(DataSourceDataLakeGen2SharedKey entity) {
        return accessor.getSharedKey(entity);
    }
}
