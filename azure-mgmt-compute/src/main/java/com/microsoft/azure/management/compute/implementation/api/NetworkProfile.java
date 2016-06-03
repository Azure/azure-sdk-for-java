/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

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
    public List<NetworkInterfaceReference> networkInterfaces() {
        return this.networkInterfaces;
    }

    /**
     * Set the networkInterfaces value.
     *
     * @param networkInterfaces the networkInterfaces value to set
     * @return the NetworkProfile object itself.
     */
    public NetworkProfile withNetworkInterfaces(List<NetworkInterfaceReference> networkInterfaces) {
        this.networkInterfaces = networkInterfaces;
        return this;
    }

}
