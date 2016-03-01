/**
 * Object]
 */

package com.microsoft.azure.management.network.models;


/**
 * Response for CheckDnsNameAvailability Api servive call.
 */
public class DnsNameAvailabilityResult {
    /**
     * Domain availability (True/False).
     */
    private Boolean available;

    /**
     * Get the available value.
     *
     * @return the available value
     */
    public Boolean getAvailable() {
        return this.available;
    }

    /**
     * Set the available value.
     *
     * @param available the available value to set
     */
    public void setAvailable(Boolean available) {
        this.available = available;
    }

}
