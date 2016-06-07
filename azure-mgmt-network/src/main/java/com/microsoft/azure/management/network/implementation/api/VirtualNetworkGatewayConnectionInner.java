/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.microsoft.azure.SubResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * A common class for general resource information.
 */
@JsonFlatten
public class VirtualNetworkGatewayConnectionInner extends Resource {
    /**
     * The authorizationKey.
     */
    @JsonProperty(value = "properties.authorizationKey")
    private String authorizationKey;

    /**
     * The virtualNetworkGateway1 property.
     */
    @JsonProperty(value = "properties.virtualNetworkGateway1")
    private VirtualNetworkGatewayInner virtualNetworkGateway1;

    /**
     * The virtualNetworkGateway2 property.
     */
    @JsonProperty(value = "properties.virtualNetworkGateway2")
    private VirtualNetworkGatewayInner virtualNetworkGateway2;

    /**
     * The localNetworkGateway2 property.
     */
    @JsonProperty(value = "properties.localNetworkGateway2")
    private LocalNetworkGatewayInner localNetworkGateway2;

    /**
     * Gateway connection type -Ipsec/Dedicated/VpnClient/Vnet2Vnet. Possible
     * values include: 'IPsec', 'Vnet2Vnet', 'ExpressRoute', 'VPNClient'.
     */
    @JsonProperty(value = "properties.connectionType")
    private String connectionType;

    /**
     * The Routing weight.
     */
    @JsonProperty(value = "properties.routingWeight")
    private Integer routingWeight;

    /**
     * The Ipsec share key.
     */
    @JsonProperty(value = "properties.sharedKey")
    private String sharedKey;

    /**
     * Virtual network Gateway connection status. Possible values include:
     * 'Unknown', 'Connecting', 'Connected', 'NotConnected'.
     */
    @JsonProperty(value = "properties.connectionStatus")
    private String connectionStatus;

    /**
     * The Egress Bytes Transferred in this connection.
     */
    @JsonProperty(value = "properties.egressBytesTransferred")
    private Long egressBytesTransferred;

    /**
     * The Ingress Bytes Transferred in this connection.
     */
    @JsonProperty(value = "properties.ingressBytesTransferred")
    private Long ingressBytesTransferred;

    /**
     * The reference to peerings resource.
     */
    @JsonProperty(value = "properties.peer")
    private SubResource peer;

    /**
     * EnableBgp Flag.
     */
    @JsonProperty(value = "properties.enableBgp")
    private Boolean enableBgp;

    /**
     * Gets or sets resource guid property of the
     * VirtualNetworkGatewayConnection resource.
     */
    @JsonProperty(value = "properties.resourceGuid")
    private String resourceGuid;

    /**
     * Gets or sets Provisioning state of the VirtualNetworkGatewayConnection
     * resource Updating/Deleting/Failed.
     */
    @JsonProperty(value = "properties.provisioningState")
    private String provisioningState;

    /**
     * Gets a unique read-only string that changes whenever the resource is
     * updated.
     */
    private String etag;

    /**
     * Get the authorizationKey value.
     *
     * @return the authorizationKey value
     */
    public String authorizationKey() {
        return this.authorizationKey;
    }

    /**
     * Set the authorizationKey value.
     *
     * @param authorizationKey the authorizationKey value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withAuthorizationKey(String authorizationKey) {
        this.authorizationKey = authorizationKey;
        return this;
    }

    /**
     * Get the virtualNetworkGateway1 value.
     *
     * @return the virtualNetworkGateway1 value
     */
    public VirtualNetworkGatewayInner virtualNetworkGateway1() {
        return this.virtualNetworkGateway1;
    }

    /**
     * Set the virtualNetworkGateway1 value.
     *
     * @param virtualNetworkGateway1 the virtualNetworkGateway1 value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withVirtualNetworkGateway1(VirtualNetworkGatewayInner virtualNetworkGateway1) {
        this.virtualNetworkGateway1 = virtualNetworkGateway1;
        return this;
    }

    /**
     * Get the virtualNetworkGateway2 value.
     *
     * @return the virtualNetworkGateway2 value
     */
    public VirtualNetworkGatewayInner virtualNetworkGateway2() {
        return this.virtualNetworkGateway2;
    }

    /**
     * Set the virtualNetworkGateway2 value.
     *
     * @param virtualNetworkGateway2 the virtualNetworkGateway2 value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withVirtualNetworkGateway2(VirtualNetworkGatewayInner virtualNetworkGateway2) {
        this.virtualNetworkGateway2 = virtualNetworkGateway2;
        return this;
    }

    /**
     * Get the localNetworkGateway2 value.
     *
     * @return the localNetworkGateway2 value
     */
    public LocalNetworkGatewayInner localNetworkGateway2() {
        return this.localNetworkGateway2;
    }

    /**
     * Set the localNetworkGateway2 value.
     *
     * @param localNetworkGateway2 the localNetworkGateway2 value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withLocalNetworkGateway2(LocalNetworkGatewayInner localNetworkGateway2) {
        this.localNetworkGateway2 = localNetworkGateway2;
        return this;
    }

    /**
     * Get the connectionType value.
     *
     * @return the connectionType value
     */
    public String connectionType() {
        return this.connectionType;
    }

    /**
     * Set the connectionType value.
     *
     * @param connectionType the connectionType value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withConnectionType(String connectionType) {
        this.connectionType = connectionType;
        return this;
    }

    /**
     * Get the routingWeight value.
     *
     * @return the routingWeight value
     */
    public Integer routingWeight() {
        return this.routingWeight;
    }

    /**
     * Set the routingWeight value.
     *
     * @param routingWeight the routingWeight value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withRoutingWeight(Integer routingWeight) {
        this.routingWeight = routingWeight;
        return this;
    }

    /**
     * Get the sharedKey value.
     *
     * @return the sharedKey value
     */
    public String sharedKey() {
        return this.sharedKey;
    }

    /**
     * Set the sharedKey value.
     *
     * @param sharedKey the sharedKey value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
        return this;
    }

    /**
     * Get the connectionStatus value.
     *
     * @return the connectionStatus value
     */
    public String connectionStatus() {
        return this.connectionStatus;
    }

    /**
     * Set the connectionStatus value.
     *
     * @param connectionStatus the connectionStatus value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
        return this;
    }

    /**
     * Get the egressBytesTransferred value.
     *
     * @return the egressBytesTransferred value
     */
    public Long egressBytesTransferred() {
        return this.egressBytesTransferred;
    }

    /**
     * Set the egressBytesTransferred value.
     *
     * @param egressBytesTransferred the egressBytesTransferred value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withEgressBytesTransferred(Long egressBytesTransferred) {
        this.egressBytesTransferred = egressBytesTransferred;
        return this;
    }

    /**
     * Get the ingressBytesTransferred value.
     *
     * @return the ingressBytesTransferred value
     */
    public Long ingressBytesTransferred() {
        return this.ingressBytesTransferred;
    }

    /**
     * Set the ingressBytesTransferred value.
     *
     * @param ingressBytesTransferred the ingressBytesTransferred value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withIngressBytesTransferred(Long ingressBytesTransferred) {
        this.ingressBytesTransferred = ingressBytesTransferred;
        return this;
    }

    /**
     * Get the peer value.
     *
     * @return the peer value
     */
    public SubResource peer() {
        return this.peer;
    }

    /**
     * Set the peer value.
     *
     * @param peer the peer value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withPeer(SubResource peer) {
        this.peer = peer;
        return this;
    }

    /**
     * Get the enableBgp value.
     *
     * @return the enableBgp value
     */
    public Boolean enableBgp() {
        return this.enableBgp;
    }

    /**
     * Set the enableBgp value.
     *
     * @param enableBgp the enableBgp value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withEnableBgp(Boolean enableBgp) {
        this.enableBgp = enableBgp;
        return this;
    }

    /**
     * Get the resourceGuid value.
     *
     * @return the resourceGuid value
     */
    public String resourceGuid() {
        return this.resourceGuid;
    }

    /**
     * Set the resourceGuid value.
     *
     * @param resourceGuid the resourceGuid value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withResourceGuid(String resourceGuid) {
        this.resourceGuid = resourceGuid;
        return this;
    }

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public String provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

    /**
     * Get the etag value.
     *
     * @return the etag value
     */
    public String etag() {
        return this.etag;
    }

    /**
     * Set the etag value.
     *
     * @param etag the etag value to set
     * @return the VirtualNetworkGatewayConnectionInner object itself.
     */
    public VirtualNetworkGatewayConnectionInner withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
