// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyMultiCategoriesResult;
import com.azure.ai.textanalytics.models.DocumentClassificationCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyMultiCategoriesResult} instance.
 */
public final class ClassifyCustomCategoriesResultPropertiesHelper {
    private static ClassifyCustomCategoriesResultAccessor accessor;

    private ClassifyCustomCategoriesResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyMultiCategoriesResult} instance.
     */
    public interface ClassifyCustomCategoriesResultAccessor {
        void setDocumentClassifications(ClassifyMultiCategoriesResult classifyMultiCategoriesResult,
            DocumentClassificationCollection documentClassifications);
    }

    /**
     * The method called from {@link ClassifyMultiCategoriesResult} to set it's accessor.
     *
     * @param classifyCustomCategoriesResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifyCustomCategoriesResultAccessor classifyCustomCategoriesResultAccessor) {
        accessor = classifyCustomCategoriesResultAccessor;
    }

    public static void setDocumentClassifications(ClassifyMultiCategoriesResult classifyMultiCategoriesResult,
        DocumentClassificationCollection documentClassifications) {
        accessor.setDocumentClassifications(classifyMultiCategoriesResult, documentClassifications);
    }
}
