// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.CategorizedEntity;

/**
 * The helper class to set the non-public properties of an {@link CategorizedEntity} instance.
 */
public final class CategorizedEntityPropertiesHelper {
    private static CategorizedEntityAccessor accessor;

    private CategorizedEntityPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CategorizedEntity} instance.
     */
    public interface CategorizedEntityAccessor {
        void setLength(CategorizedEntity entity, int length);
        void setOffset(CategorizedEntity entity, int offset);
    }

    /**
     * The method called from {@link CategorizedEntity} to set it's accessor.
     *
     * @param entityAccessor The accessor.
     */
    public static void setAccessor(final CategorizedEntityAccessor entityAccessor) {
        accessor = entityAccessor;
    }

    public static void setLength(CategorizedEntity entity, int length) {
        accessor.setLength(entity, length);
    }

    public static void setOffset(CategorizedEntity entity, int offset) {
        accessor.setOffset(entity, offset);
    }
}
