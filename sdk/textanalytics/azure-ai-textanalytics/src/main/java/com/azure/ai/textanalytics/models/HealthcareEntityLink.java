// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;


import com.azure.ai.textanalytics.implementation.HealthcareEntityLinkPropertiesHelper;

/** The HealthcareEntityLink model. */
public final class HealthcareEntityLink {
    /*
     * Entity id in the given source catalog.
     */
    private String dataSourceId;

    /*
     * Entity Catalog. Examples include: UMLS, CHV, MSH, etc.
     */
    private String dataSource;

    static {
        HealthcareEntityLinkPropertiesHelper.setAccessor(
            new HealthcareEntityLinkPropertiesHelper.HealthcareEntityLinkAccessor() {
                @Override
                public void setDataSource(HealthcareEntityLink healthcareEntityLink, String dataSource) {
                    healthcareEntityLink.setDataSource(dataSource);
                }

                @Override
                public void setDataSourceId(HealthcareEntityLink healthcareEntityLink, String dataSourceId) {
                    healthcareEntityLink.setDataSourceId(dataSourceId);
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
     * via {@link HealthcareEntityLinkPropertiesHelper.HealthcareEntityLinkAccessor}.
     *
     * @param dataSource the dataSource property: Entity Catalog. Examples include: UMLS, CHV, MSH, etc.
     */
    private void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * The private setter to set the dataSourceId property
     * via {@link HealthcareEntityLinkPropertiesHelper.HealthcareEntityLinkAccessor}.
     *
     * @param dataSourceId The entity id in the given source catalog.
     */
    private void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }
}
