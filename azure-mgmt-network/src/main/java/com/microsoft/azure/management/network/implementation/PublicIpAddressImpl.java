/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.IPVersion;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIpConfiguration;
import com.microsoft.azure.management.network.PublicFrontend;
import com.microsoft.azure.management.network.PublicIPAddressDnsSettings;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;

/**
 *  Implementation for {@link PublicIpAddress} and its create and update interfaces.
 */
@LangDefinition
class PublicIpAddressImpl
    extends GroupableResourceImpl<
        PublicIpAddress,
        PublicIPAddressInner,
        PublicIpAddressImpl,
        NetworkManager>
    implements
        PublicIpAddress,
        PublicIpAddress.Definition,
        PublicIpAddress.Update {

    private final PublicIPAddressesInner client;

    PublicIpAddressImpl(String name,
            PublicIPAddressInner innerModel,
            final PublicIPAddressesInner client,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.client = client;
    }

    // Verbs

    @Override
    public PublicIpAddress refresh() {
        PublicIPAddressInner response = this.client.get(this.resourceGroupName(), this.name());
        this.setInner(response);
        return this;
    }

    // Setters (fluent)

    @Override
    public PublicIpAddressImpl withIdleTimeoutInMinutes(int minutes) {
        this.inner().withIdleTimeoutInMinutes(minutes);
        return this;
    }

    @Override
    public PublicIpAddressImpl withStaticIp() {
        this.inner().withPublicIPAllocationMethod(IPAllocationMethod.STATIC);
        return this;
    }

    @Override
    public PublicIpAddressImpl withDynamicIp() {
        this.inner().withPublicIPAllocationMethod(IPAllocationMethod.DYNAMIC);
        return this;
    }

    @Override
    public PublicIpAddressImpl withLeafDomainLabel(String dnsName) {
        this.inner().dnsSettings().withDomainNameLabel(dnsName.toLowerCase());
        return this;
    }

    @Override
    public PublicIpAddressImpl withoutLeafDomainLabel() {
        return this.withLeafDomainLabel(null);
    }

    @Override
    public PublicIpAddressImpl withReverseFqdn(String reverseFqdn) {
        this.inner().dnsSettings().withReverseFqdn(reverseFqdn.toLowerCase());
        return this;
    }

    @Override
    public PublicIpAddressImpl withoutReverseFqdn() {
        return this.withReverseFqdn(null);
    }


    // Getters

    @Override
    public int idleTimeoutInMinutes() {
        return this.inner().idleTimeoutInMinutes();
    }

    @Override
    public IPAllocationMethod ipAllocationMethod() {
        return this.inner().publicIPAllocationMethod();
    }

    @Override
    public IPVersion version() {
        return this.inner().publicIPAddressVersion();
    }

    @Override
    public String fqdn() {
        if (this.inner().dnsSettings() != null) {
            return this.inner().dnsSettings().fqdn();
        } else {
            return null;
        }
    }

    @Override
    public String reverseFqdn() {
        if (this.inner().dnsSettings() != null) {
            return this.inner().dnsSettings().reverseFqdn();
        } else {
            return null;
        }
    }

    @Override
    public String ipAddress() {
        return this.inner().ipAddress();
    }

    @Override
    public String leafDomainLabel() {
        if (this.inner().dnsSettings() == null) {
            return null;
        } else {
            return this.inner().dnsSettings().domainNameLabel();
        }
    }

    // CreateUpdateTaskGroup.ResourceCreator implementation
    @Override
    public Observable<PublicIpAddress> createResourceAsync() {
        // Clean up empty DNS settings
        final PublicIPAddressDnsSettings dnsSettings = this.inner().dnsSettings();
        if (dnsSettings != null) {
            if ((dnsSettings.domainNameLabel() == null || dnsSettings.domainNameLabel().isEmpty())
                    && (dnsSettings.fqdn() == null || dnsSettings.fqdn().isEmpty())
                    && (dnsSettings.reverseFqdn() == null || dnsSettings.reverseFqdn().isEmpty())) {
                this.inner().withDnsSettings(null);
            }
        }

        return this.client.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
                .map(innerToFluentMap(this));
    }

    private boolean equalsResourceType(String resourceType) {
        IPConfigurationInner ipConfig = this.inner().ipConfiguration();
        if (ipConfig == null || resourceType == null) {
            return false;
        } else {
            final String refId = this.inner().ipConfiguration().id();
            final String resourceType2 = ResourceUtils.resourceTypeFromResourceId(refId);
            return resourceType.equalsIgnoreCase(resourceType2);
        }
    }

    @Override
    public boolean hasAssignedLoadBalancer() {
        return equalsResourceType("frontendIPConfigurations");
    }

    @Override
    public PublicFrontend getAssignedLoadBalancerFrontend() {
        if (this.hasAssignedLoadBalancer()) {
            final String refId = this.inner().ipConfiguration().id();
            final String loadBalancerId = ResourceUtils.parentResourcePathFromResourceId(refId);
            final LoadBalancer lb = this.myManager.loadBalancers().getById(loadBalancerId);
            final String frontendName = ResourceUtils.nameFromResourceId(refId);
            return (PublicFrontend) lb.frontends().get(frontendName);
        } else {
            return null;
        }
    }

    @Override
    public boolean hasAssignedNetworkInterface() {
        return equalsResourceType("ipConfigurations");
    }

    @Override
    public NicIpConfiguration getAssignedNetworkInterfaceIpConfiguration() {
        if (this.hasAssignedNetworkInterface()) {
            final String refId = this.inner().ipConfiguration().id();
            final String parentId = ResourceUtils.parentResourcePathFromResourceId(refId);
            final NetworkInterface nic = this.myManager.networkInterfaces().getById(parentId);
            final String childName = ResourceUtils.nameFromResourceId(refId);
            return nic.ipConfigurations().get(childName);
        } else {
            return null;
        }
    }
}
