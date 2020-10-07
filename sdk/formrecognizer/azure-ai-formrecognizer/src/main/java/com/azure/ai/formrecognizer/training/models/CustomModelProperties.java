// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training.models;

import com.azure.core.annotation.Immutable;

/**
 * The metadata poperties for a custom model.
 */
public final class CustomModelProperties {
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
