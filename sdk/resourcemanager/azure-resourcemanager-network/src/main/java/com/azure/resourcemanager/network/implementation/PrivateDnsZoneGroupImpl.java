// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.PrivateDnsZoneGroupInner;
import com.azure.resourcemanager.network.models.PrivateDnsZoneConfig;
import com.azure.resourcemanager.network.models.PrivateDnsZoneGroup;
import com.azure.resourcemanager.network.models.PrivateEndpoint;
import com.azure.resourcemanager.network.models.ProvisioningState;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.IndependentChildImpl;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PrivateDnsZoneGroupImpl extends IndependentChildImpl<
    PrivateDnsZoneGroup, PrivateEndpoint, PrivateDnsZoneGroupInner, PrivateDnsZoneGroupImpl, NetworkManager>
    implements PrivateDnsZoneGroup, PrivateDnsZoneGroup.Definition, PrivateDnsZoneGroup.Update {

    private final PrivateEndpointImpl parent;

    protected PrivateDnsZoneGroupImpl(String name, PrivateDnsZoneGroupInner innerModel, PrivateEndpointImpl parent) {
        super(name, innerModel, parent.manager());
        this.parent = parent;
    }

    @Override
    public PrivateDnsZoneGroupImpl withPrivateDnsZoneConfigure(
        String name, String privateDnsZoneId) {
        if (innerModel().privateDnsZoneConfigs() == null) {
            innerModel().withPrivateDnsZoneConfigs(new ArrayList<>());
        }
        innerModel().privateDnsZoneConfigs().add(
            new PrivateDnsZoneConfig()
                .withName(name)
                .withPrivateDnsZoneId(privateDnsZoneId));
        return this;
    }

    @Override
    public Update withoutPrivateDnsZoneConfigure(String name) {
        if (innerModel().privateDnsZoneConfigs() != null) {
            innerModel().privateDnsZoneConfigs().removeIf(config -> config.name().equals(name));
        }
        return this;
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    protected Mono<PrivateDnsZoneGroup> createChildResourceAsync() {
        return this.manager().serviceClient().getPrivateDnsZoneGroups()
            .createOrUpdateAsync(parent.resourceGroupName(), parent.name(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<PrivateDnsZoneGroupInner> getInnerAsync() {
        return this.manager().serviceClient().getPrivateDnsZoneGroups()
            .getAsync(parent.resourceGroupName(), parent.name(), this.name());
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.innerModel().provisioningState();
    }

    @Override
    public List<PrivateDnsZoneConfig> privateDnsZoneConfigures() {
        if (this.innerModel().privateDnsZoneConfigs() == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.innerModel().privateDnsZoneConfigs());
    }
}
