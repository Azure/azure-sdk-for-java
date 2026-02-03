// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models;

/** The state of the private link service connection. */
public final class PrivateLinkServiceConnectionState {

    private final PrivateEndpointServiceConnectionStatus status;
    private final String description;
    private final String actionsRequired;

    /**
     * Initializes a new instance of the {@link PrivateLinkServiceConnectionState} class.
     *
     * @param status the status of the connection.
     * @param description the description of the connection.
     * @param actionsRequired the required action for the connection.
     */
    public PrivateLinkServiceConnectionState(PrivateEndpointServiceConnectionStatus status, String description,
        String actionsRequired) {
        this.status = status;
        this.description = description;
        this.actionsRequired = actionsRequired;
    }

    /**
     * Gets the status of the connection.
     *
     * @return the status of the connection.
     */
    public PrivateEndpointServiceConnectionStatus status() {
        return this.status;
    }

    /**
     * Gets the description of the connection.
     *
     * @return the description of the connection.
     */
    public String description() {
        return this.description;
    }

    /**
     * Gets the required action for the connection.
     *
     * @return the required action for the connection.
     */
    public String actionsRequired() {
        return this.actionsRequired;
    }
}
