/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;


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
    public ImageReference getImageReference() {
        return this.imageReference;
    }

    /**
     * Set the imageReference value.
     *
     * @param imageReference the imageReference value to set
     */
    public void setImageReference(ImageReference imageReference) {
        this.imageReference = imageReference;
    }

    /**
     * Get the osDisk value.
     *
     * @return the osDisk value
     */
    public VirtualMachineScaleSetOSDisk getOsDisk() {
        return this.osDisk;
    }

    /**
     * Set the osDisk value.
     *
     * @param osDisk the osDisk value to set
     */
    public void setOsDisk(VirtualMachineScaleSetOSDisk osDisk) {
        this.osDisk = osDisk;
    }

}
