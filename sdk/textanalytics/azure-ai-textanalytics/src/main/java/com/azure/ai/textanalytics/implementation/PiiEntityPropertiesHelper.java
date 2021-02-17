// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.PiiEntity;

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

    public static void setLength(PiiEntity entity, int length) {
        accessor.setLength(entity, length);
    }
}
