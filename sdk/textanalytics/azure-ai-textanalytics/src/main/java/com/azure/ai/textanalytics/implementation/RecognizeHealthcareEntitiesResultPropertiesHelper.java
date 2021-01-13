// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.HealthcareEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeHealthcareEntitiesResult;

/**
 * The helper class to set the non-public properties of an {@link RecognizeHealthcareEntitiesResult} instance.
 */
public final class RecognizeHealthcareEntitiesResultPropertiesHelper {
    private static RecognizeHealthcareEntitiesResultAccessor accessor;

    private RecognizeHealthcareEntitiesResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link RecognizeHealthcareEntitiesResult}
     * instance.
     */
    public interface RecognizeHealthcareEntitiesResultAccessor {
        void setEntities(RecognizeHealthcareEntitiesResult entitiesResult, HealthcareEntityCollection entities);
    }

    /**
     * The method called from {@link RecognizeHealthcareEntitiesResult} to set it's accessor.
     *
     * @param recognizeHealthcareEntitiesResultAccessor The accessor.
     */
    public static void setAccessor(
        final RecognizeHealthcareEntitiesResultAccessor recognizeHealthcareEntitiesResultAccessor) {
        accessor = recognizeHealthcareEntitiesResultAccessor;
    }


    public static void setEntities(RecognizeHealthcareEntitiesResult entitiesResult,
        HealthcareEntityCollection entities) {
        accessor.setEntities(entitiesResult, entities);
    }
}
