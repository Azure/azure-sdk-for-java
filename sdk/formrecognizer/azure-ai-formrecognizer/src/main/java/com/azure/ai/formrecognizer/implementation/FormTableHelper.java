// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.models.FieldBoundingBox;
import com.azure.ai.formrecognizer.models.FormTable;

/**
 * The helper class to set the non-public properties of an {@link FormTable} instance.
 */
public final class FormTableHelper {
    private static FormTableAccessor accessor;

    private FormTableHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link FormTable} instance.
     */
    public interface FormTableAccessor {
        void setBoundingBox(FormTable formTable, FieldBoundingBox boundingBox);
    }

    /**
     * The method called from {@link FormTable} to set it's accessor.
     *
     * @param formTableAccessor The accessor.
     */
    public static void setAccessor(final FormTableAccessor formTableAccessor) {
        accessor = formTableAccessor;
    }

    public static void setBoundingBox(FormTable formTable, FieldBoundingBox boundingBox) {
        accessor.setBoundingBox(formTable, boundingBox);
    }
}
