// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models;

public final class PrivateLinkServiceConnectionState {

    private final PrivateEndpointServiceConnectionStatus status;
    private final String description;
    private final String actionsRequired;

    public PrivateLinkServiceConnectionState(PrivateEndpointServiceConnectionStatus status,
                                             String description, String actionsRequired) {
        this.status = status;
        this.description = description;
        this.actionsRequired = actionsRequired;
    }

    public PrivateEndpointServiceConnectionStatus status() {
        return this.status;
    }

    public String description() {
        return this.description;
    }

    public String actionsRequired() {
        return this.actionsRequired;
    }
}
