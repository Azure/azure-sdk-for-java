// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.AddressSpace;
import com.azure.resourcemanager.network.models.BgpSettings;
import com.azure.resourcemanager.network.models.LocalNetworkGateway;
import com.azure.resourcemanager.network.models.AppliableWithTags;
import com.azure.resourcemanager.network.fluent.models.LocalNetworkGatewayInner;
import com.azure.resourcemanager.network.models.TagsObject;
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
        return innerModel().gatewayIpAddress();
    }

    @Override
    public BgpSettings bgpSettings() {
        return innerModel().bgpSettings();
    }

    @Override
    public Set<String> addressSpaces() {
        Set<String> addressSpaces = new HashSet<>();
        if (this.innerModel().localNetworkAddressSpace() != null
            && this.innerModel().localNetworkAddressSpace().addressPrefixes() != null) {
            addressSpaces.addAll(this.innerModel().localNetworkAddressSpace().addressPrefixes());
        }
        return Collections.unmodifiableSet(addressSpaces);
    }

    @Override
    public String provisioningState() {
        return innerModel().provisioningState().toString();
    }

    @Override
    public LocalNetworkGatewayImpl withIPAddress(String ipAddress) {
        this.innerModel().withGatewayIpAddress(ipAddress);
        return this;
    }

    @Override
    public LocalNetworkGatewayImpl withAddressSpace(String cidr) {
        if (this.innerModel().localNetworkAddressSpace() == null) {
            this.innerModel().withLocalNetworkAddressSpace(new AddressSpace());
        }
        if (this.innerModel().localNetworkAddressSpace().addressPrefixes() == null) {
            this.innerModel().localNetworkAddressSpace().withAddressPrefixes(new ArrayList<String>());
        }

        this.innerModel().localNetworkAddressSpace().addressPrefixes().add(cidr);
        return this;
    }

    @Override
    public LocalNetworkGatewayImpl withoutAddressSpace(String cidr) {
        if (this.innerModel().localNetworkAddressSpace() == null
            || this.innerModel().localNetworkAddressSpace().addressPrefixes() == null) {
            return this;
        }
        this.innerModel().localNetworkAddressSpace().addressPrefixes().remove(cidr);
        return this;
    }

    @Override
    public LocalNetworkGatewayImpl withBgp(long asn, String bgpPeeringAddress) {
        ensureBgpSettings().withAsn(asn).withBgpPeeringAddress(bgpPeeringAddress);
        return this;
    }

    @Override
    public LocalNetworkGatewayImpl withoutBgp() {
        innerModel().withBgpSettings(null);
        return this;
    }

    @Override
    protected Mono<LocalNetworkGatewayInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getLocalNetworkGateways()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<LocalNetworkGateway> createResourceAsync() {
        return this
            .manager()
            .serviceClient()
            .getLocalNetworkGateways()
            .createOrUpdateAsync(resourceGroupName(), name(), innerModel())
            .map(innerToFluentMap(this));
    }

    private BgpSettings ensureBgpSettings() {
        if (innerModel().bgpSettings() == null) {
            innerModel().withBgpSettings(new BgpSettings());
        }
        return innerModel().bgpSettings();
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
            .serviceClient()
            .getLocalNetworkGateways()
            .updateTagsAsync(resourceGroupName(), name(), new TagsObject().withTags(innerModel().tags()))
            .flatMap(
                inner -> {
                    setInner(inner);
                    return Mono.just((LocalNetworkGateway) LocalNetworkGatewayImpl.this);
                });
    }
}
