// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.CategorizedEntityCollection;
import com.azure.ai.textanalytics.models.CustomRecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.models.CustomRecognizeEntitiesResult;

/**
 * The helper class to set the non-public properties of an {@link CustomRecognizeEntitiesResult} instance.
 */
public final class CustomRecognizeEntitiesResultPropertiesHelper {
    private static RecognizeCustomEntitiesResultAccessor accessor;

    private CustomRecognizeEntitiesResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomRecognizeEntitiesResult}
     * instance.
     */
    public interface RecognizeCustomEntitiesResultAccessor {
        void setEntities(CustomRecognizeEntitiesResult result, CategorizedEntityCollection entities);
    }

    /**
     * The method called from {@link CustomRecognizeEntitiesActionResult} to set it's accessor.
     *
     * @param recognizeCustomEntitiesResultAccessor The accessor.
     */
    public static void setAccessor(
        final RecognizeCustomEntitiesResultAccessor recognizeCustomEntitiesResultAccessor) {
        accessor = recognizeCustomEntitiesResultAccessor;
    }

    public static void setEntities(CustomRecognizeEntitiesResult result, CategorizedEntityCollection entities) {
        accessor.setEntities(result, entities);
    }
}
