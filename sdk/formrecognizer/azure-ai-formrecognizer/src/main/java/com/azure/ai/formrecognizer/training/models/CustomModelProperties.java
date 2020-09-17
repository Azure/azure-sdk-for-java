// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training.models;

import com.azure.core.annotation.Immutable;

@Immutable
public final class CustomModelProperties {
    private boolean compositeModel;

    public boolean isCompositeModel() {
        return this.compositeModel;
    }
}
