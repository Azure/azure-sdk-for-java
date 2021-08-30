// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyMultiCategoriesActionResult;
import com.azure.ai.textanalytics.util.ClassifyMultiCategoriesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyMultiCategoriesActionResult} instance.
 */
public final class ClassifyMultiCategoriesActionResultPropertiesHelper {

    private static ClassifyMultiCategoriesActionResultAccessor accessor;

    private ClassifyMultiCategoriesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyMultiCategoriesActionResult}
     * instance.
     */
    public interface ClassifyMultiCategoriesActionResultAccessor {
        void setDocumentsResults(ClassifyMultiCategoriesActionResult actionResult,
            ClassifyMultiCategoriesResultCollection documentsResults);
    }

    /**
     * The method called from {@link ClassifyMultiCategoriesActionResult} to set it's accessor.
     *
     * @param classifyMultiCategoriesActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyMultiCategoriesActionResultAccessor classifyMultiCategoriesActionResultAccessor) {
        accessor = classifyMultiCategoriesActionResultAccessor;
    }

    public static void setDocumentsResults(ClassifyMultiCategoriesActionResult actionResult,
        ClassifyMultiCategoriesResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
