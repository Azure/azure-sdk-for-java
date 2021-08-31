// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyCustomCategoriesActionResult;
import com.azure.ai.textanalytics.util.ClassifyCustomCategoriesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyCustomCategoriesActionResult} instance.
 */
public final class ClassifyCustomCategoriesActionResultPropertiesHelper {

    private static ClassifyCustomCategoriesActionResultAccessor accessor;

    private ClassifyCustomCategoriesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyCustomCategoriesActionResult}
     * instance.
     */
    public interface ClassifyCustomCategoriesActionResultAccessor {
        void setDocumentsResults(ClassifyCustomCategoriesActionResult actionResult,
            ClassifyCustomCategoriesResultCollection documentsResults);
    }

    /**
     * The method called from {@link ClassifyCustomCategoriesActionResult} to set it's accessor.
     *
     * @param classifyCustomCategoriesActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoriesActionResultAccessor classifyCustomCategoriesActionResultAccessor) {
        accessor = classifyCustomCategoriesActionResultAccessor;
    }

    public static void setDocumentsResults(ClassifyCustomCategoriesActionResult actionResult,
        ClassifyCustomCategoriesResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
