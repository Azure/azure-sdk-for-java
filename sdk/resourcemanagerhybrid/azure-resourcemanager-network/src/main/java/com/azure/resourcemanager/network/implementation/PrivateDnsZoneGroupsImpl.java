// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.PrivateDnsZoneGroupsClient;
import com.azure.resourcemanager.network.fluent.models.PrivateDnsZoneGroupInner;
import com.azure.resourcemanager.network.models.PrivateDnsZoneGroup;
import com.azure.resourcemanager.network.models.PrivateDnsZoneGroups;
import com.azure.resourcemanager.network.models.PrivateEndpoint;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.IndependentChildrenImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

public class PrivateDnsZoneGroupsImpl
    extends
    IndependentChildrenImpl<
        PrivateDnsZoneGroup, PrivateDnsZoneGroupImpl,
        PrivateDnsZoneGroupInner, PrivateDnsZoneGroupsClient,
        NetworkManager, PrivateEndpoint>
    implements
    PrivateDnsZoneGroups {

    private final PrivateEndpointImpl parent;

    protected PrivateDnsZoneGroupsImpl(PrivateEndpointImpl parent) {
        super(parent.manager().serviceClient().getPrivateDnsZoneGroups(), parent.manager());
        this.parent = parent;
    }

    @Override
    public Mono<Void> deleteByParentAsync(String groupName, String parentName, String name) {
        return this.innerModel().deleteAsync(groupName, parentName, name);
    }

    @Override
    public Mono<PrivateDnsZoneGroup> getByParentAsync(String resourceGroup, String parentName, String name) {
        return this.innerModel().getAsync(resourceGroup, parentName, name).map(this::wrapModel);
    }

    @Override
    public PagedIterable<PrivateDnsZoneGroup> listByParent(String resourceGroupName, String parentName) {
        return PagedConverter.mapPage(this.innerModel().list(parentName, resourceGroupName), this::wrapModel);
    }

    @Override
    protected PrivateDnsZoneGroupImpl wrapModel(String name) {
        return new PrivateDnsZoneGroupImpl(name, new PrivateDnsZoneGroupInner(), this.parent);
    }

    @Override
    protected PrivateDnsZoneGroupImpl wrapModel(PrivateDnsZoneGroupInner innerModel) {
        return new PrivateDnsZoneGroupImpl(innerModel.name(), innerModel, this.parent);
    }

    @Override
    public PrivateDnsZoneGroupImpl define(String name) {
        return this.wrapModel(name);
    }

    @Override
    public PagedIterable<PrivateDnsZoneGroup> list() {
        return new PagedIterable<>(this.listAsync());
    }

    @Override
    public PagedFlux<PrivateDnsZoneGroup> listAsync() {
        return PagedConverter.mapPage(this.innerModel().
            listAsync(parent.name(), parent.resourceGroupName()), this::wrapModel);
    }
}
