// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.privatedns.fluent.models.VirtualNetworkLinkInner;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.privatedns.models.ProvisioningState;
import com.azure.resourcemanager.privatedns.models.VirtualNetworkLink;
import com.azure.resourcemanager.privatedns.models.VirtualNetworkLinkState;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ETagState;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/** Implementation of {@link VirtualNetworkLink}. */
class VirtualNetworkLinkImpl
    extends ExternalChildResourceImpl<VirtualNetworkLink, VirtualNetworkLinkInner, PrivateDnsZoneImpl, PrivateDnsZone>
    implements VirtualNetworkLink,
        VirtualNetworkLink.Definition<PrivateDnsZone.DefinitionStages.WithCreate>,
        VirtualNetworkLink.UpdateDefinition<PrivateDnsZone.Update>,
        VirtualNetworkLink.Update {

    private final ETagState etagState = new ETagState();
    private VirtualNetworkLinkInner linkToRemove;

    VirtualNetworkLinkImpl(String name, final PrivateDnsZoneImpl parent, final VirtualNetworkLinkInner innerModel) {
        super(name, parent, innerModel);
        linkToRemove = new VirtualNetworkLinkInner();
        linkToRemove.withTags(new HashMap<>());
    }

    static VirtualNetworkLinkImpl newVirtualNetworkLink(String name, PrivateDnsZoneImpl parent) {
        VirtualNetworkLinkInner inner = new VirtualNetworkLinkInner();
        inner.withLocation("global");
        inner.withRegistrationEnabled(false);
        return new VirtualNetworkLinkImpl(name, parent, inner);
    }

    @Override
    public String etag() {
        return innerModel().etag();
    }

    @Override
    public String referencedVirtualNetworkId() {
        return innerModel().virtualNetwork() == null ? null : innerModel().virtualNetwork().id();
    }

    @Override
    public boolean isAutoRegistrationEnabled() {
        return innerModel().registrationEnabled();
    }

    @Override
    public VirtualNetworkLinkState virtualNetworkLinkState() {
        return innerModel().virtualNetworkLinkState();
    }

    @Override
    public ProvisioningState provisioningState() {
        return innerModel().provisioningState();
    }

    @Override
    public VirtualNetworkLinkImpl enableAutoRegistration() {
        innerModel().withRegistrationEnabled(true);
        return this;
    }

    @Override
    public VirtualNetworkLinkImpl disableAutoRegistration() {
        innerModel().withRegistrationEnabled(false);
        return this;
    }

    @Override
    public VirtualNetworkLinkImpl withETagCheck() {
        etagState.withImplicitETagCheckOnCreateOrUpdate(isInCreateMode());
        return this;
    }

    @Override
    public VirtualNetworkLinkImpl withETagCheck(String etagValue) {
        etagState.withExplicitETagCheckOnUpdate(etagValue);
        return this;
    }

    @Override
    public VirtualNetworkLinkImpl withVirtualNetworkId(String virtualNetworkId) {
        innerModel().withVirtualNetwork(new SubResource().withId(virtualNetworkId));
        return this;
    }

    @Override
    public VirtualNetworkLinkImpl withRegion(String regionName) {
        innerModel().withLocation(regionName);
        return this;
    }

    @Override
    public VirtualNetworkLinkImpl withRegion(Region region) {
        return withRegion(region.name());
    }

    @Override
    public VirtualNetworkLinkImpl withTags(Map<String, String> tags) {
        innerModel().withTags(tags);
        return this;
    }

    @Override
    public VirtualNetworkLinkImpl withTag(String key, String value) {
        if (innerModel().tags() == null) {
            innerModel().withTags(new HashMap<>());
        }
        innerModel().tags().put(key, value);
        return this;
    }

    @Override
    public VirtualNetworkLinkImpl withoutTag(String key) {
        linkToRemove.tags().put(key, null);
        return this;
    }

    @Override
    public Mono<VirtualNetworkLink> createResourceAsync() {
        return createOrUpdateAsync(innerModel());
    }

    @Override
    public Mono<VirtualNetworkLink> updateResourceAsync() {
        return parent().manager().serviceClient().getVirtualNetworkLinks()
            .getAsync(parent().resourceGroupName(), parent().name(), name())
            .map(virtualNetworkLinkInner -> prepareForUpdate(virtualNetworkLinkInner))
            .flatMap(virtualNetworkLinkInner -> createOrUpdateAsync(virtualNetworkLinkInner));
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return parent().manager().serviceClient().getVirtualNetworkLinks()
            .deleteAsync(parent().resourceGroupName(), parent().name(), name(), etagState.ifMatchValueOnDelete());
    }

    @Override
    protected Mono<VirtualNetworkLinkInner> getInnerAsync() {
        return parent().manager().serviceClient().getVirtualNetworkLinks()
            .getAsync(parent().resourceGroupName(), parent().name(), name());
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public PrivateDnsZoneImpl attach() {
        return parent();
    }

    private Mono<VirtualNetworkLink> createOrUpdateAsync(VirtualNetworkLinkInner resource) {
        final VirtualNetworkLinkImpl self = this;
        return parent().manager().serviceClient().getVirtualNetworkLinks()
            .createOrUpdateAsync(
                parent().resourceGroupName(),
                parent().name(),
                name(),
                resource,
                etagState.ifMatchValueOnUpdate(resource.etag()),
                etagState.ifNonMatchValueOnCreate())
            .map(virtualNetworkLinkInner -> {
                setInner(virtualNetworkLinkInner);
                self.etagState.clear();
                return self;
            });
    }

    private VirtualNetworkLinkInner prepareForUpdate(VirtualNetworkLinkInner resource) {
        if (innerModel().registrationEnabled() != null) {
            resource.withRegistrationEnabled(innerModel().registrationEnabled());
        }
        if (innerModel().tags() != null && !innerModel().tags().isEmpty()) {
            if (resource.tags() == null) {
                resource.withTags(new HashMap<>());
            }
            for (Map.Entry<String, String> entryToAdd : innerModel().tags().entrySet()) {
                resource.tags().put(entryToAdd.getKey(), entryToAdd.getValue());
            }
            innerModel().tags().clear();
        }
        if (!linkToRemove.tags().isEmpty()) {
            for (Map.Entry<String, String> entryToDelete : linkToRemove.tags().entrySet()) {
                resource.tags().remove(entryToDelete.getKey());
            }
            linkToRemove.tags().clear();
        }
        return resource;
    }

    VirtualNetworkLinkImpl withETagOnDelete(String etagValue) {
        etagState.withExplicitETagCheckOnDelete(etagValue);
        return this;
    }

    private boolean isInCreateMode() {
        return innerModel().id() == null;
    }
}
