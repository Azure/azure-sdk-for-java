// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.CustomEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesResult;

/**
 * The helper class to set the non-public properties of an {@link RecognizeCustomEntitiesResult} instance.
 */
public final class RecognizeCustomEntitiesResultPropertiesHelper {
    private static RecognizeCustomEntitiesResultAccessor accessor;

    private RecognizeCustomEntitiesResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link RecognizeCustomEntitiesResult}
     * instance.
     */
    public interface RecognizeCustomEntitiesResultAccessor {
        void setEntities(RecognizeCustomEntitiesResult actionResult, CustomEntityCollection entities);
    }

    /**
     * The method called from {@link RecognizeCustomEntitiesActionResult} to set it's accessor.
     *
     * @param recognizeCustomEntitiesResultAccessor The accessor.
     */
    public static void setAccessor(
        final RecognizeCustomEntitiesResultAccessor recognizeCustomEntitiesResultAccessor) {
        accessor = recognizeCustomEntitiesResultAccessor;
    }

    public static void setEntities(RecognizeCustomEntitiesResult actionResult, CustomEntityCollection entities) {
        accessor.setEntities(actionResult, entities);
    }
}
