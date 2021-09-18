// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.CustomClassifyDocumentMultiCategoriesActionResult;
import com.azure.ai.textanalytics.util.CustomClassifyDocumentMultiCategoriesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link CustomClassifyDocumentMultiCategoriesActionResult} instance.
 */
public final class ClassifyCustomCategoriesActionResultPropertiesHelper {

    private static ClassifyCustomCategoriesActionResultAccessor accessor;

    private ClassifyCustomCategoriesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomClassifyDocumentMultiCategoriesActionResult}
     * instance.
     */
    public interface ClassifyCustomCategoriesActionResultAccessor {
        void setDocumentsResults(CustomClassifyDocumentMultiCategoriesActionResult actionResult,
            CustomClassifyDocumentMultiCategoriesResultCollection documentsResults);
    }

    /**
     * The method called from {@link CustomClassifyDocumentMultiCategoriesActionResult} to set it's accessor.
     *
     * @param classifyCustomCategoriesActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoriesActionResultAccessor classifyCustomCategoriesActionResultAccessor) {
        accessor = classifyCustomCategoriesActionResultAccessor;
    }

    public static void setDocumentsResults(CustomClassifyDocumentMultiCategoriesActionResult actionResult,
        CustomClassifyDocumentMultiCategoriesResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
