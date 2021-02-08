// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.training.models.CustomFormModelProperties;

/**
 * The helper class to set the non-public properties of an {@link CustomFormModelProperties} instance.
 */
public final class CustomFormModelPropertiesHelper {
    private static CustomFormModelPropertiesAccessor accessor;

    private CustomFormModelPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomFormModelProperties} instance.
     */
    public interface CustomFormModelPropertiesAccessor {
        void setIsComposed(CustomFormModelProperties formModelProperties, boolean isComposed);
    }

    /**
     * The method called from {@link CustomFormModelProperties} to set it's accessor.
     *
     * @param formModelPropertiesAccessor The accessor.
     */
    public static void setAccessor(final CustomFormModelPropertiesAccessor formModelPropertiesAccessor) {
        accessor = formModelPropertiesAccessor;
    }

    public static void setIsComposed(CustomFormModelProperties formModelProperties, boolean isComposed) {
        accessor.setIsComposed(formModelProperties, isComposed);
    }
}
