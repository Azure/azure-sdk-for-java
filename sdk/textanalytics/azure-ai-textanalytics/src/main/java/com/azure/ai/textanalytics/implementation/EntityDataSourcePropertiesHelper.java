// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.EntityDataSource;

/**
 * The helper class to set the non-public properties of an {@link EntityDataSource} instance.
 */
public final class EntityDataSourcePropertiesHelper {
    private static EntityDataSourceAccessor accessor;

    private EntityDataSourcePropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link EntityDataSource} instance.
     */
    public interface EntityDataSourceAccessor {
        void setName(EntityDataSource entityDataSource, String name);
        void setEntityId(EntityDataSource entityDataSource, String entityId);
    }

    /**
     * The method called from {@link EntityDataSource} to set it's accessor.
     *
     * @param entityDataSourceAccessor The accessor.
     */
    public static void setAccessor(final EntityDataSourceAccessor entityDataSourceAccessor) {
        accessor = entityDataSourceAccessor;
    }

    public static void setName(EntityDataSource entityDataSource, String name) {
        accessor.setName(entityDataSource, name);
    }

    public static void setEntityId(EntityDataSource entityDataSource, String entityId) {
        accessor.setEntityId(entityDataSource, entityId);
    }
}
