/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayFrontend;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for ApplicationGatewayFrontend.
 */
@LangDefinition
class ApplicationGatewayFrontendImpl
    extends ChildResourceImpl<ApplicationGatewayFrontendIPConfigurationInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayFrontend,
        ApplicationGatewayFrontend.Definition<ApplicationGateway.DefinitionStages.WithListener>,
        ApplicationGatewayFrontend.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayFrontend.Update {

    ApplicationGatewayFrontendImpl(ApplicationGatewayFrontendIPConfigurationInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters

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
    public String privateIpAddress() {
        return this.inner().privateIPAddress();
    }

    @Override
    public IPAllocationMethod privateIpAllocationMethod() {
        return this.inner().privateIPAllocationMethod();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String publicIpAddressId() {
        if (this.inner().publicIPAddress() != null) {
            return this.inner().publicIPAddress().id();
        } else {
            return null;
        }
    }

    @Override
    public boolean isPublic() {
        return (this.inner().publicIPAddress() != null);
    }

    @Override
    public boolean isPrivate() {
        return (this.inner().subnet() != null);
    }

    // Fluent setters

    @Override
    public ApplicationGatewayFrontendImpl withExistingSubnet(Network network, String subnetName) {
        return this.withExistingSubnet(network.id(), subnetName);
    }

    @Override
    public ApplicationGatewayFrontendImpl withExistingSubnet(String parentNetworkResourceId, String subnetName) {
        SubResource subnetRef = new SubResource()
                .withId(parentNetworkResourceId + "/subnets/" + subnetName);
        this.inner().withSubnet(subnetRef);

        // Ensure this frontend is not public
        this.withoutPublicIpAddress();
        return this;
    }

    @Override
    public ApplicationGatewayFrontendImpl withExistingPublicIpAddress(PublicIpAddress pip) {
        return this.withExistingPublicIpAddress(pip.id());
    }

    @Override
    public ApplicationGatewayFrontendImpl withExistingPublicIpAddress(String resourceId) {
        SubResource pipRef = new SubResource().withId(resourceId);
        this.inner().withPublicIPAddress(pipRef);
        this.withoutSubnet(); // Ensure no conflicting public and private settings
        return this;
    }

    @Override
    public ApplicationGatewayFrontendImpl withoutPublicIpAddress() {
        this.inner().withPublicIPAddress(null);
        return this;
    }

    public ApplicationGatewayFrontendImpl withoutSubnet() {
        this.inner()
            .withSubnet(null)
            .withPrivateIPAddress(null)
            .withPrivateIPAllocationMethod(null);
        return this;
    }

    @Override
    public ApplicationGatewayFrontendImpl withPrivateIpAddressDynamic() {
        this.inner()
            .withPrivateIPAddress(null)
            .withPrivateIPAllocationMethod(IPAllocationMethod.DYNAMIC);
        return this;
    }

    @Override
    public ApplicationGatewayFrontendImpl withPrivateIpAddressStatic(String ipAddress) {
        this.inner()
            .withPrivateIPAddress(ipAddress)
            .withPrivateIPAllocationMethod(IPAllocationMethod.STATIC);
        return this;
    }

    // Verbs

    @Override
    public Subnet getSubnet() {
        return this.parent().manager().getAssociatedSubnet(this.inner().subnet());
    }

    @Override
    public ApplicationGatewayImpl attach() {
        return this.parent().withFrontend(this);
    }

    @Override
    public PublicIpAddress getPublicIpAddress() {
        final String pipId = this.publicIpAddressId();
        if (pipId == null) {
            return null;
        } else {
            return this.parent().manager().publicIpAddresses().getById(pipId);
        }
    }
}
