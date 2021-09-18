// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyDocumentMultiCategoriesResult;
import com.azure.ai.textanalytics.models.DocumentClassificationCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyDocumentMultiCategoriesResult} instance.
 */
public final class ClassifyCustomCategoriesResultPropertiesHelper {
    private static ClassifyCustomCategoriesResultAccessor accessor;

    private ClassifyCustomCategoriesResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyDocumentMultiCategoriesResult} instance.
     */
    public interface ClassifyCustomCategoriesResultAccessor {
        void setDocumentClassifications(ClassifyDocumentMultiCategoriesResult classifyDocumentMultiCategoriesResult,
            DocumentClassificationCollection documentClassifications);
    }

    /**
     * The method called from {@link ClassifyDocumentMultiCategoriesResult} to set it's accessor.
     *
     * @param classifyCustomCategoriesResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifyCustomCategoriesResultAccessor classifyCustomCategoriesResultAccessor) {
        accessor = classifyCustomCategoriesResultAccessor;
    }

    public static void setDocumentClassifications(ClassifyDocumentMultiCategoriesResult classifyDocumentMultiCategoriesResult,
        DocumentClassificationCollection documentClassifications) {
        accessor.setDocumentClassifications(classifyDocumentMultiCategoriesResult, documentClassifications);
    }
}
