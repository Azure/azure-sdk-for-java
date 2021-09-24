// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.SingleCategoryClassifyActionResult;
import com.azure.ai.textanalytics.util.SingleCategoryClassifyResultCollection;

/**
 * The helper class to set the non-public properties of an {@link SingleCategoryClassifyActionResult} instance.
 */
public final class ClassifyCustomCategoryActionResultPropertiesHelper {
    private static ClassifyCustomCategoryActionResultAccessor accessor;

    private ClassifyCustomCategoryActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SingleCategoryClassifyActionResult}
     * instance.
     */
    public interface ClassifyCustomCategoryActionResultAccessor {
        void setDocumentsResults(SingleCategoryClassifyActionResult actionResult,
            SingleCategoryClassifyResultCollection documentsResults);
    }

    /**
     * The method called from {@link SingleCategoryClassifyActionResult} to set it's accessor.
     *
     * @param classifyCustomCategoryActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoryActionResultAccessor classifyCustomCategoryActionResultAccessor) {
        accessor = classifyCustomCategoryActionResultAccessor;
    }

    public static void setDocumentsResults(SingleCategoryClassifyActionResult actionResult,
        SingleCategoryClassifyResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
