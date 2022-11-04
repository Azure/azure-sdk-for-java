// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityRelation;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeHealthcareEntitiesResult} instance.
 */
public final class AnalyzeHealthcareEntitiesResultPropertiesHelper {
    private static AnalyzeHealthcareEntitiesResultAccessor accessor;

    private AnalyzeHealthcareEntitiesResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeHealthcareEntitiesResult}
     * instance.
     */
    public interface AnalyzeHealthcareEntitiesResultAccessor {
        void setDetectedLanguage(AnalyzeHealthcareEntitiesResult entitiesResult, DetectedLanguage detectedLanguage);
        void setEntities(AnalyzeHealthcareEntitiesResult entitiesResult, IterableStream<HealthcareEntity> entities);
        void setWarnings(AnalyzeHealthcareEntitiesResult entitiesResult,
            IterableStream<TextAnalyticsWarning> warnings);
        void setEntityRelations(AnalyzeHealthcareEntitiesResult entitiesResult,
            IterableStream<HealthcareEntityRelation> entityRelations);
        void setFhirBundle(AnalyzeHealthcareEntitiesResult entitiesResult, Map<String, Object> fhirBundle);
    }

    /**
     * The method called from {@link AnalyzeHealthcareEntitiesResult} to set it's accessor.
     *
     * @param analyzeHealthcareEntitiesResultAccessor The accessor.
     */
    public static void setAccessor(
        final AnalyzeHealthcareEntitiesResultAccessor analyzeHealthcareEntitiesResultAccessor) {
        accessor = analyzeHealthcareEntitiesResultAccessor;
    }

    public static void setDetectedLanguage(AnalyzeHealthcareEntitiesResult entitiesResult,
        DetectedLanguage detectedLanguage) {
        accessor.setDetectedLanguage(entitiesResult, detectedLanguage);
    }

    public static void setEntities(AnalyzeHealthcareEntitiesResult entitiesResult,
        IterableStream<HealthcareEntity> entities) {
        accessor.setEntities(entitiesResult, entities);
    }

    public static void setWarnings(AnalyzeHealthcareEntitiesResult entitiesResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(entitiesResult, warnings);
    }

    public static void setEntityRelations(AnalyzeHealthcareEntitiesResult entitiesResult,
        IterableStream<HealthcareEntityRelation> entityRelations) {
        accessor.setEntityRelations(entitiesResult, entityRelations);
    }

    public static void setFhirBundle(AnalyzeHealthcareEntitiesResult entitiesResult, Map<String, Object> fhirBundle) {
        accessor.setFhirBundle(entitiesResult, fhirBundle);
    }
}
