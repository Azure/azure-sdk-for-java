// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.PublicIpPrefixesClient;
import com.azure.resourcemanager.network.fluent.models.PublicIpPrefixInner;
import com.azure.resourcemanager.network.models.AppliableWithTags;
import com.azure.resourcemanager.network.models.IpTag;
import com.azure.resourcemanager.network.models.IpVersion;
import com.azure.resourcemanager.network.models.ProvisioningState;
import com.azure.resourcemanager.network.models.PublicIpPrefix;
import com.azure.resourcemanager.network.models.PublicIpPrefixSku;
import com.azure.resourcemanager.network.models.ReferencedPublicIpAddress;
import com.azure.resourcemanager.network.models.TagsObject;
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
        PublicIpPrefixesClient client = this.manager().serviceClient().getPublicIpPrefixes();
        return client
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    public Mono<PublicIpPrefix> updateResourceAsync() {
        PublicIpPrefixesClient client = this.manager().serviceClient().getPublicIpPrefixes();
        return client
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<PublicIpPrefixInner> getInnerAsync() {
        PublicIpPrefixesClient client = this.manager().serviceClient().getPublicIpPrefixes();
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
        return this
            .manager()
            .serviceClient()
            .getPublicIpPrefixes()
            .updateTagsAsync(resourceGroupName(), name(), new TagsObject().withTags(innerModel().tags()))
            .map(
                inner -> {
                    setInner(inner);
                    return PublicIpPrefixImpl.this;
                });
    }

    @Override
    public boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }

    @Override
    public String ipPrefix() {
        return this.innerModel().ipPrefix();
    }

    @Override
    public List<IpTag> ipTags() {
        return Collections.unmodifiableList(innerModel().ipTags() == null ? new ArrayList<>() : innerModel().ipTags());
    }

    @Override
    public SubResource loadBalancerFrontendIpConfiguration() {
        return this.innerModel().loadBalancerFrontendIpConfiguration();
    }

    @Override
    public Integer prefixLength() {
        return this.innerModel().prefixLength();
    }

    @Override
    public ProvisioningState provisioningState() {
        return ProvisioningState.fromString(innerModel().provisioningState());
    }

    @Override
    public List<ReferencedPublicIpAddress> publicIpAddresses() {
        return Collections
            .unmodifiableList(
                innerModel().publicIpAddresses() == null ? new ArrayList<>() : this.innerModel().publicIpAddresses());
    }

    @Override
    public IpVersion publicIpAddressVersion() {
        return this.innerModel().publicIpAddressVersion();
    }

    @Override
    public String resourceGuid() {
        return this.innerModel().resourceGuid();
    }

    @Override
    public PublicIpPrefixSku sku() {
        return this.innerModel().sku();
    }

    @Override
    public Set<AvailabilityZoneId> availabilityZones() {
        Set<AvailabilityZoneId> zones = new HashSet<>();
        if (this.innerModel().zones() != null) {
            for (String zone : this.innerModel().zones()) {
                zones.add(AvailabilityZoneId.fromString(zone));
            }
        }
        return Collections.unmodifiableSet(zones);
    }

    @Override
    public PublicIpPrefixImpl withIpTags(List<IpTag> ipTags) {
        this.innerModel().withIpTags(ipTags);
        return this;
    }

    @Override
    public PublicIpPrefixImpl withPrefixLength(Integer prefixLength) {
        this.innerModel().withPrefixLength(prefixLength);
        return this;
    }

    @Override
    public PublicIpPrefixImpl withPublicIpAddressVersion(IpVersion publicIpAddressVersion) {
        this.innerModel().withPublicIpAddressVersion(publicIpAddressVersion);
        return this;
    }

    @Override
    public PublicIpPrefixImpl withSku(PublicIpPrefixSku sku) {
        this.innerModel().withSku(sku);
        return this;
    }

    @Override
    public PublicIpPrefixImpl withAvailabilityZone(AvailabilityZoneId zoneId) {
        // Note: Zone is not updatable as of now, so this is available only during definition time.
        // Service return `ResourceAvailabilityZonesCannotBeModified` upon attempt to append a new
        // zone or remove one. Trying to remove the last one means attempt to change resource from
        // zonal to regional, which is not supported.
        //
        if (this.innerModel().zones() == null) {
            this.innerModel().withZones(new ArrayList<>());
        }
        this.innerModel().zones().add(zoneId.toString());
        return this;
    }
}
