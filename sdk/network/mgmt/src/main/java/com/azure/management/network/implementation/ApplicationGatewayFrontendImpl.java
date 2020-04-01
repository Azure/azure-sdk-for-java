/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.management.network.ApplicationGateway;
import com.azure.management.network.ApplicationGatewayFrontend;
import com.azure.management.network.ApplicationGatewayFrontendIPConfiguration;
import com.azure.management.network.IPAllocationMethod;
import com.azure.management.network.Network;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.network.Subnet;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 * Implementation for ApplicationGatewayFrontend.
 */
class ApplicationGatewayFrontendImpl
        extends ChildResourceImpl<ApplicationGatewayFrontendIPConfiguration, ApplicationGatewayImpl, ApplicationGateway>
        implements
        ApplicationGatewayFrontend,
        ApplicationGatewayFrontend.Definition<ApplicationGateway.DefinitionStages.WithListener>,
        ApplicationGatewayFrontend.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayFrontend.Update {

    ApplicationGatewayFrontendImpl(ApplicationGatewayFrontendIPConfiguration inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters

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
    public String privateIPAddress() {
        return this.inner().privateIPAddress();
    }

    @Override
    public IPAllocationMethod privateIPAllocationMethod() {
        return this.inner().privateIPAllocationMethod();
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
                .setId(parentNetworkResourceId + "/subnets/" + subnetName);
        this.inner().withSubnet(subnetRef);

        // Ensure this frontend is not public
        this.withoutPublicIPAddress();
        return this;
    }

    @Override
    public ApplicationGatewayFrontendImpl withExistingPublicIPAddress(PublicIPAddress pip) {
        return this.withExistingPublicIPAddress(pip.id());
    }

    @Override
    public ApplicationGatewayFrontendImpl withExistingPublicIPAddress(String resourceId) {
        SubResource pipRef = new SubResource().setId(resourceId);
        this.inner().withPublicIPAddress(pipRef);
        this.withoutSubnet(); // Ensure no conflicting public and private settings
        return this;
    }

    @Override
    public ApplicationGatewayFrontendImpl withoutPublicIPAddress() {
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
    public ApplicationGatewayFrontendImpl withPrivateIPAddressDynamic() {
        this.inner()
                .withPrivateIPAddress(null)
                .withPrivateIPAllocationMethod(IPAllocationMethod.DYNAMIC);
        return this;
    }

    @Override
    public ApplicationGatewayFrontendImpl withPrivateIPAddressStatic(String ipAddress) {
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
    public PublicIPAddress getPublicIPAddress() {
        final String pipId = this.publicIPAddressId();
        if (pipId == null) {
            return null;
        } else {
            return this.parent().manager().publicIPAddresses().getById(pipId);
        }
    }
}
