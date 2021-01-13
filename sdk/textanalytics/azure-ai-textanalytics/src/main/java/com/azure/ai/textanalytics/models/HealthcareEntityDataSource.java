// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;


import com.azure.ai.textanalytics.implementation.HealthcareEntityDataSourcePropertiesHelper;

/** The HealthcareEntityLink model. */
public final class HealthcareEntityDataSource {
    /*
     * Entity id in the given source catalog.
     */
    private String id;

    /*
     * Entity Catalog. Examples include: UMLS, CHV, MSH, etc.
     */
    private String name;

    static {
        HealthcareEntityDataSourcePropertiesHelper.setAccessor(
            new HealthcareEntityDataSourcePropertiesHelper.HealthcareEntityDataSourceAccessor() {
                @Override
                public void setName(HealthcareEntityDataSource healthcareEntityDataSource, String name) {
                    healthcareEntityDataSource.setName(name);
                }

                @Override
                public void setId(HealthcareEntityDataSource healthcareEntityDataSource,
                    String id) {
                    healthcareEntityDataSource.setId(id);
                }
            });
    }

    /**
     * Get the data source name property: Entity Catalog. Examples include: UMLS, CHV, MSH, etc.
     *
     * @return the data source name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the data source ID property: data source ID in the given source catalog.
     *
     * @return the data source iD.
     */
    public String getId() {
        return this.id;
    }

    /**
     * The private setter to set the data source name property
     * via {@link HealthcareEntityDataSourcePropertiesHelper.HealthcareEntityDataSourceAccessor}.
     *
     * @param name the data source name property: Entity Catalog. Examples include: UMLS, CHV, MSH, etc.
     */
    private void setName(String name) {
        this.name = name;
    }

    /**
     * The private setter to set the data source ID property
     * via {@link HealthcareEntityDataSourcePropertiesHelper.HealthcareEntityDataSourceAccessor}.
     *
     * @param id The data source ID in the given source catalog.
     */
    private void setId(String id) {
        this.id = id;
    }
}
