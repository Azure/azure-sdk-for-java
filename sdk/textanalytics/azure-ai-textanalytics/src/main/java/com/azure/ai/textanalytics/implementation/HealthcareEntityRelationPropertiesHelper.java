// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.HealthcareEntityRelation;

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
        void setRelationType(HealthcareEntityRelation healthcareEntityRelation, String relationType);
        void setBidirectional(HealthcareEntityRelation healthcareEntityRelation, boolean bidirectional);
        void setSourceLink(HealthcareEntityRelation healthcareEntityRelation, String sourceLink);
        void setTargetLink(HealthcareEntityRelation healthcareEntityRelation, String targetLink);
    }

    /**
     * The method called from {@link HealthcareEntityRelation} to set it's accessor.
     *
     * @param healthcareEntityRelationAccessor The accessor.
     */
    public static void setAccessor(final HealthcareEntityRelationAccessor healthcareEntityRelationAccessor) {
        accessor = healthcareEntityRelationAccessor;
    }

    public static void setRelationType(HealthcareEntityRelation healthcareEntityRelation, String relationType) {
        accessor.setRelationType(healthcareEntityRelation, relationType);
    }

    public static void setBidirectional(HealthcareEntityRelation healthcareEntityRelation, boolean bidirectional) {
        accessor.setBidirectional(healthcareEntityRelation, bidirectional);
    }

    public static void setSourceLink(HealthcareEntityRelation healthcareEntityRelation, String sourceLink) {
        accessor.setSourceLink(healthcareEntityRelation, sourceLink);
    }

    public static void setTargetLink(HealthcareEntityRelation healthcareEntityRelation, String targetLink) {
        accessor.setTargetLink(healthcareEntityRelation, targetLink);
    }
}
