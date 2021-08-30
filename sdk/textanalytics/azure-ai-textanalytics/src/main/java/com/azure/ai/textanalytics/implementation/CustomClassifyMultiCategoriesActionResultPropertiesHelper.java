// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.CustomClassifyMultiCategoriesActionResult;
import com.azure.ai.textanalytics.util.CustomClassifyMultiCategoriesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link CustomClassifyMultiCategoriesActionResult} instance.
 */
public final class CustomClassifyMultiCategoriesActionResultPropertiesHelper {

    private static ClassifyMultiCategoriesActionResultAccessor accessor;

    private CustomClassifyMultiCategoriesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomClassifyMultiCategoriesActionResult}
     * instance.
     */
    public interface ClassifyMultiCategoriesActionResultAccessor {
        void setDocumentsResults(CustomClassifyMultiCategoriesActionResult actionResult,
            CustomClassifyMultiCategoriesResultCollection documentsResults);
    }

    /**
     * The method called from {@link CustomClassifyMultiCategoriesActionResult} to set it's accessor.
     *
     * @param classifyMultiCategoriesActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyMultiCategoriesActionResultAccessor classifyMultiCategoriesActionResultAccessor) {
        accessor = classifyMultiCategoriesActionResultAccessor;
    }

    public static void setDocumentsResults(CustomClassifyMultiCategoriesActionResult actionResult,
        CustomClassifyMultiCategoriesResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
