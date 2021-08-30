// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.CustomClassifyMultiCategoriesResult;
import com.azure.ai.textanalytics.models.DocumentClassificationCollection;

/**
 * The helper class to set the non-public properties of an {@link CustomClassifyMultiCategoriesResult} instance.
 */
public final class CustomClassifyMultiCategoriesResultPropertiesHelper {
    private static ClassifyMultiCategoriesResultAccessor accessor;

    private CustomClassifyMultiCategoriesResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomClassifyMultiCategoriesResult} instance.
     */
    public interface ClassifyMultiCategoriesResultAccessor {
        void setDocumentClassifications(CustomClassifyMultiCategoriesResult customClassifyMultiCategoriesResult,
            DocumentClassificationCollection documentClassifications);
    }

    /**
     * The method called from {@link CustomClassifyMultiCategoriesResult} to set it's accessor.
     *
     * @param classifyMultiCategoriesResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifyMultiCategoriesResultAccessor classifyMultiCategoriesResultAccessor) {
        accessor = classifyMultiCategoriesResultAccessor;
    }

    public static void setDocumentClassifications(CustomClassifyMultiCategoriesResult customClassifyMultiCategoriesResult,
        DocumentClassificationCollection documentClassifications) {
        accessor.setDocumentClassifications(customClassifyMultiCategoriesResult, documentClassifications);
    }
}
