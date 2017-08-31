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
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.LoadBalancerPublicFrontend;
import com.microsoft.azure.management.network.PublicIPAddressDnsSettings;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;

import java.util.ArrayList;

/**
 *  Implementation for PublicIPAddress and its create and update interfaces.
 */
@LangDefinition
class PublicIPAddressImpl
    extends GroupableResourceImpl<
        PublicIPAddress,
        PublicIPAddressInner,
        PublicIPAddressImpl,
        NetworkManager>
    implements
        PublicIPAddress,
        PublicIPAddress.Definition,
        PublicIPAddress.Update {

    PublicIPAddressImpl(String name,
            PublicIPAddressInner innerModel,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    // Verbs

    @Override
    protected Observable<PublicIPAddressInner> getInnerAsync() {
        return this.manager().inner().publicIPAddresses().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    // Setters (fluent)

    @Override
    public PublicIPAddressImpl withIdleTimeoutInMinutes(int minutes) {
        this.inner().withIdleTimeoutInMinutes(minutes);
        return this;
    }

    @Override
    public PublicIPAddressImpl withStaticIP() {
        this.inner().withPublicIPAllocationMethod(IPAllocationMethod.STATIC);
        return this;
    }

    @Override
    public PublicIPAddressImpl withDynamicIP() {
        this.inner().withPublicIPAllocationMethod(IPAllocationMethod.DYNAMIC);
        return this;
    }

    @Override
    public PublicIPAddressImpl withLeafDomainLabel(String dnsName) {
        this.inner().dnsSettings().withDomainNameLabel(dnsName.toLowerCase());
        return this;
    }

    @Override
    public PublicIPAddressImpl withAvailabilityZone(String zoneId) {
        if (this.inner().zones() == null) {
            this.inner().withZones(new ArrayList<String>());
        }
        this.inner().zones().add(zoneId);
        return this;
    }

    @Override
    public PublicIPAddressImpl withoutLeafDomainLabel() {
        return this.withLeafDomainLabel(null);
    }

    @Override
    public PublicIPAddressImpl withReverseFqdn(String reverseFqdn) {
        this.inner().dnsSettings().withReverseFqdn(reverseFqdn.toLowerCase());
        return this;
    }

    @Override
    public PublicIPAddressImpl withoutReverseFqdn() {
        return this.withReverseFqdn(null);
    }

    // Getters

    @Override
    public int idleTimeoutInMinutes() {
        return Utils.toPrimitiveInt(this.inner().idleTimeoutInMinutes());
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
    public Observable<PublicIPAddress> createResourceAsync() {
        // Clean up empty DNS settings
        final PublicIPAddressDnsSettings dnsSettings = this.inner().dnsSettings();
        if (dnsSettings != null) {
            if ((dnsSettings.domainNameLabel() == null || dnsSettings.domainNameLabel().isEmpty())
                    && (dnsSettings.fqdn() == null || dnsSettings.fqdn().isEmpty())
                    && (dnsSettings.reverseFqdn() == null || dnsSettings.reverseFqdn().isEmpty())) {
                this.inner().withDnsSettings(null);
            }
        }

        return this.manager().inner().publicIPAddresses().createOrUpdateAsync(
                this.resourceGroupName(), this.name(), this.inner())
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
    public LoadBalancerPublicFrontend getAssignedLoadBalancerFrontend() {
        if (this.hasAssignedLoadBalancer()) {
            final String refId = this.inner().ipConfiguration().id();
            final String loadBalancerId = ResourceUtils.parentResourceIdFromResourceId(refId);
            final LoadBalancer lb = this.myManager.loadBalancers().getById(loadBalancerId);
            final String frontendName = ResourceUtils.nameFromResourceId(refId);
            return (LoadBalancerPublicFrontend) lb.frontends().get(frontendName);
        } else {
            return null;
        }
    }

    @Override
    public boolean hasAssignedNetworkInterface() {
        return equalsResourceType("ipConfigurations");
    }

    @Override
    public NicIPConfiguration getAssignedNetworkInterfaceIPConfiguration() {
        if (this.hasAssignedNetworkInterface()) {
            final String refId = this.inner().ipConfiguration().id();
            final String parentId = ResourceUtils.parentResourceIdFromResourceId(refId);
            final NetworkInterface nic = this.myManager.networkInterfaces().getById(parentId);
            final String childName = ResourceUtils.nameFromResourceId(refId);
            return nic.ipConfigurations().get(childName);
        } else {
            return null;
        }
    }
}
