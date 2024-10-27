// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.EntityDataSource;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityAssertion;
import com.azure.ai.textanalytics.models.HealthcareEntityCategory;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link HealthcareEntity} instance.
 */
public final class HealthcareEntityPropertiesHelper {
    private static HealthcareEntityAccessor accessor;

    private HealthcareEntityPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link HealthcareEntity} instance.
     */
    public interface HealthcareEntityAccessor {
        void setText(HealthcareEntity healthcareEntity, String text);
        void setNormalizedText(HealthcareEntity healthcareEntity, String normalizedText);
        void setCategory(HealthcareEntity healthcareEntity, HealthcareEntityCategory category);
        void setSubcategory(HealthcareEntity healthcareEntity, String subcategory);
        void setConfidenceScore(HealthcareEntity healthcareEntity, double confidenceScore);
        void setDataSources(HealthcareEntity healthcareEntity, IterableStream<EntityDataSource> dataSources);
        void setAssertion(HealthcareEntity healthcareEntity, HealthcareEntityAssertion assertion);
        void setOffset(HealthcareEntity healthcareEntity, int offset);
        void setLength(HealthcareEntity healthcareEntity, int length);
    }

    /**
     * The method called from {@link HealthcareEntity} to set it's accessor.
     *
     * @param healthcareEntityAccessor The accessor.
     */
    public static void setAccessor(final HealthcareEntityAccessor healthcareEntityAccessor) {
        accessor = healthcareEntityAccessor;
    }

    public static void setText(HealthcareEntity healthcareEntity, String text) {
        accessor.setText(healthcareEntity, text);
    }

    public static void setCategory(HealthcareEntity healthcareEntity, HealthcareEntityCategory category) {
        accessor.setCategory(healthcareEntity, category);
    }

    public static void setSubcategory(HealthcareEntity healthcareEntity, String subcategory) {
        accessor.setSubcategory(healthcareEntity, subcategory);
    }

    public static void setConfidenceScore(HealthcareEntity healthcareEntity, double confidenceScore) {
        accessor.setConfidenceScore(healthcareEntity, confidenceScore);
    }

    public static void setDataSources(HealthcareEntity healthcareEntity,
        IterableStream<EntityDataSource> dataSources) {
        accessor.setDataSources(healthcareEntity, dataSources);
    }

    public static void setNormalizedText(HealthcareEntity healthcareEntity, String normalizedText) {
        accessor.setNormalizedText(healthcareEntity, normalizedText);
    }

    public static void setAssertion(HealthcareEntity healthcareEntity, HealthcareEntityAssertion assertion) {
        accessor.setAssertion(healthcareEntity, assertion);
    }

    public static void setOffset(HealthcareEntity healthcareEntity, int offset) {
        accessor.setOffset(healthcareEntity, offset);
    }

    public static void setLength(HealthcareEntity healthcareEntity, int length) {
        accessor.setLength(healthcareEntity, length);
    }
}
