// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.models.Style;
import com.azure.ai.formrecognizer.models.TextStyle;

/**
 * The helper class to set the non-public properties of an {@link Style} instance.
 */
public final class StyleHelper {
    private static StyleAccessor accessor;

    private StyleHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link Style} instance.
     */
    public interface StyleAccessor {
        void setName(Style style, TextStyle name);
        void setConfidence(Style style, float confidence);
    }

    /**
     * The method called from {@link Style} to set it's accessor.
     *
     * @param styleAccessor The accessor.
     */
    public static void setAccessor(final StyleAccessor styleAccessor) {
        accessor = styleAccessor;
    }

    public static void setName(Style style, TextStyle name) {
        accessor.setName(style, name);
    }

    public static void setConfidence(Style style, float confidence) {
        accessor.setConfidence(style, confidence);
    }
}
