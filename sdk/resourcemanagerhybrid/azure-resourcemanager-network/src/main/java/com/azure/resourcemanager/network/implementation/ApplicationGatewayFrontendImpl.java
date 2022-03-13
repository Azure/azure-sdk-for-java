// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayFrontend;
import com.azure.resourcemanager.network.models.ApplicationGatewayFrontendIpConfiguration;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import reactor.core.publisher.Mono;

/** Implementation for ApplicationGatewayFrontend. */
class ApplicationGatewayFrontendImpl
    extends ChildResourceImpl<ApplicationGatewayFrontendIpConfiguration, ApplicationGatewayImpl, ApplicationGateway>
    implements ApplicationGatewayFrontend,
        ApplicationGatewayFrontend.Definition<ApplicationGateway.DefinitionStages.WithListener>,
        ApplicationGatewayFrontend.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayFrontend.Update {

    ApplicationGatewayFrontendImpl(ApplicationGatewayFrontendIpConfiguration inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters

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
    public String privateIpAddress() {
        return this.innerModel().privateIpAddress();
    }

    @Override
    public IpAllocationMethod privateIpAllocationMethod() {
        return this.innerModel().privateIpAllocationMethod();
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String publicIpAddressId() {
        if (this.innerModel().publicIpAddress() != null) {
            return this.innerModel().publicIpAddress().id();
        } else {
            return null;
        }
    }

    @Override
    public boolean isPublic() {
        return (this.innerModel().publicIpAddress() != null);
    }

    @Override
    public boolean isPrivate() {
        return (this.innerModel().subnet() != null);
    }

    // Fluent setters

    @Override
    public ApplicationGatewayFrontendImpl withExistingSubnet(Network network, String subnetName) {
        return this.withExistingSubnet(network.id(), subnetName);
    }

    @Override
    public ApplicationGatewayFrontendImpl withExistingSubnet(String parentNetworkResourceId, String subnetName) {
        SubResource subnetRef = new SubResource().withId(parentNetworkResourceId + "/subnets/" + subnetName);
        this.innerModel().withSubnet(subnetRef);

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
        this.innerModel().withPublicIpAddress(pipRef);
        this.withoutSubnet(); // Ensure no conflicting public and private settings
        return this;
    }

    @Override
    public ApplicationGatewayFrontendImpl withoutPublicIpAddress() {
        this.innerModel().withPublicIpAddress(null);
        return this;
    }

    public ApplicationGatewayFrontendImpl withoutSubnet() {
        this.innerModel().withSubnet(null).withPrivateIpAddress(null).withPrivateIpAllocationMethod(null);
        return this;
    }

    @Override
    public ApplicationGatewayFrontendImpl withPrivateIpAddressDynamic() {
        this.innerModel().withPrivateIpAddress(null).withPrivateIpAllocationMethod(IpAllocationMethod.DYNAMIC);
        return this;
    }

    @Override
    public ApplicationGatewayFrontendImpl withPrivateIpAddressStatic(String ipAddress) {
        this.innerModel().withPrivateIpAddress(ipAddress).withPrivateIpAllocationMethod(IpAllocationMethod.STATIC);
        return this;
    }

    // Verbs

    @Override
    public Subnet getSubnet() {
        return Utils.getAssociatedSubnet(this.parent().manager(), this.innerModel().subnet());
    }

    @Override
    public ApplicationGatewayImpl attach() {
        return this.parent().withFrontend(this);
    }

    @Override
    public PublicIpAddress getPublicIpAddress() {
        return this.getPublicIpAddressAsync().block();
    }

    @Override
    public Mono<PublicIpAddress> getPublicIpAddressAsync() {
        String pipId = this.publicIpAddressId();
        return pipId == null ? Mono.empty() : this.parent().manager().publicIpAddresses().getByIdAsync(pipId);
    }
}
