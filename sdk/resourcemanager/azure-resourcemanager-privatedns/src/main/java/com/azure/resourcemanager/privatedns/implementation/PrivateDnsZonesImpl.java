// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.PrivateDnsZoneManager;
import com.azure.resourcemanager.privatedns.fluent.PrivateZonesClient;
import com.azure.resourcemanager.privatedns.fluent.models.PrivateZoneInner;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZones;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import reactor.core.publisher.Mono;

/** Implementation of {@link PrivateDnsZones}. */
public final class PrivateDnsZonesImpl
    extends TopLevelModifiableResourcesImpl<
        PrivateDnsZone,
        PrivateDnsZoneImpl,
        PrivateZoneInner,
        PrivateZonesClient,
        PrivateDnsZoneManager>
    implements PrivateDnsZones {

    public PrivateDnsZonesImpl(final PrivateDnsZoneManager manager) {
        super(manager.serviceClient().getPrivateZones(), manager);
    }

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).block();
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return deleteByIdAsync(id, null);
    }

    @Override
    public void deleteById(String id, String etagValue) {
        deleteByIdAsync(id, etagValue).block();
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id, String etagValue) {
        return deleteByResourceGroupNameAsync(
            ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id), etagValue);
    }

    @Override
    public void deleteByResourceGroupName(String resourceGroupName, String name) {
        deleteByResourceGroupNameAsync(resourceGroupName, name).block();
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
        return manager().serviceClient().getPrivateZones().deleteAsync(resourceGroupName, name, etagValue);
    }

    @Override
    protected PrivateDnsZoneImpl wrapModel(String name) {
        return new PrivateDnsZoneImpl(name, new PrivateZoneInner(), manager());
    }

    @Override
    protected PrivateDnsZoneImpl wrapModel(PrivateZoneInner inner) {
        if (inner == null) {
            return null;
        }
        return new PrivateDnsZoneImpl(inner.name(), inner, manager());
    }

    @Override
    public PrivateDnsZoneImpl define(String name) {
        return setDefaults(wrapModel(name));
    }

    private PrivateDnsZoneImpl setDefaults(PrivateDnsZoneImpl privateDnsZone) {
        privateDnsZone.innerModel().withLocation("global");
        return privateDnsZone;
    }
}
