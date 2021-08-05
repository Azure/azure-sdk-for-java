// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

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
     * @return the primary network interface associated with this resource
     */
    Mono<NetworkInterface> getPrimaryNetworkInterfaceAsync();

    /** @return the resource id of the primary network interface associated with this resource */
    String primaryNetworkInterfaceId();

    /** @return the list of resource IDs of the network interfaces associated with this resource */
    List<String> networkInterfaceIds();
}
