// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationRole;

/**
 * The helper class to set the non-public properties of an {@link HealthcareEntityRelationRole} instance.
 */
public final class HealthcareEntityRelationRolePropertiesHelper {
    private static HealthcareEntityRelationRoleAccessor accessor;

    private HealthcareEntityRelationRolePropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link HealthcareEntityRelationRole} instance.
     */
    public interface HealthcareEntityRelationRoleAccessor {
        void setName(HealthcareEntityRelationRole healthcareEntityRelationRole, String name);
        void setEntity(HealthcareEntityRelationRole healthcareEntityRelationRole, HealthcareEntity entity);
    }

    /**
     * The method called from {@link HealthcareEntityRelationRole} to set it's accessor.
     *
     * @param entityRelationRoleAccessor The accessor.
     */
    public static void setAccessor(final HealthcareEntityRelationRoleAccessor entityRelationRoleAccessor) {
        accessor = entityRelationRoleAccessor;
    }

    public static void setName(HealthcareEntityRelationRole healthcareEntityRelationRole, String name) {
        accessor.setName(healthcareEntityRelationRole, name);
    }

    public static void setEntity(HealthcareEntityRelationRole healthcareEntityRelationRole, HealthcareEntity entity) {
        accessor.setEntity(healthcareEntityRelationRole, entity);
    }
}
