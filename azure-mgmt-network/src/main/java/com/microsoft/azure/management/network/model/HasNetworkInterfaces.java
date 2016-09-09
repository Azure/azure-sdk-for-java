/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.model;

import java.io.IOException;
import java.util.List;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.network.NetworkInterface;

/**
 * Interface exposing a list of network interfaces.
 */
public interface HasNetworkInterfaces {
    /**
     * Gets the primary network interface.
     * <p>
     * Note that this method can result in a call to the cloud to fetch the network interface information.
     *
     * @return the primary network interface associated with this resource
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    NetworkInterface primaryNetworkInterface() throws CloudException, IOException;

    /**
     * @return the resource id of the primary network interface associated with this resource
     */
    String primaryNetworkInterfaceId();

    /**
     * @return the list of resource IDs of the network interfaces associated with this resource
     */
    List<String> networkInterfaceIds();
}
