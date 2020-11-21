// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.HealthcareEntityCollection;
import com.azure.ai.textanalytics.models.HealthcareEntityRelation;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link HealthcareEntityCollection} instance.
 */
public final class HealthcareEntityCollectionPropertiesHelper {
    private static HealthcareEntityCollectionAccessor accessor;

    private HealthcareEntityCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link HealthcareEntityCollection} instance.
     */
    public interface HealthcareEntityCollectionAccessor {
        void setWarnings(HealthcareEntityCollection healthcareEntityCollection,
            IterableStream<TextAnalyticsWarning> warnings);
        void setEntityRelations(HealthcareEntityCollection healthcareEntityCollection,
            IterableStream<HealthcareEntityRelation> entityRelations);
    }

    /**
     * The method called from {@link HealthcareEntityCollection} to set it's accessor.
     *
     * @param healthcareEntityCollectionAccessor The accessor.
     */
    public static void setAccessor(final HealthcareEntityCollectionAccessor healthcareEntityCollectionAccessor) {
        accessor = healthcareEntityCollectionAccessor;
    }

    public static void setWarnings(HealthcareEntityCollection healthcareEntityCollection,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(healthcareEntityCollection, warnings);
    }

    public static void setEntityRelations(HealthcareEntityCollection healthcareEntityCollection,
        IterableStream<HealthcareEntityRelation> entityRelations) {
        accessor.setEntityRelations(healthcareEntityCollection, entityRelations);
    }
}
