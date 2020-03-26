/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.management.network.ApplicationGateway;
import com.azure.management.network.ApplicationGatewayIPConfiguration;
import com.azure.management.network.Network;
import com.azure.management.network.Subnet;
import com.azure.management.network.models.ApplicationGatewayIPConfigurationInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 * Implementation for ApplicationGatewayIPConfiguration.
 */
class ApplicationGatewayIPConfigurationImpl
        extends ChildResourceImpl<ApplicationGatewayIPConfigurationInner, ApplicationGatewayImpl, ApplicationGateway>
        implements
        ApplicationGatewayIPConfiguration,
        ApplicationGatewayIPConfiguration.Definition<ApplicationGateway.DefinitionStages.WithCreate>,
        ApplicationGatewayIPConfiguration.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayIPConfiguration.Update {

    ApplicationGatewayIPConfigurationImpl(ApplicationGatewayIPConfigurationInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters
    @Override
    public String name() {
        return this.inner().name();
    }

    // Fluent setters

    @Override
    public ApplicationGatewayIPConfigurationImpl withExistingSubnet(Subnet subnet) {
        return this.withExistingSubnet(subnet.parent().id(), subnet.name());
    }

    @Override
    public ApplicationGatewayIPConfigurationImpl withExistingSubnet(Network network, String subnetName) {
        return this.withExistingSubnet(network.id(), subnetName);
    }

    @Override
    public ApplicationGatewayIPConfigurationImpl withExistingSubnet(String networkId, String subnetName) {
        SubResource subnetRef = new SubResource().setId(networkId + "/subnets/" + subnetName);
        this.inner().withSubnet(subnetRef);
        return this;
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        return this.parent().withConfig(this);
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
    public Subnet getSubnet() {
        return this.parent().manager().getAssociatedSubnet(this.inner().subnet());
    }
}
