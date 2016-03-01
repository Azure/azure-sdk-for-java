/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;

import java.util.List;

/**
 * Describes a network profile.
 */
public class NetworkProfile {
    /**
     * Gets or sets the network interfaces.
     */
    private List<NetworkInterfaceReference> networkInterfaces;

    /**
     * Get the networkInterfaces value.
     *
     * @return the networkInterfaces value
     */
    public List<NetworkInterfaceReference> getNetworkInterfaces() {
        return this.networkInterfaces;
    }

    /**
     * Set the networkInterfaces value.
     *
     * @param networkInterfaces the networkInterfaces value to set
     */
    public void setNetworkInterfaces(List<NetworkInterfaceReference> networkInterfaces) {
        this.networkInterfaces = networkInterfaces;
    }

}
