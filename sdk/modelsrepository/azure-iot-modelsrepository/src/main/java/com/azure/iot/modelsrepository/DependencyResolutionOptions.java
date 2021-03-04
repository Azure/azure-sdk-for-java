package com.azure.iot.modelsrepository;

/*
The model dependency resolution options.
 */
public enum DependencyResolutionOptions {

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
     * If the model expanded form does not exist fall back to DependencyResolutionOption.Enabled processing.
     */
    TRY_FROM_EXPANDED,
}
