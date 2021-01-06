// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.HealthcareEntityDataSource;

/**
 * The helper class to set the non-public properties of an {@link HealthcareEntityDataSource} instance.
 */
public final class HealthcareEntityDataSourcePropertiesHelper {
    private static HealthcareEntityDataSourceAccessor accessor;

    private HealthcareEntityDataSourcePropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link HealthcareEntityDataSource} instance.
     */
    public interface HealthcareEntityDataSourceAccessor {
        void setDataSource(HealthcareEntityDataSource healthcareEntityDataSource, String dataSource);
        void setDataSourceId(HealthcareEntityDataSource healthcareEntityDataSource, String dataSourceId);
    }

    /**
     * The method called from {@link HealthcareEntityDataSource} to set it's accessor.
     *
     * @param healthcareEntityDataSourceAccessor The accessor.
     */
    public static void setAccessor(final HealthcareEntityDataSourceAccessor healthcareEntityDataSourceAccessor) {
        accessor = healthcareEntityDataSourceAccessor;
    }

    public static void setDataSource(HealthcareEntityDataSource healthcareEntityDataSource, String dataSource) {
        accessor.setDataSource(healthcareEntityDataSource, dataSource);
    }

    public static void setDataSourceId(HealthcareEntityDataSource healthcareEntityDataSource, String dataSourceId) {
        accessor.setDataSourceId(healthcareEntityDataSource, dataSourceId);
    }
}
