// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training.models;

import com.azure.ai.formrecognizer.implementation.CustomFormModelPropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The metadata properties for a custom model.
 */
@Immutable
public final class CustomFormModelProperties {
    private boolean isComposed;

    static {
        CustomFormModelPropertiesHelper.setAccessor(
            new CustomFormModelPropertiesHelper.CustomFormModelPropertiesAccessor() {
                @Override
                public void setIsComposed(CustomFormModelProperties formModelProperties, boolean isComposed) {
                    formModelProperties.setIsComposed(isComposed);
                }
            });
    }
    /**
     * Is this model composed?
     *
     * @return the isComposed value.
     */
    public boolean isComposed() {
        return this.isComposed;
    }

    /**
     * The private setter to set the state property
     * via {@link CustomFormModelPropertiesHelper.CustomFormModelPropertiesAccessor}.
     *
     * @param isComposed the isComposed value.
     */
    private void setIsComposed(boolean isComposed) {
        this.isComposed = isComposed;
    }
}
