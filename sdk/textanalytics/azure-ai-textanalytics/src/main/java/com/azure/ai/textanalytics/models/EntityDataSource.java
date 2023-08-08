// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;


import com.azure.ai.textanalytics.implementation.EntityDataSourcePropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The {@link EntityDataSource} model.
 */
@Immutable
public final class EntityDataSource {
    /*
     * Entity id in the given source catalog.
     */
    private String entityId;

    /*
     * Entity Catalog. Examples include: UMLS, CHV, MSH, etc.
     */
    private String name;

    static {
        EntityDataSourcePropertiesHelper.setAccessor(
            new EntityDataSourcePropertiesHelper.EntityDataSourceAccessor() {
                @Override
                public void setName(EntityDataSource entityDataSource, String name) {
                    entityDataSource.setName(name);
                }

                @Override
                public void setEntityId(EntityDataSource entityDataSource, String entityId) {
                    entityDataSource.setEntityId(entityId);
                }
            });
    }

    /**
     * Gets the data source name property: Entity Catalog. Examples include: UMLS, CHV, MSH, etc.
     *
     * @return the data source name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the data source entity ID property: data source entity ID in the given source catalog.
     *
     * @return the data source entity ID.
     */
    public String getEntityId() {
        return this.entityId;
    }

    /**
     * The private setter to set the data source name property
     * via {@link EntityDataSourcePropertiesHelper.EntityDataSourceAccessor}.
     *
     * @param name The data source name property: Entity Catalog. Examples include: UMLS, CHV, MSH, etc.
     */
    private void setName(String name) {
        this.name = name;
    }

    /**
     * The private setter to set the data source entity ID property
     * via {@link EntityDataSourcePropertiesHelper.EntityDataSourceAccessor}.
     *
     * @param entityId The data source entity ID in the given source catalog.
     */
    private void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
