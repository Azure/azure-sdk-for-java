// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.HealthcareEntityLink;

/**
 * The helper class to set the non-public properties of an {@link HealthcareEntityLink} instance.
 */
public final class HealthcareEntityLinkPropertiesHelper {
    private static HealthcareEntityLinkAccessor accessor;

    private HealthcareEntityLinkPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link HealthcareEntityLink} instance.
     */
    public interface HealthcareEntityLinkAccessor {
        void setDataSource(HealthcareEntityLink healthcareEntityLink, String dataSource);
        void setDataSourceId(HealthcareEntityLink healthcareEntityLink, String dataSourceId);
    }

    /**
     * The method called from {@link HealthcareEntityLink} to set it's accessor.
     *
     * @param healthcareEntityLinkAccessor The accessor.
     */
    public static void setAccessor(final HealthcareEntityLinkAccessor healthcareEntityLinkAccessor) {
        accessor = healthcareEntityLinkAccessor;
    }

    public static void setDataSource(HealthcareEntityLink healthcareEntityLink, String dataSource) {
        accessor.setDataSource(healthcareEntityLink, dataSource);
    }

    public static void setDataSourceId(HealthcareEntityLink healthcareEntityLink, String dataSourceId) {
        accessor.setDataSourceId(healthcareEntityLink, dataSourceId);
    }
}
