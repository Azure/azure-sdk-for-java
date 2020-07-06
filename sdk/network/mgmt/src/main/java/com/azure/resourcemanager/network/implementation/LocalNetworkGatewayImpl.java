// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.AddressSpace;
import com.azure.resourcemanager.network.models.BgpSettings;
import com.azure.resourcemanager.network.models.LocalNetworkGateway;
import com.azure.resourcemanager.network.models.AppliableWithTags;
import com.azure.resourcemanager.network.fluent.inner.LocalNetworkGatewayInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import reactor.core.publisher.Mono;

/** Implementation for LocalNetworkGateway and its create and update interfaces. */
class LocalNetworkGatewayImpl
    extends GroupableResourceImpl<
        LocalNetworkGateway, LocalNetworkGatewayInner, LocalNetworkGatewayImpl, NetworkManager>
    implements LocalNetworkGateway,
        LocalNetworkGateway.Definition,
        LocalNetworkGateway.Update,
        AppliableWithTags<LocalNetworkGateway> {

    LocalNetworkGatewayImpl(
        String name, final LocalNetworkGatewayInner innerModel, final NetworkManager networkManager) {
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
        if (this.inner().localNetworkAddressSpace() != null
            && this.inner().localNetworkAddressSpace().addressPrefixes() != null) {
            addressSpaces.addAll(this.inner().localNetworkAddressSpace().addressPrefixes());
        }
        return Collections.unmodifiableSet(addressSpaces);
    }

    @Override
    public String provisioningState() {
        return inner().provisioningState().toString();
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
        if (this.inner().localNetworkAddressSpace() == null
            || this.inner().localNetworkAddressSpace().addressPrefixes() == null) {
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
    protected Mono<LocalNetworkGatewayInner> getInnerAsync() {
        return this
            .manager()
            .inner()
            .getLocalNetworkGateways()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<LocalNetworkGateway> createResourceAsync() {
        return this
            .manager()
            .inner()
            .getLocalNetworkGateways()
            .createOrUpdateAsync(resourceGroupName(), name(), inner())
            .map(innerToFluentMap(this));
    }

    private BgpSettings ensureBgpSettings() {
        if (inner().bgpSettings() == null) {
            inner().withBgpSettings(new BgpSettings());
        }
        return inner().bgpSettings();
    }

    @Override
    public LocalNetworkGatewayImpl updateTags() {
        return this;
    }

    @Override
    public LocalNetworkGateway applyTags() {
        return applyTagsAsync().block();
    }

    @Override
    public Mono<LocalNetworkGateway> applyTagsAsync() {
        return this
            .manager()
            .inner()
            .getLocalNetworkGateways()
            .updateTagsAsync(resourceGroupName(), name(), inner().tags())
            .flatMap(
                inner -> {
                    setInner(inner);
                    return Mono.just((LocalNetworkGateway) LocalNetworkGatewayImpl.this);
                });
    }
}
