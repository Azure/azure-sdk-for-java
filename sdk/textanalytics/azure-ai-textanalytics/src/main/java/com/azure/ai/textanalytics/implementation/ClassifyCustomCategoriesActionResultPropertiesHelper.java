// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyDocumentMultiCategoriesActionResult;
import com.azure.ai.textanalytics.util.ClassifyDocumentMultiCategoriesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyDocumentMultiCategoriesActionResult} instance.
 */
public final class ClassifyCustomCategoriesActionResultPropertiesHelper {

    private static ClassifyCustomCategoriesActionResultAccessor accessor;

    private ClassifyCustomCategoriesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyDocumentMultiCategoriesActionResult}
     * instance.
     */
    public interface ClassifyCustomCategoriesActionResultAccessor {
        void setDocumentsResults(ClassifyDocumentMultiCategoriesActionResult actionResult,
            ClassifyDocumentMultiCategoriesResultCollection documentsResults);
    }

    /**
     * The method called from {@link ClassifyDocumentMultiCategoriesActionResult} to set it's accessor.
     *
     * @param classifyCustomCategoriesActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoriesActionResultAccessor classifyCustomCategoriesActionResultAccessor) {
        accessor = classifyCustomCategoriesActionResultAccessor;
    }

    public static void setDocumentsResults(ClassifyDocumentMultiCategoriesActionResult actionResult,
        ClassifyDocumentMultiCategoriesResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
