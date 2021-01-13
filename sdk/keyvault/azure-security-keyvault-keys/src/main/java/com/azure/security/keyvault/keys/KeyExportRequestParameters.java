// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The parameters for the key export operation.
 */
@Fluent
class KeyExportRequestParameters {
    /**
     * The target environment assertion.
     */
    @JsonProperty(value = "env")
    private String environment;

    /**
     * Get the target environment assertion.
     *
     * @return The environment.
     */
    public String getEnvironment() {
        return this.environment;
    }

    /**
     * Set the target environment assertion.
     *
     * @param environment The environment value to set.
     * @return The updated {@link KeyExportRequestParameters} object.
     */
    public KeyExportRequestParameters setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }
}
