// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.CustomClassifySingleCategoryActionResult;
import com.azure.ai.textanalytics.util.CustomClassifySingleCategoryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link CustomClassifySingleCategoryActionResult} instance.
 */
public final class CustomClassifySingleCategoryActionResultPropertiesHelper {
    private static ClassifySingleCategoryActionResultAccessor accessor;

    private CustomClassifySingleCategoryActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomClassifySingleCategoryActionResult}
     * instance.
     */
    public interface ClassifySingleCategoryActionResultAccessor {
        void setDocumentsResults(CustomClassifySingleCategoryActionResult actionResult,
            CustomClassifySingleCategoryResultCollection documentsResults);
    }

    /**
     * The method called from {@link CustomClassifySingleCategoryActionResult} to set it's accessor.
     *
     * @param classifySingleCategoryActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifySingleCategoryActionResultAccessor classifySingleCategoryActionResultAccessor) {
        accessor = classifySingleCategoryActionResultAccessor;
    }

    public static void setDocumentsResults(CustomClassifySingleCategoryActionResult actionResult,
        CustomClassifySingleCategoryResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
