// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifySingleCategoryActionResult;
import com.azure.ai.textanalytics.util.ClassifySingleCategoryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifySingleCategoryActionResult} instance.
 */
public final class ClassifySingleCategoryActionResultPropertiesHelper {
    private static ClassifySingleCategoryActionResultAccessor accessor;

    private ClassifySingleCategoryActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifySingleCategoryActionResult}
     * instance.
     */
    public interface ClassifySingleCategoryActionResultAccessor {
        void setDocumentsResults(ClassifySingleCategoryActionResult actionResult,
            ClassifySingleCategoryResultCollection documentsResults);
    }

    /**
     * The method called from {@link ClassifySingleCategoryActionResult} to set it's accessor.
     *
     * @param classifySingleCategoryActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifySingleCategoryActionResultAccessor classifySingleCategoryActionResultAccessor) {
        accessor = classifySingleCategoryActionResultAccessor;
    }

    public static void setDocumentsResults(ClassifySingleCategoryActionResult actionResult,
        ClassifySingleCategoryResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
