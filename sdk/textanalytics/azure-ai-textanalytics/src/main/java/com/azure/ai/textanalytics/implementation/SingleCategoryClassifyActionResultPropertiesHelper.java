// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.SingleCategoryClassifyActionResult;
import com.azure.ai.textanalytics.util.SingleCategoryClassifyResultCollection;

/**
 * The helper class to set the non-public properties of an {@link SingleCategoryClassifyActionResult} instance.
 */
public final class SingleCategoryClassifyActionResultPropertiesHelper {
    private static SingleCategoryClassifyActionResultAccessor accessor;

    private SingleCategoryClassifyActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SingleCategoryClassifyActionResult}
     * instance.
     */
    public interface SingleCategoryClassifyActionResultAccessor {
        void setDocumentsResults(SingleCategoryClassifyActionResult actionResult,
            SingleCategoryClassifyResultCollection documentsResults);
    }

    /**
     * The method called from {@link SingleCategoryClassifyActionResult} to set it's accessor.
     *
     * @param singleCategoryClassifyActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final SingleCategoryClassifyActionResultAccessor singleCategoryClassifyActionResultAccessor) {
        accessor = singleCategoryClassifyActionResultAccessor;
    }

    public static void setDocumentsResults(SingleCategoryClassifyActionResult actionResult,
        SingleCategoryClassifyResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
