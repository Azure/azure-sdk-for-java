// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.PiiEntityCategory;

/**
 * The helper class to set the non-public properties of an {@link PiiEntity} instance.
 */
public final class PiiEntityPropertiesHelper {
    private static PiiEntityAccessor accessor;

    private PiiEntityPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link PiiEntity} instance.
     */
    public interface PiiEntityAccessor {
        void setText(PiiEntity entity, String text);
        void setCategory(PiiEntity entity, PiiEntityCategory category);
        void setSubcategory(PiiEntity entity, String subcategory);
        void setConfidenceScore(PiiEntity entity, double confidenceScore);
        void setOffset(PiiEntity entity, int offset);
        void setLength(PiiEntity entity, int length);
    }

    /**
     * The method called from {@link PiiEntity} to set it's accessor.
     *
     * @param entityAccessor The accessor.
     */
    public static void setAccessor(final PiiEntityAccessor entityAccessor) {
        accessor = entityAccessor;
    }

    public static void setText(PiiEntity entity, String text) {
        accessor.setText(entity, text);
    }

    public static void setCategory(PiiEntity entity, PiiEntityCategory category) {
        accessor.setCategory(entity, category);
    }

    public static void setSubcategory(PiiEntity entity, String subcategory) {
        accessor.setSubcategory(entity, subcategory);
    }

    public static void setConfidenceScore(PiiEntity entity, double confidenceScore) {
        accessor.setConfidenceScore(entity, confidenceScore);
    }

    public static void setOffset(PiiEntity entity, int offset) {
        accessor.setOffset(entity, offset);
    }

    public static void setLength(PiiEntity entity, int length) {
        accessor.setLength(entity, length);
    }
}
