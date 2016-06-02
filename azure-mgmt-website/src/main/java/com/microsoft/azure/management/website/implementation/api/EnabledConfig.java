/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Enabled configuration.
 */
public class EnabledConfig {
    /**
     * Enabled.
     */
    private Boolean enabled;

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean enabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled the enabled value to set
     * @return the EnabledConfig object itself.
     */
    public EnabledConfig withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

}
