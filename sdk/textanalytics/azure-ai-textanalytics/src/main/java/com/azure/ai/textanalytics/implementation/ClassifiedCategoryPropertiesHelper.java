// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifiedCategory;

/**
 * The helper class to set the non-public properties of an {@link ClassifiedCategory} instance.
 */
public final class ClassifiedCategoryPropertiesHelper {
    private static ClassifiedCategoryAccessor accessor;

    private ClassifiedCategoryPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifiedCategory} instance.
     */
    public interface ClassifiedCategoryAccessor {
        void setCategory(ClassifiedCategory classifiedCategory, String category);
        void setConfidenceScore(ClassifiedCategory classifiedCategory, double confidenceScore);
    }

    /**
     * The method called from {@link ClassifiedCategory} to set it's accessor.
     *
     * @param classifiedCategoryAccessor The accessor.
     */
    public static void setAccessor(final ClassifiedCategoryAccessor classifiedCategoryAccessor) {
        accessor = classifiedCategoryAccessor;
    }

    public static void setCategory(ClassifiedCategory classifiedCategory, String category) {
        accessor.setCategory(classifiedCategory, category);
    }

    public static void setConfidenceScore(ClassifiedCategory classifiedCategory, double confidenceScore) {
        accessor.setConfidenceScore(classifiedCategory, confidenceScore);
    }
}
