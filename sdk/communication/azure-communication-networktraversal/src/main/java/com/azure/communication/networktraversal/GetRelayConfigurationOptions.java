// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.networktraversal;

import com.azure.communication.networktraversal.models.RouteType;
import com.azure.communication.common.CommunicationUserIdentifier;

/**
 * Additional options for getting a relay configuration.
 *
 */
public final class GetRelayConfigurationOptions {
    private CommunicationUserIdentifier communicationUser;
    private RouteType routeType;

    /**
     * Empty constructor for GetRelayConfigurationOptions
     *
     */
    public GetRelayConfigurationOptions() {

    }

    /**
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @param routeType The specified RouteType for the relay requests
     */
    public GetRelayConfigurationOptions(CommunicationUserIdentifier communicationUser, RouteType routeType) {
        this.communicationUser = communicationUser;
        this.routeType = routeType;
    }

    /**
     *
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     */
    public GetRelayConfigurationOptions(CommunicationUserIdentifier communicationUser) {
        this.communicationUser = communicationUser;
    }

    /**
     * Set the routeType property: The routing methodology to where the ICE server will be located from the client.
     *
     * @param routeType The specified RouteType for the relay request
     */
    public GetRelayConfigurationOptions(RouteType routeType) {
        this.routeType = routeType;
    }

    /**
     * Get the communicationUser property: The CommunicationUserIdentifier for whom to issue a token.
     *
     * @return the communicationUser value.
    */
    public CommunicationUserIdentifier getCommunicationUserIdentifier() {
        return this.communicationUser;
    }

    /**
     * Set the communicationUser property: The CommunicationUserIdentifier for whom to issue a token
     *
     * @param communicationUser the communicationUser value to set.
     */
    public void setCommunicationUserIdentifier(CommunicationUserIdentifier communicationUser) {
        this.communicationUser = communicationUser;
    }

    /**
     * Get the routeType property: The routing methodology to where the ICE server will be located from the client.
     *
     * @return the routeType value.
    */
    public RouteType getRouteType() {
        return this.routeType;
    }

    /**
     * Set the routeType property: The routing methodology to where the ICE server will be located from the client.
     *
     * @param routeType the routeType value to set.
     */
    public void setRouteType(RouteType routeType) {
        this.routeType = routeType;
    }
}
