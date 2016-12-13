/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayIpConfiguration;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for ApplicationGatewayIpConfiguration.
 */
@LangDefinition
class ApplicationGatewayIpConfigurationImpl
    extends ChildResourceImpl<ApplicationGatewayIPConfigurationInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayIpConfiguration,
        ApplicationGatewayIpConfiguration.Definition<ApplicationGateway.DefinitionStages.WithCreate>,
        ApplicationGatewayIpConfiguration.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayIpConfiguration.Update {

    ApplicationGatewayIpConfigurationImpl(ApplicationGatewayIPConfigurationInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters
    @Override
    public String name() {
        return this.inner().name();
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
    public Subnet getSubnet() {
        return this.parent().manager().getAssociatedSubnet(this.inner().subnet());
    }
}
