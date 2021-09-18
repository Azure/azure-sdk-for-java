// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.CustomClassifyDocumentSingleCategoryActionResult;
import com.azure.ai.textanalytics.util.CustomClassifyDocumentSingleCategoryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link CustomClassifyDocumentSingleCategoryActionResult} instance.
 */
public final class ClassifyCustomCategoryActionResultPropertiesHelper {
    private static ClassifyCustomCategoryActionResultAccessor accessor;

    private ClassifyCustomCategoryActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomClassifyDocumentSingleCategoryActionResult}
     * instance.
     */
    public interface ClassifyCustomCategoryActionResultAccessor {
        void setDocumentsResults(CustomClassifyDocumentSingleCategoryActionResult actionResult,
            CustomClassifyDocumentSingleCategoryResultCollection documentsResults);
    }

    /**
     * The method called from {@link CustomClassifyDocumentSingleCategoryActionResult} to set it's accessor.
     *
     * @param classifyCustomCategoryActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoryActionResultAccessor classifyCustomCategoryActionResultAccessor) {
        accessor = classifyCustomCategoryActionResultAccessor;
    }

    public static void setDocumentsResults(CustomClassifyDocumentSingleCategoryActionResult actionResult,
        CustomClassifyDocumentSingleCategoryResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
