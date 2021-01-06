// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;


import com.azure.ai.textanalytics.implementation.HealthcareEntityDataSourcePropertiesHelper;

/** The HealthcareEntityLink model. */
public final class HealthcareEntityDataSource {
    /*
     * Entity id in the given source catalog.
     */
    private String dataSourceId;

    /*
     * Entity Catalog. Examples include: UMLS, CHV, MSH, etc.
     */
    private String dataSource;

    static {
        HealthcareEntityDataSourcePropertiesHelper.setAccessor(
            new HealthcareEntityDataSourcePropertiesHelper.HealthcareEntityDataSourceAccessor() {
                @Override
                public void setDataSource(HealthcareEntityDataSource healthcareEntityDataSource, String dataSource) {
                    healthcareEntityDataSource.setDataSource(dataSource);
                }

                @Override
                public void setDataSourceId(HealthcareEntityDataSource healthcareEntityDataSource,
                    String dataSourceId) {
                    healthcareEntityDataSource.setDataSourceId(dataSourceId);
                }
            });
    }

    /**
     * Get the dataSource property: Entity Catalog. Examples include: UMLS, CHV, MSH, etc.
     *
     * @return the dataSource value.
     */
    public String getDataSource() {
        return this.dataSource;
    }

    /**
     * Get the id property: Entity id in the given source catalog.
     *
     * @return the id value.
     */
    public String getDataSourceId() {
        return this.dataSourceId;
    }

    /**
     * The private setter to set the dataSource property
     * via {@link HealthcareEntityDataSourcePropertiesHelper.HealthcareEntityDataSourceAccessor}.
     *
     * @param dataSource the dataSource property: Entity Catalog. Examples include: UMLS, CHV, MSH, etc.
     */
    private void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * The private setter to set the dataSourceId property
     * via {@link HealthcareEntityDataSourcePropertiesHelper.HealthcareEntityDataSourceAccessor}.
     *
     * @param dataSourceId The entity id in the given source catalog.
     */
    private void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }
}
