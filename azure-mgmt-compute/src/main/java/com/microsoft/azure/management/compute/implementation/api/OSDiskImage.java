/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains the os disk image information.
 */
public class OSDiskImage {
    /**
     * Gets or sets the operating system of the osDiskImage. Possible values
     * include: 'Windows', 'Linux'.
     */
    @JsonProperty(required = true)
    private OperatingSystemTypes operatingSystem;

    /**
     * Get the operatingSystem value.
     *
     * @return the operatingSystem value
     */
    public OperatingSystemTypes operatingSystem() {
        return this.operatingSystem;
    }

    /**
     * Set the operatingSystem value.
     *
     * @param operatingSystem the operatingSystem value to set
     * @return the OSDiskImage object itself.
     */
    public OSDiskImage withOperatingSystem(OperatingSystemTypes operatingSystem) {
        this.operatingSystem = operatingSystem;
        return this;
    }

}
