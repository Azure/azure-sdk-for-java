// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.privatedns.fluent.VirtualNetworkLinksClient;
import com.azure.resourcemanager.privatedns.fluent.inner.VirtualNetworkLinkInner;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.privatedns.models.VirtualNetworkLink;
import com.azure.resourcemanager.privatedns.models.VirtualNetworkLinks;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;
import reactor.core.publisher.Mono;

/** Implementation of {@link VirtualNetworkLinks}. */
class VirtualNetworkLinksImpl
    extends ExternalChildResourcesNonCachedImpl<VirtualNetworkLinkImpl,
        VirtualNetworkLink,
        VirtualNetworkLinkInner,
        PrivateDnsZoneImpl,
        PrivateDnsZone>
    implements VirtualNetworkLinks {

    VirtualNetworkLinksImpl(PrivateDnsZoneImpl parent) {
        super(parent, parent.taskGroup(), "VirtualNetworkLink");
    }

    @Override
    public PagedIterable<VirtualNetworkLink> list(int pageSize) {
        return new PagedIterable<>(listAsync(pageSize));
    }

    @Override
    public PagedFlux<VirtualNetworkLink> listAsync(int pageSize) {
        return parent().manager().inner().getVirtualNetworkLinks()
            .listAsync(parent().resourceGroupName(), parent().name(), pageSize)
            .mapPage(this::wrapModel);
    }

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).block();
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return deleteByResourceGroupNameAsync(
            ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id), null);
    }

    @Override
    public void deleteById(String id, String etagValue) {
        deleteByResourceGroupNameAsync(
            ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id), etagValue).block();
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id, String etagValue) {
        return deleteByResourceGroupNameAsync(
            ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id), etagValue);
    }

    @Override
    public void deleteByResourceGroupName(String resourceGroupName, String name) {
        deleteByResourceGroupNameAsync(resourceGroupName, name, null).block();
    }

    @Override
    public Mono<Void> deleteByResourceGroupNameAsync(String resourceGroupName, String name) {
        return deleteByResourceGroupNameAsync(resourceGroupName, name, null);
    }

    @Override
    public void deleteByResourceGroupName(String resourceGroupName, String name, String etagValue) {
        deleteByResourceGroupNameAsync(resourceGroupName, name, etagValue).block();
    }

    @Override
    public Mono<Void> deleteByResourceGroupNameAsync(String resourceGroupName, String name, String etagValue) {
        return parent().manager().inner().getVirtualNetworkLinks().deleteAsync(resourceGroupName, name, etagValue);
    }

    @Override
    public VirtualNetworkLink getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<VirtualNetworkLink> getByIdAsync(String id) {
        return getByNameAsync(ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public VirtualNetworkLink getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<VirtualNetworkLink> getByNameAsync(String name) {
        return parent().manager().inner().getVirtualNetworkLinks()
            .getAsync(parent().resourceGroupName(), parent().name(), name)
            .map(this::wrapModel);
    }

    @Override
    public PrivateDnsZone parent() {
        return getParent();
    }

    @Override
    public PagedIterable<VirtualNetworkLink> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<VirtualNetworkLink> listAsync() {
        return parent().manager().inner().getVirtualNetworkLinks()
            .listAsync(parent().resourceGroupName(), parent().name())
            .mapPage(this::wrapModel);
    }

    @Override
    public VirtualNetworkLinksClient inner() {
        return parent().manager().inner().getVirtualNetworkLinks();
    }

    private VirtualNetworkLink wrapModel(VirtualNetworkLinkInner inner) {
        return inner == null ? null : new VirtualNetworkLinkImpl(inner.name(), getParent(), inner);
    }

    VirtualNetworkLinkImpl defineVirtualNetworkLink(String name) {
        return prepareInlineDefine(VirtualNetworkLinkImpl.newVirtualNetworkLink(name, getParent()));
    }

    VirtualNetworkLinkImpl updateVirtualNetworkLink(String name) {
        return prepareInlineUpdate(VirtualNetworkLinkImpl.newVirtualNetworkLink(name, getParent()));
    }

    void withoutVirtualNetworkLink(String name, String etagValue) {
        prepareInlineRemove(
            VirtualNetworkLinkImpl.newVirtualNetworkLink(name, getParent()).withETagOnDelete(etagValue));
    }
}
