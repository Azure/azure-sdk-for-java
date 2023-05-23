// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.HealthcareEntityRelation;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationRole;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationType;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link HealthcareEntityRelation} instance.
 */
public final class HealthcareEntityRelationPropertiesHelper {
    private static HealthcareEntityRelationAccessor accessor;

    private HealthcareEntityRelationPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link HealthcareEntityRelation} instance.
     */
    public interface HealthcareEntityRelationAccessor {
        void setRelationType(HealthcareEntityRelation healthcareEntityRelation,
            HealthcareEntityRelationType relationType);
        void setRoles(HealthcareEntityRelation healthcareEntityRelation,
            IterableStream<HealthcareEntityRelationRole> roles);
    }

    /**
     * The method called from {@link HealthcareEntityRelation} to set it's accessor.
     *
     * @param entityRelationAccessor The accessor.
     */
    public static void setAccessor(final HealthcareEntityRelationAccessor entityRelationAccessor) {
        accessor = entityRelationAccessor;
    }

    public static void setRelationType(HealthcareEntityRelation healthcareEntityRelation,
        HealthcareEntityRelationType relationType) {
        accessor.setRelationType(healthcareEntityRelation, relationType);
    }

    public static void setRoles(HealthcareEntityRelation healthcareEntityRelation,
        IterableStream<HealthcareEntityRelationRole> roles) {
        accessor.setRoles(healthcareEntityRelation, roles);
    }
}
