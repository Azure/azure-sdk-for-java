/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.PublicIPAddressDnsSettings;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

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
    public PublicIpAddressImpl apply() throws Exception {
        return this.create();
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<PublicIpAddress> callback) {
        return this.createAsync(callback);
    }

    @Override
    public PublicIpAddress refresh() throws Exception {
        ServiceResponse<PublicIPAddressInner> response =
            this.client.get(this.resourceGroupName(), this.name());
        PublicIPAddressInner inner = response.getBody();
        this.setInner(inner);
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
    public IpAllocationMethod ipAllocationMethod() {
        return IpAllocationMethod.fromString(this.inner().publicIPAllocationMethod());
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

    @Override
    protected void createResource() throws Exception {
        // Clean up empty DNS settings
        final PublicIPAddressDnsSettings dnsSettings = this.inner().dnsSettings();
        if (dnsSettings != null) {
            if ((dnsSettings.domainNameLabel() == null || dnsSettings.domainNameLabel().isEmpty())
                    && (dnsSettings.fqdn() == null || dnsSettings.fqdn().isEmpty())
                    && (dnsSettings.reverseFqdn() == null || dnsSettings.reverseFqdn().isEmpty())) {
                this.inner().withDnsSettings(null);
            }
        }

        ServiceResponse<PublicIPAddressInner> response =
                this.client.createOrUpdate(this.resourceGroupName(), this.name(), this.inner());
        this.setInner(response.getBody());
    }

    @Override
    protected ServiceCall createResourceAsync(ServiceCallback<Void> callback) {
        // Clean up empty DNS settings
        final PublicIPAddressDnsSettings dnsSettings = this.inner().dnsSettings();
        if (dnsSettings != null) {
            if ((dnsSettings.domainNameLabel() == null || dnsSettings.domainNameLabel().isEmpty())
                    && (dnsSettings.fqdn() == null || dnsSettings.fqdn().isEmpty())
                    && (dnsSettings.reverseFqdn() == null || dnsSettings.reverseFqdn().isEmpty())) {
                this.inner().withDnsSettings(null);
            }
        }

        return this.client.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner(),
                Utils.fromVoidCallback(this, callback));
    }
}