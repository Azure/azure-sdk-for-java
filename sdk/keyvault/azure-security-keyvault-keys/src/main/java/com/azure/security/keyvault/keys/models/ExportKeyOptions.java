// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;

/**
 * Represents the configurable options to export a key.
 */
@Fluent
public class ExportKeyOptions {
    /**
     * The target environment assertion.
     */
    private String environment;

    /**
     * Get the target environment assertion.
     *
     * @return The environment.
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Set the target environment assertion.
     *
     * @param environment The environment value to set.
     * @return The updated {@link ExportKeyOptions} object.
     */
    public ExportKeyOptions setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }
}
