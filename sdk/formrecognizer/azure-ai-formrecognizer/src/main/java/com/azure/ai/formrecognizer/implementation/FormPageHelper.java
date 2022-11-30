// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormSelectionMark;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link FormPage} instance.
 */
public final class FormPageHelper {
    private static FormPageAccessor accessor;

    private FormPageHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link FormPage} instance.
     */
    public interface FormPageAccessor {
        void setSelectionMarks(FormPage formPage, List<FormSelectionMark> selectionMarks);
    }

    /**
     * The method called from {@link FormPage} to set it's accessor.
     *
     * @param pageAccessor The accessor.
     */
    public static void setAccessor(final FormPageAccessor pageAccessor) {
        accessor = pageAccessor;
    }

    public static void setSelectionMarks(FormPage formPage, List<FormSelectionMark> selectionMarks) {
        accessor.setSelectionMarks(formPage, selectionMarks);
    }
}
