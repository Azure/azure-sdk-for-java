// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyCustomCategoryActionResult;
import com.azure.ai.textanalytics.util.ClassifyCustomCategoryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyCustomCategoryActionResult} instance.
 */
public final class ClassifyCustomCategoryActionResultPropertiesHelper {
    private static ClassifyCustomCategoryActionResultAccessor accessor;

    private ClassifyCustomCategoryActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyCustomCategoryActionResult}
     * instance.
     */
    public interface ClassifyCustomCategoryActionResultAccessor {
        void setDocumentsResults(ClassifyCustomCategoryActionResult actionResult,
            ClassifyCustomCategoryResultCollection documentsResults);
    }

    /**
     * The method called from {@link ClassifyCustomCategoryActionResult} to set it's accessor.
     *
     * @param classifyCustomCategoryActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoryActionResultAccessor classifyCustomCategoryActionResultAccessor) {
        accessor = classifyCustomCategoryActionResultAccessor;
    }

    public static void setDocumentsResults(ClassifyCustomCategoryActionResult actionResult,
        ClassifyCustomCategoryResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
