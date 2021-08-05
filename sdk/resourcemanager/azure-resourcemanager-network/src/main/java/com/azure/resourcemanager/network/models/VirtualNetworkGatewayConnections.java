// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import reactor.core.publisher.Mono;

/** Entry point for virtual network gateway connections management API in Azure. */
@Fluent
public interface VirtualNetworkGatewayConnections
    extends SupportsCreating<VirtualNetworkGatewayConnection.DefinitionStages.Blank>,
        SupportsListing<VirtualNetworkGatewayConnection>,
        SupportsGettingByName<VirtualNetworkGatewayConnection>,
        SupportsGettingById<VirtualNetworkGatewayConnection>,
        SupportsDeletingByName,
        SupportsDeletingById,
        HasParent<VirtualNetworkGateway> {

    /**
     * Gets the shared key of the virtual network gateway connection by resource ID.
     *
     * @param id the resource ID.
     * @return the shared key.
     */
    String getSharedKeyById(String id);

    /**
     * Gets the shared key of the virtual network gateway connection by resource ID.
     *
     * @param id the resource ID.
     * @return A {@link Mono} that emits the found resource asynchronously.
     */
    Mono<String> getSharedKeyByIdAsync(String id);

    /**
     * Sets the shared key of the virtual network gateway connection.
     *
     * @param id the resource ID.
     * @param sharedKey the shared key.
     * @return the shared key.
     */
    String setSharedKeyById(String id, String sharedKey);

    /**
     * Sets the shared key of the virtual network gateway connection.
     *
     * @param id the resource ID.
     * @param sharedKey the shared key.
     * @return A {@link Mono} that emits the found resource asynchronously.
     */
    Mono<String> setSharedKeyByIdAsync(String id, String sharedKey);

    /**
     * Sets the shared key of the virtual network gateway connection.
     *
     * @param name the resource name.
     * @param sharedKey the shared key.
     * @return the shared key.
     */
    String setSharedKeyByName(String name, String sharedKey);

    /**
     * Sets the shared key of the virtual network gateway connection.
     *
     * @param name the resource name.
     * @param sharedKey the shared key.
     * @return A {@link Mono} that emits the found resource asynchronously.
     */
    Mono<String> setSharedKeyByNameAsync(String name, String sharedKey);
}
