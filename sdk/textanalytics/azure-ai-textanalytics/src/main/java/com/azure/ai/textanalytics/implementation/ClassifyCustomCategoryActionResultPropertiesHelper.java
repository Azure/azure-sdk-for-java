// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyDocumentSingleCategoryActionResult;
import com.azure.ai.textanalytics.util.ClassifyDocumentSingleCategoryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyDocumentSingleCategoryActionResult} instance.
 */
public final class ClassifyCustomCategoryActionResultPropertiesHelper {
    private static ClassifyCustomCategoryActionResultAccessor accessor;

    private ClassifyCustomCategoryActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyDocumentSingleCategoryActionResult}
     * instance.
     */
    public interface ClassifyCustomCategoryActionResultAccessor {
        void setDocumentsResults(ClassifyDocumentSingleCategoryActionResult actionResult,
            ClassifyDocumentSingleCategoryResultCollection documentsResults);
    }

    /**
     * The method called from {@link ClassifyDocumentSingleCategoryActionResult} to set it's accessor.
     *
     * @param classifyCustomCategoryActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoryActionResultAccessor classifyCustomCategoryActionResultAccessor) {
        accessor = classifyCustomCategoryActionResultAccessor;
    }

    public static void setDocumentsResults(ClassifyDocumentSingleCategoryActionResult actionResult,
        ClassifyDocumentSingleCategoryResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
