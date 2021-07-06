// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.LinkedEntity;

/**
 * The helper class to set the non-public properties of an {@link LinkedEntity} instance.
 */
public final class LinkedEntityPropertiesHelper {
    private static LinkedEntityAccessor accessor;

    private LinkedEntityPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link LinkedEntity} instance.
     */
    public interface LinkedEntityAccessor {
        void setBingEntitySearchApiId(LinkedEntity entity, String bingEntitySearchApiId);
    }

    /**
     * The method called from {@link LinkedEntity} to set it's accessor.
     *
     * @param entityAccessor The accessor.
     */
    public static void setAccessor(final LinkedEntityAccessor entityAccessor) {
        accessor = entityAccessor;
    }

    public static void setBingEntitySearchApiId(LinkedEntity entity, String bingEntitySearchApiId) {
        accessor.setBingEntitySearchApiId(entity, bingEntitySearchApiId);
    }
}
