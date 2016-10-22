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
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for ApplicationGatewayIpConfiguration.
 */
@LangDefinition
class ApplicationGatewayIpConfigurationImpl
    extends ChildResourceImpl<ApplicationGatewayIPConfigurationInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayIpConfiguration,
        ApplicationGatewayIpConfiguration.Definition<ApplicationGateway.DefinitionStages.WithFrontend>,
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
    public ApplicationGatewayIpConfigurationImpl withContainingSubnet(Subnet subnet) {
        return this.withContainingSubnet(subnet.parent().id(), subnet.name());
    }

    @Override
    public ApplicationGatewayIpConfigurationImpl withContainingSubnet(Network network, String subnetName) {
        return this.withContainingSubnet(network.id(), subnetName);
    }

    @Override
    public ApplicationGatewayIpConfigurationImpl withContainingSubnet(String networkId, String subnetName) {
        SubResource subnetRef = new SubResource().withId(networkId + "/subnets/" + subnetName);
        this.inner().withSubnet(subnetRef);
        return this;
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        return this.parent().withConfig(this);
    }
}
