// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models;

/** An interface representing a private endpoint connection. */
public interface PrivateEndpointConnection {

    /**
     * Gets the ID of the resource.
     *
     * @return the ID of the resource.
     */
    String id();

    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource.
     */
    String name();

    /**
     * Gets the type of the resource.
     *
     * @return the type of the resource.
     */
    String type();

    /**
     * Gets the private endpoint.
     *
     * @return the private endpoint.
     */
    PrivateEndpoint privateEndpoint();

    /**
     * Gets the state of the private link service connection.
     *
     * @return the state of the private link service connection.
     */
    PrivateLinkServiceConnectionState privateLinkServiceConnectionState();

    /**
     * Gets the provisioning state.
     *
     * @return the provisioning state.
     */
    PrivateEndpointConnectionProvisioningState provisioningState();
}
