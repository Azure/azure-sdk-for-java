/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;


/**
 * Describes a Virtual Machine Scale Set Extension.
 */
public class VirtualMachineScaleSetExtension extends SubResource {
    /**
     * Gets or sets the name of the extension.
     */
    private String name;

    /**
     * The properties property.
     */
    private VirtualMachineScaleSetExtensionProperties properties;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public VirtualMachineScaleSetExtensionProperties getProperties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     */
    public void setProperties(VirtualMachineScaleSetExtensionProperties properties) {
        this.properties = properties;
    }

}
