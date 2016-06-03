/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * This is an object used to store a full view of network features (presently
 * VNET integration and Hybrid Connections)
 * for a web app.
 */
@JsonFlatten
public class NetworkFeaturesInner extends Resource {
    /**
     * The Vnet Name.
     */
    @JsonProperty(value = "properties.virtualNetworkName")
    private String virtualNetworkName;

    /**
     * The Vnet Summary view.
     */
    @JsonProperty(value = "properties.virtualNetworkConnection")
    private VnetInfoInner virtualNetworkConnection;

    /**
     * The Hybrid Connections Summary view.
     */
    @JsonProperty(value = "properties.hybridConnections")
    private List<RelayServiceConnectionEntityInner> hybridConnections;

    /**
     * Get the virtualNetworkName value.
     *
     * @return the virtualNetworkName value
     */
    public String virtualNetworkName() {
        return this.virtualNetworkName;
    }

    /**
     * Set the virtualNetworkName value.
     *
     * @param virtualNetworkName the virtualNetworkName value to set
     * @return the NetworkFeaturesInner object itself.
     */
    public NetworkFeaturesInner withVirtualNetworkName(String virtualNetworkName) {
        this.virtualNetworkName = virtualNetworkName;
        return this;
    }

    /**
     * Get the virtualNetworkConnection value.
     *
     * @return the virtualNetworkConnection value
     */
    public VnetInfoInner virtualNetworkConnection() {
        return this.virtualNetworkConnection;
    }

    /**
     * Set the virtualNetworkConnection value.
     *
     * @param virtualNetworkConnection the virtualNetworkConnection value to set
     * @return the NetworkFeaturesInner object itself.
     */
    public NetworkFeaturesInner withVirtualNetworkConnection(VnetInfoInner virtualNetworkConnection) {
        this.virtualNetworkConnection = virtualNetworkConnection;
        return this;
    }

    /**
     * Get the hybridConnections value.
     *
     * @return the hybridConnections value
     */
    public List<RelayServiceConnectionEntityInner> hybridConnections() {
        return this.hybridConnections;
    }

    /**
     * Set the hybridConnections value.
     *
     * @param hybridConnections the hybridConnections value to set
     * @return the NetworkFeaturesInner object itself.
     */
    public NetworkFeaturesInner withHybridConnections(List<RelayServiceConnectionEntityInner> hybridConnections) {
        this.hybridConnections = hybridConnections;
        return this;
    }

}
