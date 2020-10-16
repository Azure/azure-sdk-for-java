// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training.models;

/**
 * The metadata properties for a custom model.
 */
public final class CustomFormModelProperties {
    private boolean isComposed;

    /**
     * Is this model composed?
     *
     * @return the isComposed value.
     */
    public boolean isComposed() {
        return this.isComposed;
    }
}
