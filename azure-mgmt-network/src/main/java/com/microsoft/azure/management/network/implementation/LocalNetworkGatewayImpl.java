/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.AddressSpace;
import com.microsoft.azure.management.network.BgpSettings;
import com.microsoft.azure.management.network.LocalNetworkGateway;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation for LocalNetworkGateway and its create and update interfaces.
 */
@LangDefinition
class LocalNetworkGatewayImpl
        extends GroupableResourceImpl<
                LocalNetworkGateway,
                LocalNetworkGatewayInner,
                LocalNetworkGatewayImpl,
                NetworkManager>
        implements
        LocalNetworkGateway,
        LocalNetworkGateway.Definition,
        LocalNetworkGateway.Update {

    LocalNetworkGatewayImpl(String name,
                              final LocalNetworkGatewayInner innerModel,
                              final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    @Override
    public String ipAddress() {
        return inner().gatewayIpAddress();
    }

    @Override
    public BgpSettings bgpSettings() {
        return inner().bgpSettings();
    }

    @Override
    public Set<String> addressSpaces() {
        Set<String> addressSpaces = new HashSet<>();
        if (this.inner().localNetworkAddressSpace() != null && this.inner().localNetworkAddressSpace().addressPrefixes() != null) {
            addressSpaces.addAll(this.inner().localNetworkAddressSpace().addressPrefixes());
        }
        return Collections.unmodifiableSet(addressSpaces);
    }

    @Override
    public String provisioningState() {
        return inner().provisioningState();
    }

    @Override
    public LocalNetworkGatewayImpl withIPAddress(String ipAddress) {
        this.inner().withGatewayIpAddress(ipAddress);
        return this;
    }

    @Override
    public LocalNetworkGatewayImpl withAddressSpace(String cidr) {
        if (this.inner().localNetworkAddressSpace() == null) {
            this.inner().withLocalNetworkAddressSpace(new AddressSpace());
        }
        if (this.inner().localNetworkAddressSpace().addressPrefixes() == null) {
            this.inner().localNetworkAddressSpace().withAddressPrefixes(new ArrayList<String>());
        }

        this.inner().localNetworkAddressSpace().addressPrefixes().add(cidr);
        return this;
    }

    @Override
    public LocalNetworkGatewayImpl withoutAddressSpace(String cidr) {
        if (this.inner().localNetworkAddressSpace() == null || this.inner().localNetworkAddressSpace().addressPrefixes() == null) {
            return this;
        }
        this.inner().localNetworkAddressSpace().addressPrefixes().remove(cidr);
        return this;
    }

    @Override
    public LocalNetworkGatewayImpl withBgp(long asn, String bgpPeeringAddress) {
        ensureBgpSettings().withAsn(asn).withBgpPeeringAddress(bgpPeeringAddress);
        return this;
    }

    @Override
    public LocalNetworkGatewayImpl withoutBgp() {
        inner().withBgpSettings(null);
        return this;
    }

    @Override
    protected Observable<LocalNetworkGatewayInner> getInnerAsync() {
        return this.manager().inner().localNetworkGateways().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Observable<LocalNetworkGateway> createResourceAsync() {
        return this.manager().inner().localNetworkGateways().createOrUpdateAsync(resourceGroupName(), name(), inner())
                .map(innerToFluentMap(this));
    }

    private BgpSettings ensureBgpSettings() {
        if (inner().bgpSettings() == null) {
            inner().withBgpSettings(new BgpSettings());
        }
        return inner().bgpSettings();
    }
}
