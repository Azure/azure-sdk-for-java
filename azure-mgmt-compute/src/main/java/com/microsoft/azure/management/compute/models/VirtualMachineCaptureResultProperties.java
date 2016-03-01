/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;


/**
 * Compute-specific operation properties, including output.
 */
public class VirtualMachineCaptureResultProperties {
    /**
     * Operation output data (raw JSON).
     */
    private Object output;

    /**
     * Get the output value.
     *
     * @return the output value
     */
    public Object getOutput() {
        return this.output;
    }

    /**
     * Set the output value.
     *
     * @param output the output value to set
     */
    public void setOutput(Object output) {
        this.output = output;
    }

}
