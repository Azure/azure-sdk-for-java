/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;

/**
 * Describes a virtual machine scale set extension profile.
 */
public class VirtualMachineScaleSetExtensionProfile {
    /**
     * Gets the virtual machine scale set child extension resources.
     */
    private List<VirtualMachineScaleSetExtension> extensions;

    /**
     * Get the extensions value.
     *
     * @return the extensions value
     */
    public List<VirtualMachineScaleSetExtension> extensions() {
        return this.extensions;
    }

    /**
     * Set the extensions value.
     *
     * @param extensions the extensions value to set
     * @return the VirtualMachineScaleSetExtensionProfile object itself.
     */
    public VirtualMachineScaleSetExtensionProfile withExtensions(List<VirtualMachineScaleSetExtension> extensions) {
        this.extensions = extensions;
        return this;
    }

}
