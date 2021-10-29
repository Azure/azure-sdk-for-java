// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.MultiCategoryClassifyActionResult;
import com.azure.ai.textanalytics.util.MultiCategoryClassifyResultCollection;

/**
 * The helper class to set the non-public properties of an {@link MultiCategoryClassifyActionResult} instance.
 */
public final class MultiCategoryClassifyActionResultPropertiesHelper {

    private static MultiCategoryClassifyActionResultAccessor accessor;

    private MultiCategoryClassifyActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link MultiCategoryClassifyActionResult}
     * instance.
     */
    public interface MultiCategoryClassifyActionResultAccessor {
        void setDocumentsResults(MultiCategoryClassifyActionResult actionResult,
            MultiCategoryClassifyResultCollection documentsResults);
    }

    /**
     * The method called from {@link MultiCategoryClassifyActionResult} to set it's accessor.
     *
     * @param multiCategoryClassifyActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final MultiCategoryClassifyActionResultAccessor multiCategoryClassifyActionResultAccessor) {
        accessor = multiCategoryClassifyActionResultAccessor;
    }

    public static void setDocumentsResults(MultiCategoryClassifyActionResult actionResult,
        MultiCategoryClassifyResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
