// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.models.TextAppearance;
import com.azure.ai.formrecognizer.models.FormLine;

/**
 * The helper class to set the non-public properties of an {@link FormLine} instance.
 */
public final class FormLineHelper {
    private static FormLineAccessor accessor;

    private FormLineHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link FormLine} instance.
     */
    public interface FormLineAccessor {
        void setAppearance(FormLine formLine, TextAppearance textAppearance);
    }

    /**
     * The method called from {@link FormLine} to set it's accessor.
     *
     * @param formLineAccessor The accessor.
     */
    public static void setAccessor(final FormLineAccessor formLineAccessor) {
        accessor = formLineAccessor;
    }

    public static void setAppearance(FormLine formLine, TextAppearance textAppearance) {
        accessor.setAppearance(formLine, textAppearance);
    }
}
