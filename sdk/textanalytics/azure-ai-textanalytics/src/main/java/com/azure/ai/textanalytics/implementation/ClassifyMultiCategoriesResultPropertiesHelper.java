// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyMultiCategoriesResult;
import com.azure.ai.textanalytics.models.DocumentClassificationCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyMultiCategoriesResult} instance.
 */
public final class ClassifyMultiCategoriesResultPropertiesHelper {
    private static ClassifyMultiCategoriesResultAccessor accessor;

    private ClassifyMultiCategoriesResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyMultiCategoriesResult} instance.
     */
    public interface ClassifyMultiCategoriesResultAccessor {
        void setDocumentClassifications(ClassifyMultiCategoriesResult classifyMultiCategoriesResult,
            DocumentClassificationCollection documentClassifications);
    }

    /**
     * The method called from {@link ClassifyMultiCategoriesResult} to set it's accessor.
     *
     * @param classifyMultiCategoriesResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifyMultiCategoriesResultAccessor classifyMultiCategoriesResultAccessor) {
        accessor = classifyMultiCategoriesResultAccessor;
    }

    public static void setDocumentClassifications(ClassifyMultiCategoriesResult classifyMultiCategoriesResult,
        DocumentClassificationCollection documentClassifications) {
        accessor.setDocumentClassifications(classifyMultiCategoriesResult, documentClassifications);
    }
}
