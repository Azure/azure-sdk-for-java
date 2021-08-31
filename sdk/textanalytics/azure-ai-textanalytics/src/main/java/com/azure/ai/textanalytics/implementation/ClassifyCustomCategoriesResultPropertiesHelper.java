// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyCustomCategoriesResult;
import com.azure.ai.textanalytics.models.DocumentClassificationCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyCustomCategoriesResult} instance.
 */
public final class ClassifyCustomCategoriesResultPropertiesHelper {
    private static ClassifyCustomCategoriesResultAccessor accessor;

    private ClassifyCustomCategoriesResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyCustomCategoriesResult} instance.
     */
    public interface ClassifyCustomCategoriesResultAccessor {
        void setDocumentClassifications(ClassifyCustomCategoriesResult classifyCustomCategoriesResult,
            DocumentClassificationCollection documentClassifications);
    }

    /**
     * The method called from {@link ClassifyCustomCategoriesResult} to set it's accessor.
     *
     * @param classifyCustomCategoriesResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifyCustomCategoriesResultAccessor classifyCustomCategoriesResultAccessor) {
        accessor = classifyCustomCategoriesResultAccessor;
    }

    public static void setDocumentClassifications(ClassifyCustomCategoriesResult classifyCustomCategoriesResult,
        DocumentClassificationCollection documentClassifications) {
        accessor.setDocumentClassifications(classifyCustomCategoriesResult, documentClassifications);
    }
}
