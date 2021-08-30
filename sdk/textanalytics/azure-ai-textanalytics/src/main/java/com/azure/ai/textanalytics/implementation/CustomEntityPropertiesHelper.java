// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.CustomEntity;
import com.azure.ai.textanalytics.models.CustomEntityCategory;

/**
 * The helper class to set the non-public properties of an {@link CustomEntity} instance.
 */
public final class CustomEntityPropertiesHelper {
    private static CustomEntityAccessor accessor;

    private CustomEntityPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomEntity} instance.
     */
    public interface CustomEntityAccessor {
        void setText(CustomEntity entity, String text);
        void setCategory(CustomEntity entity, CustomEntityCategory category);
        void setSubcategory(CustomEntity entity, String subcategory);
        void setConfidenceScore(CustomEntity entity, double confidenceScore);
        void setOffset(CustomEntity entity, int offset);
        void setLength(CustomEntity entity, int length);
    }

    /**
     * The method called from {@link CustomEntity} to set it's accessor.
     *
     * @param entityAccessor The accessor.
     */
    public static void setAccessor(final CustomEntityAccessor entityAccessor) {
        accessor = entityAccessor;
    }

    public static void setText(CustomEntity entity, String text) {
        accessor.setText(entity, text);
    }

    public static void setCategory(CustomEntity entity, CustomEntityCategory category) {
        accessor.setCategory(entity, category);
    }

    public static void setSubcategory(CustomEntity entity, String subcategory) {
        accessor.setSubcategory(entity, subcategory);
    }

    public static void setConfidenceScore(CustomEntity entity, double confidenceScore) {
        accessor.setConfidenceScore(entity, confidenceScore);
    }

    public static void setOffset(CustomEntity entity, int offset) {
        accessor.setOffset(entity, offset);
    }

    public static void setLength(CustomEntity entity, int length) {
        accessor.setLength(entity, length);
    }
}
