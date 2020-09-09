// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.PublicIpPrefixesClient;
import com.azure.resourcemanager.network.fluent.inner.PublicIpPrefixInner;
import com.azure.resourcemanager.network.models.AppliableWithTags;
import com.azure.resourcemanager.network.models.IpTag;
import com.azure.resourcemanager.network.models.IpVersion;
import com.azure.resourcemanager.network.models.ProvisioningState;
import com.azure.resourcemanager.network.models.PublicIpPrefix;
import com.azure.resourcemanager.network.models.PublicIpPrefixSku;
import com.azure.resourcemanager.network.models.ReferencedPublicIpAddress;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class PublicIpPrefixImpl
    extends GroupableResourceImpl<PublicIpPrefix, PublicIpPrefixInner, PublicIpPrefixImpl, NetworkManager>
    implements PublicIpPrefix, PublicIpPrefix.Definition, PublicIpPrefix.Update, AppliableWithTags<PublicIpPrefix> {

    PublicIpPrefixImpl(String name, PublicIpPrefixInner inner, NetworkManager manager) {
        super(name, inner, manager);
    }

    @Override
    public Mono<PublicIpPrefix> createResourceAsync() {
        PublicIpPrefixesClient client = this.manager().inner().getPublicIpPrefixes();
        return client.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
            .map(innerToFluentMap(this));
    }

    @Override
    public Mono<PublicIpPrefix> updateResourceAsync() {
        PublicIpPrefixesClient client = this.manager().inner().getPublicIpPrefixes();
        return client.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<PublicIpPrefixInner> getInnerAsync() {
        PublicIpPrefixesClient client = this.manager().inner().getPublicIpPrefixes();
        return client.getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public PublicIpPrefixImpl updateTags() {
        return this;
    }

    @Override
    public PublicIpPrefix applyTags() {
        return applyTagsAsync().block();
    }

    @Override
    public Mono<PublicIpPrefix> applyTagsAsync() {
        return this.manager().inner().getPublicIpPrefixes().updateTagsAsync(resourceGroupName(), name(), inner().tags())
            .map(inner -> {
                setInner(inner);
                return PublicIpPrefixImpl.this;
            });
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }

    @Override
    public String ipPrefix() {
        return this.inner().ipPrefix();
    }

    @Override
    public List<IpTag> ipTags() {
        return Collections.unmodifiableList(inner().ipTags() == null ? new ArrayList<>() : inner().ipTags());
    }

    @Override
    public SubResource loadBalancerFrontendIpConfiguration() {
        return this.inner().loadBalancerFrontendIpConfiguration();
    }

    @Override
    public Integer prefixLength() {
        return this.inner().prefixLength();
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.inner().provisioningState();
    }

    @Override
    public List<ReferencedPublicIpAddress> publicIpAddresses() {
        return Collections.unmodifiableList(
            inner().publicIpAddresses() == null ? new ArrayList<>() : this.inner().publicIpAddresses());
    }

    @Override
    public IpVersion publicIpAddressVersion() {
        return this.inner().publicIpAddressVersion();
    }

    @Override
    public String resourceGuid() {
        return this.inner().resourceGuid();
    }

    @Override
    public PublicIpPrefixSku sku() {
        return this.inner().sku();
    }

    @Override
    public Set<AvailabilityZoneId> availabilityZones() {
        Set<AvailabilityZoneId> zones = new HashSet<>();
        if (this.inner().zones() != null) {
            for (String zone : this.inner().zones()) {
                zones.add(AvailabilityZoneId.fromString(zone));
            }
        }
        return Collections.unmodifiableSet(zones);
    }

    @Override
    public PublicIpPrefixImpl withIpTags(List<IpTag> ipTags) {
        this.inner().withIpTags(ipTags);
        return this;
    }

    @Override
    public PublicIpPrefixImpl withPrefixLength(Integer prefixLength) {
        this.inner().withPrefixLength(prefixLength);
        return this;
    }

    @Override
    public PublicIpPrefixImpl withPublicIpAddressVersion(IpVersion publicIpAddressVersion) {
        this.inner().withPublicIpAddressVersion(publicIpAddressVersion);
        return this;
    }

    @Override
    public PublicIpPrefixImpl withSku(PublicIpPrefixSku sku) {
        this.inner().withSku(sku);
        return this;
    }

    @Override
    public PublicIpPrefixImpl withAvailabilityZone(AvailabilityZoneId zoneId) {
        // Note: Zone is not updatable as of now, so this is available only during definition time.
        // Service return `ResourceAvailabilityZonesCannotBeModified` upon attempt to append a new
        // zone or remove one. Trying to remove the last one means attempt to change resource from
        // zonal to regional, which is not supported.
        //
        if (this.inner().zones() == null) {
            this.inner().withZones(new ArrayList<>());
        }
        this.inner().zones().add(zoneId.toString());
        return this;
    }
}
