/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.management.network.IPAllocationMethod;
import com.azure.management.network.Network;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.network.Subnet;
import com.azure.management.network.VirtualNetworkGateway;
import com.azure.management.network.VirtualNetworkGatewayIPConfiguration;
import com.azure.management.network.models.VirtualNetworkGatewayIPConfigurationInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 * Implementation for VirtualNetworkGatewayIPConfiguration.
 */
class VirtualNetworkGatewayIPConfigurationImpl
        extends ChildResourceImpl<VirtualNetworkGatewayIPConfigurationInner, VirtualNetworkGatewayImpl, VirtualNetworkGateway>
        implements
        VirtualNetworkGatewayIPConfiguration,
        VirtualNetworkGatewayIPConfiguration.Definition<VirtualNetworkGateway.DefinitionStages.WithCreate>,
        VirtualNetworkGatewayIPConfiguration.UpdateDefinition<VirtualNetworkGateway.Update>,
        VirtualNetworkGatewayIPConfiguration.Update {

    VirtualNetworkGatewayIPConfigurationImpl(VirtualNetworkGatewayIPConfigurationInner inner, VirtualNetworkGatewayImpl parent) {
        super(inner, parent);
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String publicIPAddressId() {
        if (this.inner().publicIPAddress() != null) {
            return this.inner().publicIPAddress().getId();
        } else {
            return null;
        }
    }

    @Override
    public String networkId() {
        SubResource subnetRef = this.inner().subnet();
        if (subnetRef != null) {
            return ResourceUtils.parentResourceIdFromResourceId(subnetRef.getId());
        } else {
            return null;
        }
    }

    @Override
    public String subnetName() {
        SubResource subnetRef = this.inner().subnet();
        if (subnetRef != null) {
            return ResourceUtils.nameFromResourceId(subnetRef.getId());
        } else {
            return null;
        }
    }

    @Override
    public IPAllocationMethod privateIPAllocationMethod() {
        return inner().privateIPAllocationMethod();
    }

    @Override
    public Subnet getSubnet() {
        return this.parent().manager().getAssociatedSubnet(this.inner().subnet());
    }

    @Override
    public VirtualNetworkGatewayIPConfigurationImpl withExistingSubnet(String networkId, String subnetName) {
        SubResource subnetRef = new SubResource().setId(networkId + "/subnets/" + subnetName);
        this.inner().withSubnet(subnetRef);
        return this;
    }

    @Override
    public VirtualNetworkGatewayIPConfigurationImpl withExistingSubnet(Subnet subnet) {
        return this.withExistingSubnet(subnet.parent().id(), subnet.name());
    }

    @Override
    public VirtualNetworkGatewayIPConfigurationImpl withExistingSubnet(Network network, String subnetName) {
        return this.withExistingSubnet(network.id(), subnetName);
    }

    @Override
    public VirtualNetworkGatewayImpl attach() {
        return this.parent().withConfig(this);
    }

    @Override
    public VirtualNetworkGatewayIPConfigurationImpl withExistingPublicIPAddress(PublicIPAddress pip) {
        return this.withExistingPublicIPAddress(pip.id());
    }

    @Override
    public VirtualNetworkGatewayIPConfigurationImpl withExistingPublicIPAddress(String resourceId) {
        SubResource pipRef = new SubResource().setId(resourceId);
        this.inner().withPublicIPAddress(pipRef);
        return this;
    }
}
