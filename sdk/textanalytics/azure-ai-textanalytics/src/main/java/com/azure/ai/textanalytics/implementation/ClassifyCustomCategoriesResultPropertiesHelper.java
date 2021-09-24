// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.MultiCategoryClassifyResult;
import com.azure.ai.textanalytics.models.ClassificationCategoryCollection;

/**
 * The helper class to set the non-public properties of an {@link MultiCategoryClassifyResult} instance.
 */
public final class ClassifyCustomCategoriesResultPropertiesHelper {
    private static ClassifyCustomCategoriesResultAccessor accessor;

    private ClassifyCustomCategoriesResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link MultiCategoryClassifyResult} instance.
     */
    public interface ClassifyCustomCategoriesResultAccessor {
        void setDocumentClassifications(MultiCategoryClassifyResult multiCategoryClassifyResult,
            ClassificationCategoryCollection documentClassifications);
    }

    /**
     * The method called from {@link MultiCategoryClassifyResult} to set it's accessor.
     *
     * @param classifyCustomCategoriesResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifyCustomCategoriesResultAccessor classifyCustomCategoriesResultAccessor) {
        accessor = classifyCustomCategoriesResultAccessor;
    }

    public static void setDocumentClassifications(MultiCategoryClassifyResult multiCategoryClassifyResult,
        ClassificationCategoryCollection documentClassifications) {
        accessor.setDocumentClassifications(multiCategoryClassifyResult, documentClassifications);
    }
}
