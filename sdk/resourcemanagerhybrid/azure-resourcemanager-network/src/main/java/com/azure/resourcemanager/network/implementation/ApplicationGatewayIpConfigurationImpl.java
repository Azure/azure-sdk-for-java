// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayIpConfiguration;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayIpConfigurationInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/** Implementation for ApplicationGatewayIpConfiguration. */
class ApplicationGatewayIpConfigurationImpl
    extends ChildResourceImpl<ApplicationGatewayIpConfigurationInner, ApplicationGatewayImpl, ApplicationGateway>
    implements ApplicationGatewayIpConfiguration,
        ApplicationGatewayIpConfiguration.Definition<ApplicationGateway.DefinitionStages.WithCreate>,
        ApplicationGatewayIpConfiguration.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayIpConfiguration.Update {

    ApplicationGatewayIpConfigurationImpl(ApplicationGatewayIpConfigurationInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters
    @Override
    public String name() {
        return this.innerModel().name();
    }

    // Fluent setters

    @Override
    public ApplicationGatewayIpConfigurationImpl withExistingSubnet(Subnet subnet) {
        return this.withExistingSubnet(subnet.parent().id(), subnet.name());
    }

    @Override
    public ApplicationGatewayIpConfigurationImpl withExistingSubnet(Network network, String subnetName) {
        return this.withExistingSubnet(network.id(), subnetName);
    }

    @Override
    public ApplicationGatewayIpConfigurationImpl withExistingSubnet(String networkId, String subnetName) {
        SubResource subnetRef = new SubResource().withId(networkId + "/subnets/" + subnetName);
        this.innerModel().withSubnet(subnetRef);
        return this;
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        return this.parent().withConfig(this);
    }

    @Override
    public String networkId() {
        SubResource subnetRef = this.innerModel().subnet();
        if (subnetRef != null) {
            return ResourceUtils.parentResourceIdFromResourceId(subnetRef.id());
        } else {
            return null;
        }
    }

    @Override
    public String subnetName() {
        SubResource subnetRef = this.innerModel().subnet();
        if (subnetRef != null) {
            return ResourceUtils.nameFromResourceId(subnetRef.id());
        } else {
            return null;
        }
    }

    @Override
    public Subnet getSubnet() {
        return Utils.getAssociatedSubnet(this.parent().manager(), this.innerModel().subnet());
    }
}
