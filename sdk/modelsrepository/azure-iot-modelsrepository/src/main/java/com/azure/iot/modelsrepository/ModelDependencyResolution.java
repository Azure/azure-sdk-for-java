// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

/**
 * The model dependency resolution options.
 */
public enum ModelDependencyResolution {
    /**
     * Disable model dependency resolution.
     */
    DISABLED,

    /**
     * Enable model dependency resolution. The client will parse models and calculate dependencies recursively.
     */
    ENABLED,

    /**
     * Try to get pre-computed model dependencies using .expanded.json.
     * If the model expanded form does not exist, it will fall back to {@link ModelDependencyResolution#ENABLED}.
     */
    TRY_FROM_EXPANDED,
}
