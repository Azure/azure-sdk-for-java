// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.models.TextStyle;
import com.azure.ai.formrecognizer.models.TextStyleName;

/**
 * The helper class to set the non-public properties of an {@link TextStyle} instance.
 */
public final class TextStyleHelper {
    private static StyleAccessor accessor;

    private TextStyleHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link TextStyle} instance.
     */
    public interface StyleAccessor {
        void setName(TextStyle textStyle, TextStyleName name);
        void setConfidence(TextStyle textStyle, float confidence);
    }

    /**
     * The method called from {@link TextStyle} to set it's accessor.
     *
     * @param styleAccessor The accessor.
     */
    public static void setAccessor(final StyleAccessor styleAccessor) {
        accessor = styleAccessor;
    }

    public static void setName(TextStyle textStyle, TextStyleName name) {
        accessor.setName(textStyle, name);
    }

    public static void setConfidence(TextStyle textStyle, float confidence) {
        accessor.setConfidence(textStyle, confidence);
    }
}
