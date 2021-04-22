// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models;

/** An interface representing a private endpoint connection. */
public interface PrivateEndpointConnection {

    /** @return the ID of the resource. */
    String id();

    /** @return the name of the resource. */
    String name();

    /** @return the type of the resource. */
    String type();

    /** @return the private endpoint. */
    PrivateEndpoint privateEndpoint();

    /** @return the state of the private link service connection. */
    PrivateLinkServiceConnectionState privateLinkServiceConnectionState();

    /** @return the provisioning state. */
    PrivateEndpointConnectionProvisioningState provisioningState();
}
