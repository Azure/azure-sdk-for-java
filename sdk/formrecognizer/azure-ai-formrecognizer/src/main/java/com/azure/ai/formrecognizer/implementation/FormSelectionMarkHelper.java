// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.models.FormSelectionMark;
import com.azure.ai.formrecognizer.models.SelectionMarkState;

/**
 * The helper class to set the non-public properties of an {@link FormSelectionMark} instance.
 */
public final class FormSelectionMarkHelper {
    private static FormSelectionMarkAccessor accessor;

    private FormSelectionMarkHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link FormSelectionMark} instance.
     */
    public interface FormSelectionMarkAccessor {
        void setConfidence(FormSelectionMark selectionMark, float confidence);
        void setState(FormSelectionMark selectionMark, SelectionMarkState state);
    }

    /**
     * The method called from {@link FormSelectionMark} to set it's accessor.
     *
     * @param selectionMarkAccessor The accessor.
     */
    public static void setAccessor(final FormSelectionMarkAccessor selectionMarkAccessor) {
        accessor = selectionMarkAccessor;
    }

    public static void setConfidence(FormSelectionMark selectionMark, float confidence) {
        accessor.setConfidence(selectionMark, confidence);
    }

    public static void setState(FormSelectionMark selectionMark, SelectionMarkState state) {
        accessor.setState(selectionMark, state);
    }
}
