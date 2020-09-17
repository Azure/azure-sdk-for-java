// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.network.models.VirtualNetworkGateway;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayIpConfiguration;
import com.azure.resourcemanager.network.fluent.inner.VirtualNetworkGatewayIpConfigurationInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/** Implementation for VirtualNetworkGatewayIpConfiguration. */
class VirtualNetworkGatewayIpConfigurationImpl
    extends ChildResourceImpl<
        VirtualNetworkGatewayIpConfigurationInner, VirtualNetworkGatewayImpl, VirtualNetworkGateway>
    implements VirtualNetworkGatewayIpConfiguration,
        VirtualNetworkGatewayIpConfiguration.Definition<VirtualNetworkGateway.DefinitionStages.WithCreate>,
        VirtualNetworkGatewayIpConfiguration.UpdateDefinition<VirtualNetworkGateway.Update>,
        VirtualNetworkGatewayIpConfiguration.Update {

    VirtualNetworkGatewayIpConfigurationImpl(
        VirtualNetworkGatewayIpConfigurationInner inner, VirtualNetworkGatewayImpl parent) {
        super(inner, parent);
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String publicIpAddressId() {
        if (this.inner().publicIpAddress() != null) {
            return this.inner().publicIpAddress().id();
        } else {
            return null;
        }
    }

    @Override
    public String networkId() {
        SubResource subnetRef = this.inner().subnet();
        if (subnetRef != null) {
            return ResourceUtils.parentResourceIdFromResourceId(subnetRef.id());
        } else {
            return null;
        }
    }

    @Override
    public String subnetName() {
        SubResource subnetRef = this.inner().subnet();
        if (subnetRef != null) {
            return ResourceUtils.nameFromResourceId(subnetRef.id());
        } else {
            return null;
        }
    }

    @Override
    public IpAllocationMethod privateIpAllocationMethod() {
        return inner().privateIpAllocationMethod();
    }

    @Override
    public Subnet getSubnet() {
        return Utils.getAssociatedSubnet(this.parent().manager(), this.inner().subnet());
    }

    @Override
    public VirtualNetworkGatewayIpConfigurationImpl withExistingSubnet(String networkId, String subnetName) {
        SubResource subnetRef = new SubResource().withId(networkId + "/subnets/" + subnetName);
        this.inner().withSubnet(subnetRef);
        return this;
    }

    @Override
    public VirtualNetworkGatewayIpConfigurationImpl withExistingSubnet(Subnet subnet) {
        return this.withExistingSubnet(subnet.parent().id(), subnet.name());
    }

    @Override
    public VirtualNetworkGatewayIpConfigurationImpl withExistingSubnet(Network network, String subnetName) {
        return this.withExistingSubnet(network.id(), subnetName);
    }

    @Override
    public VirtualNetworkGatewayImpl attach() {
        return this.parent().withConfig(this);
    }

    @Override
    public VirtualNetworkGatewayIpConfigurationImpl withExistingPublicIpAddress(PublicIpAddress pip) {
        return this.withExistingPublicIpAddress(pip.id());
    }

    @Override
    public VirtualNetworkGatewayIpConfigurationImpl withExistingPublicIpAddress(String resourceId) {
        SubResource pipRef = new SubResource().withId(resourceId);
        this.inner().withPublicIpAddress(pipRef);
        return this;
    }
}
