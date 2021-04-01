// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models;

public interface PrivateEndpointConnection {

    String id();

    String name();

    String type();

    PrivateEndpoint privateEndpoint();

    PrivateLinkServiceConnectionState privateLinkServiceConnectionState();

    PrivateEndpointConnectionProvisioningState provisioningState();
}
