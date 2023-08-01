// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.training.models.CustomFormSubmodel;

/**
 * The helper class to set the non-public properties of an {@link CustomFormSubmodel} instance.
 */
public final class CustomFormSubmodelHelper {
    private static CustomFormSubmodelAccessor accessor;

    private CustomFormSubmodelHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomFormSubmodel} instance.
     */
    public interface CustomFormSubmodelAccessor {
        void setModelId(CustomFormSubmodel formSubmodel, String modelId);
    }

    /**
     * The method called from {@link CustomFormSubmodel} to set it's accessor.
     *
     * @param formSubmodelAccessor The accessor.
     */
    public static void setAccessor(final CustomFormSubmodelAccessor formSubmodelAccessor) {
        accessor = formSubmodelAccessor;
    }

    public static void setModelId(CustomFormSubmodel customFormSubmodel, String modelId) {
        accessor.setModelId(customFormSubmodel, modelId);
    }
}
