/**
 * Object]
 */

package com.microsoft.azure.management.network.models;


/**
 * Response for GetConnectionSharedKey Api servive call.
 */
public class ConnectionSharedKey {
    /**
     * The virtual network connection shared key value.
     */
    private String value;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}
