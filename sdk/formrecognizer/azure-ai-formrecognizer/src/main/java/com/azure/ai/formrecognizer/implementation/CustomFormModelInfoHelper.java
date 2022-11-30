// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.training.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.training.models.CustomFormModelProperties;

/**
 * The helper class to set the non-public properties of an {@link CustomFormModelInfo} instance.
 */
public final class CustomFormModelInfoHelper {
    private static CustomFormModelInfoAccessor accessor;

    private CustomFormModelInfoHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomFormModelInfo} instance.
     */
    public interface CustomFormModelInfoAccessor {
        void setModelName(CustomFormModelInfo formModelInfo, String modelName);
        void setCustomFormModelProperties(CustomFormModelInfo formModelInfo,
                                          CustomFormModelProperties customFormModelProperties);
    }

    /**
     * The method called from {@link CustomFormModelInfo} to set it's accessor.
     *
     * @param formModelInfoAccessor The accessor.
     */
    public static void setAccessor(final CustomFormModelInfoAccessor formModelInfoAccessor) {
        accessor = formModelInfoAccessor;
    }

    public static void setModelName(CustomFormModelInfo formModelInfo, String modelName) {
        accessor.setModelName(formModelInfo, modelName);
    }

    public static void setCustomFormModelProperties(CustomFormModelInfo formModelInfo,
                                                    CustomFormModelProperties customFormModelProperties) {
        accessor.setCustomFormModelProperties(formModelInfo, customFormModelProperties);
    }
}
