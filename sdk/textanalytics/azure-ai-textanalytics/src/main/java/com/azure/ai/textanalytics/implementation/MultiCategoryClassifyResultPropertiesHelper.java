// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.MultiCategoryClassifyResult;
import com.azure.ai.textanalytics.models.ClassificationCategoryCollection;

/**
 * The helper class to set the non-public properties of an {@link MultiCategoryClassifyResult} instance.
 */
public final class MultiCategoryClassifyResultPropertiesHelper {
    private static MultiCategoryClassifyResultAccessor accessor;

    private MultiCategoryClassifyResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link MultiCategoryClassifyResult} instance.
     */
    public interface MultiCategoryClassifyResultAccessor {
        void setClassifications(MultiCategoryClassifyResult multiCategoryClassifyResult,
            ClassificationCategoryCollection classifications);
    }

    /**
     * The method called from {@link MultiCategoryClassifyResult} to set it's accessor.
     *
     * @param multiCategoryClassifyResultAccessor The accessor.
     */
    public static void setAccessor(final MultiCategoryClassifyResultAccessor multiCategoryClassifyResultAccessor) {
        accessor = multiCategoryClassifyResultAccessor;
    }

    public static void setClassifications(MultiCategoryClassifyResult multiCategoryClassifyResult,
        ClassificationCategoryCollection classifications) {
        accessor.setClassifications(multiCategoryClassifyResult, classifications);
    }
}
