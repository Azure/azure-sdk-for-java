// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training.models;

/**
 * The metadata properties for a custom model.
 */
public final class CustomFormModelProperties {
    private final boolean isComposed;

    // TODO: remove this constructor
    /**
     * Create an instance of CustomFormModelProperties
     *
     * @param isComposed is composed model.
     */
    public CustomFormModelProperties(final boolean isComposed) {
        this.isComposed = isComposed;
    }

    /**
     * Is this model composed?
     *
     * @return the isComposed value.
     */
    public boolean isComposed() {
        return this.isComposed;
    }
}
