/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.synapse.v2020_12_01;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * Private Endpoint Connection For Private Link Hub - Basic.
 */
@JsonFlatten
public class PrivateEndpointConnectionForPrivateLinkHubBasic {
    /**
     * identifier.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /**
     * The private endpoint which the connection belongs to.
     */
    @JsonProperty(value = "properties.privateEndpoint")
    private PrivateEndpoint privateEndpoint;

    /**
     * Connection state of the private endpoint connection.
     */
    @JsonProperty(value = "properties.privateLinkServiceConnectionState")
    private PrivateLinkServiceConnectionState privateLinkServiceConnectionState;

    /**
     * Provisioning state of the private endpoint connection.
     */
    @JsonProperty(value = "properties.provisioningState", access = JsonProperty.Access.WRITE_ONLY)
    private String provisioningState;

    /**
     * Get identifier.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Get the private endpoint which the connection belongs to.
     *
     * @return the privateEndpoint value
     */
    public PrivateEndpoint privateEndpoint() {
        return this.privateEndpoint;
    }

    /**
     * Set the private endpoint which the connection belongs to.
     *
     * @param privateEndpoint the privateEndpoint value to set
     * @return the PrivateEndpointConnectionForPrivateLinkHubBasic object itself.
     */
    public PrivateEndpointConnectionForPrivateLinkHubBasic withPrivateEndpoint(PrivateEndpoint privateEndpoint) {
        this.privateEndpoint = privateEndpoint;
        return this;
    }

    /**
     * Get connection state of the private endpoint connection.
     *
     * @return the privateLinkServiceConnectionState value
     */
    public PrivateLinkServiceConnectionState privateLinkServiceConnectionState() {
        return this.privateLinkServiceConnectionState;
    }

    /**
     * Set connection state of the private endpoint connection.
     *
     * @param privateLinkServiceConnectionState the privateLinkServiceConnectionState value to set
     * @return the PrivateEndpointConnectionForPrivateLinkHubBasic object itself.
     */
    public PrivateEndpointConnectionForPrivateLinkHubBasic withPrivateLinkServiceConnectionState(PrivateLinkServiceConnectionState privateLinkServiceConnectionState) {
        this.privateLinkServiceConnectionState = privateLinkServiceConnectionState;
        return this;
    }

    /**
     * Get provisioning state of the private endpoint connection.
     *
     * @return the provisioningState value
     */
    public String provisioningState() {
        return this.provisioningState;
    }

}
