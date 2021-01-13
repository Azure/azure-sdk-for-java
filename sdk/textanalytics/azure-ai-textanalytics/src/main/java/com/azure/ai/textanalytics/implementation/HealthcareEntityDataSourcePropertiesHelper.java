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
        void setName(HealthcareEntityDataSource healthcareEntityDataSource, String name);
        void setId(HealthcareEntityDataSource healthcareEntityDataSource, String id);
    }

    /**
     * The method called from {@link HealthcareEntityDataSource} to set it's accessor.
     *
     * @param healthcareEntityDataSourceAccessor The accessor.
     */
    public static void setAccessor(final HealthcareEntityDataSourceAccessor healthcareEntityDataSourceAccessor) {
        accessor = healthcareEntityDataSourceAccessor;
    }

    public static void setName(HealthcareEntityDataSource healthcareEntityDataSource, String name) {
        accessor.setName(healthcareEntityDataSource, name);
    }

    public static void setId(HealthcareEntityDataSource healthcareEntityDataSource, String id) {
        accessor.setId(healthcareEntityDataSource, id);
    }
}
