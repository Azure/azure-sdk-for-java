// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassificationCategory;

/**
 * The helper class to set the non-public properties of an {@link ClassificationCategory} instance.
 */
public final class ClassificationCategoryPropertiesHelper {
    private static ClassificationCategoryAccessor accessor;

    private ClassificationCategoryPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassificationCategory} instance.
     */
    public interface ClassificationCategoryAccessor {
        void setCategory(ClassificationCategory classificationCategory, String category);
        void setConfidenceScore(ClassificationCategory classificationCategory, double confidenceScore);
    }

    /**
     * The method called from {@link ClassificationCategory} to set it's accessor.
     *
     * @param classificationCategoryAccessor The accessor.
     */
    public static void setAccessor(final ClassificationCategoryAccessor classificationCategoryAccessor) {
        accessor = classificationCategoryAccessor;
    }

    public static void setCategory(ClassificationCategory classificationCategory, String category) {
        accessor.setCategory(classificationCategory, category);
    }

    public static void setConfidenceScore(ClassificationCategory classificationCategory, double confidenceScore) {
        accessor.setConfidenceScore(classificationCategory, confidenceScore);
    }
}
