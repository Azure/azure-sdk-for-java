/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;


/**
 * Resource Id.
 */
public class VirtualMachineCaptureResult extends SubResource {
    /**
     * The properties property.
     */
    private VirtualMachineCaptureResultProperties properties;

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public VirtualMachineCaptureResultProperties getProperties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     */
    public void setProperties(VirtualMachineCaptureResultProperties properties) {
        this.properties = properties;
    }

}
