// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.BooleanResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** A resolution for boolean expressions. */
@Immutable
public final class BooleanResolution extends BaseResolution {
    /*
     * The value property.
     */
    private boolean value;

    static {
        BooleanResolutionPropertiesHelper.setAccessor((booleanResolution, value) -> booleanResolution.setValue(value));
    }

    /**
     * Get the value property: The value property.
     *
     * @return the value value.
     */
    public boolean getValue() {
        return this.value;
    }

    private void setValue(boolean value) {
        this.value = value;
    }
}
