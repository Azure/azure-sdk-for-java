// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.networktraversal.models;

import com.azure.communication.common.CommunicationUserIdentifier;

/**
 * Additional options for getting a relay configuration.
 *
 */
public final class GetRelayConfigurationOptions {
    private CommunicationUserIdentifier communicationUser;
    private RouteType routeType;
    private Integer ttl;

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
     * @return the GetRelayConfigurationOptions object itself.
     */
    public GetRelayConfigurationOptions setCommunicationUserIdentifier(CommunicationUserIdentifier communicationUser) {
        this.communicationUser = communicationUser;
        return this;
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
     * @return the GetRelayConfigurationOptions object itself.
     */
    public GetRelayConfigurationOptions setRouteType(RouteType routeType) {
        this.routeType = routeType;
        return this;
    }

    /**
     * Get the ttl property: The specified time to live for the relay credential in seconds
     *
     * @return the time to live value.
    */
    public Integer getTtl() {
        return this.ttl;
    }

    /**
     * Set the ttl property: The specified time to live for the relay credential in seconds
     *
     * @param ttl the time to live value to set.
     * @return the GetRelayConfigurationOptions object itself.
     */
    public GetRelayConfigurationOptions setTtl(Integer ttl) {
        this.ttl = ttl;
        return this;
    }
}
