// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.util.Context;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import reactor.core.publisher.Mono;

import java.util.List;

/** Interface exposing a list of network interfaces. */
public interface HasNetworkInterfaces extends HasId {
    /**
     * Gets the primary network interface.
     *
     * <p>Note that this method can result in a call to the cloud to fetch the network interface information.
     *
     * @return the primary network interface associated with this resource
     */
    NetworkInterface getPrimaryNetworkInterface();

    /**
     * Gets the primary network interface.
     *
     * <p>Note that this method can result in a call to the cloud to fetch the network interface information.
     *
     * @param context the {@link Context} of the request
     * @return the primary network interface associated with this resource
     */
    default NetworkInterface getPrimaryNetworkInterface(Context context) {
        throw new UnsupportedOperationException(
            "[getPrimaryNetworkInterface(Context)] is not supported in " + getClass());
    }

    /**
     * Gets the primary network interface.
     *
     * <p>Note that this method can result in a call to the cloud to fetch the network interface information.
     *
     * @return the primary network interface associated with this resource
     */
    Mono<NetworkInterface> getPrimaryNetworkInterfaceAsync();

    /**
     * Gets the resource ID of the primary network interface associated with this resource.
     *
     * @return the resource ID of the primary network interface associated with this resource
     */
    String primaryNetworkInterfaceId();

    /**
     * Gets the list of resource IDs of the network interfaces associated with this resource.
     *
     * @return the list of resource IDs of the network interfaces associated with this resource
     */
    List<String> networkInterfaceIds();
}
