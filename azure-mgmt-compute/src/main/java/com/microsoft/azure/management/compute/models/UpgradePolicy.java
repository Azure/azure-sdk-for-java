/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.models;


/**
 * Describes an upgrade policy - automatic or manual.
 */
public class UpgradePolicy {
    /**
     * Gets or sets the upgrade mode. Possible values include: 'Automatic',
     * 'Manual'.
     */
    private String mode;

    /**
     * Get the mode value.
     *
     * @return the mode value
     */
    public String getMode() {
        return this.mode;
    }

    /**
     * Set the mode value.
     *
     * @param mode the mode value to set
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

}
