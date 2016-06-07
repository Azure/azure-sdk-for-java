/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;


/**
 * Describes a virtual machine scale set storage profile.
 */
public class VirtualMachineScaleSetStorageProfile {
    /**
     * Gets or sets the image reference.
     */
    private ImageReference imageReference;

    /**
     * Gets or sets the OS disk.
     */
    private VirtualMachineScaleSetOSDisk osDisk;

    /**
     * Get the imageReference value.
     *
     * @return the imageReference value
     */
    public ImageReference imageReference() {
        return this.imageReference;
    }

    /**
     * Set the imageReference value.
     *
     * @param imageReference the imageReference value to set
     * @return the VirtualMachineScaleSetStorageProfile object itself.
     */
    public VirtualMachineScaleSetStorageProfile withImageReference(ImageReference imageReference) {
        this.imageReference = imageReference;
        return this;
    }

    /**
     * Get the osDisk value.
     *
     * @return the osDisk value
     */
    public VirtualMachineScaleSetOSDisk osDisk() {
        return this.osDisk;
    }

    /**
     * Set the osDisk value.
     *
     * @param osDisk the osDisk value to set
     * @return the VirtualMachineScaleSetStorageProfile object itself.
     */
    public VirtualMachineScaleSetStorageProfile withOsDisk(VirtualMachineScaleSetOSDisk osDisk) {
        this.osDisk = osDisk;
        return this;
    }

}
