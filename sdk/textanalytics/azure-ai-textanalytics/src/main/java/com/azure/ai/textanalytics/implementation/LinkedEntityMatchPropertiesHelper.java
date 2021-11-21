// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.LinkedEntityMatch;

/**
 * The helper class to set the non-public properties of an {@link LinkedEntityMatch} instance.
 */
public final class LinkedEntityMatchPropertiesHelper {
    private static LinkedEntityMatchAccessor accessor;

    private LinkedEntityMatchPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link LinkedEntityMatch} instance.
     */
    public interface LinkedEntityMatchAccessor {
        void setLength(LinkedEntityMatch entity, int length);
        void setOffset(LinkedEntityMatch entity, int offset);
    }

    /**
     * The method called from {@link LinkedEntityMatch} to set it's accessor.
     *
     * @param entityAccessor The accessor.
     */
    public static void setAccessor(final LinkedEntityMatchAccessor entityAccessor) {
        accessor = entityAccessor;
    }

    public static void setLength(LinkedEntityMatch entity, int length) {
        accessor.setLength(entity, length);
    }

    public static void setOffset(LinkedEntityMatch entity, int offset) {
        accessor.setOffset(entity, offset);
    }
}
