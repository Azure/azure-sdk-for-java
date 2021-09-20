// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyCustomMultiCategoriesActionResult;
import com.azure.ai.textanalytics.util.ClassifyCustomMultiCategoriesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyCustomMultiCategoriesActionResult} instance.
 */
public final class ClassifyCustomCategoriesActionResultPropertiesHelper {

    private static ClassifyCustomCategoriesActionResultAccessor accessor;

    private ClassifyCustomCategoriesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyCustomMultiCategoriesActionResult}
     * instance.
     */
    public interface ClassifyCustomCategoriesActionResultAccessor {
        void setDocumentsResults(ClassifyCustomMultiCategoriesActionResult actionResult,
            ClassifyCustomMultiCategoriesResultCollection documentsResults);
    }

    /**
     * The method called from {@link ClassifyCustomMultiCategoriesActionResult} to set it's accessor.
     *
     * @param classifyCustomCategoriesActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoriesActionResultAccessor classifyCustomCategoriesActionResultAccessor) {
        accessor = classifyCustomCategoriesActionResultAccessor;
    }

    public static void setDocumentsResults(ClassifyCustomMultiCategoriesActionResult actionResult,
        ClassifyCustomMultiCategoriesResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
