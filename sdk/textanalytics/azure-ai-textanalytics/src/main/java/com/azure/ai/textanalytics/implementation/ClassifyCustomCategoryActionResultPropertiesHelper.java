// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyCustomSingleCategoryActionResult;
import com.azure.ai.textanalytics.util.ClassifyCustomSingleCategoryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyCustomSingleCategoryActionResult} instance.
 */
public final class ClassifyCustomCategoryActionResultPropertiesHelper {
    private static ClassifyCustomCategoryActionResultAccessor accessor;

    private ClassifyCustomCategoryActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyCustomSingleCategoryActionResult}
     * instance.
     */
    public interface ClassifyCustomCategoryActionResultAccessor {
        void setDocumentsResults(ClassifyCustomSingleCategoryActionResult actionResult,
            ClassifyCustomSingleCategoryResultCollection documentsResults);
    }

    /**
     * The method called from {@link ClassifyCustomSingleCategoryActionResult} to set it's accessor.
     *
     * @param classifyCustomCategoryActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoryActionResultAccessor classifyCustomCategoryActionResultAccessor) {
        accessor = classifyCustomCategoryActionResultAccessor;
    }

    public static void setDocumentsResults(ClassifyCustomSingleCategoryActionResult actionResult,
        ClassifyCustomSingleCategoryResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
