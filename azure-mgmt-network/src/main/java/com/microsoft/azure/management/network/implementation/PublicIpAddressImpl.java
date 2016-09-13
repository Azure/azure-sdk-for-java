/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.PublicIPAddressDnsSettings;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;

/**
 *  Implementation for {@link PublicIpAddress} and its create and update interfaces.
 */
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
    public Observable<PublicIpAddress> applyAsync() {
        return this.createAsync();
    }

    @Override
    public PublicIpAddress refresh() throws Exception {
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
    public String fqdn() {
        return this.inner().dnsSettings().fqdn();
    }

    @Override
    public String reverseFqdn() {
        return this.inner().dnsSettings().reverseFqdn();
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

    // CreatorTaskGroup.ResourceCreator implementation
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
}