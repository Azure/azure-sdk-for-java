// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.VirtualWanInner;
import com.azure.resourcemanager.network.models.VirtualWan;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

public class VirtualWanImpl extends GroupableResourceImpl<VirtualWan, VirtualWanInner, VirtualWanImpl, NetworkManager>
    implements VirtualWan, VirtualWan.Definition, VirtualWan.Update {

    VirtualWanImpl(String name, VirtualWanInner innerObject, NetworkManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public VirtualWanImpl enableVpnEncryption() {
        this.innerModel().withDisableVpnEncryption(false);
        return this;
    }

    @Override
    public VirtualWanImpl disableVpnEncryption() {
        this.innerModel().withDisableVpnEncryption(true);
        return this;
    }

    @Override
    public VirtualWanImpl withVirtualWanType(String virtualWanType) {
        this.innerModel().withTypePropertiesType(virtualWanType);
        return this;
    }

    @Override
    public VirtualWanImpl withAllowBranchToBranchTraffic(Boolean allowBranchToBranchTraffic) {
        this.innerModel().withAllowBranchToBranchTraffic(allowBranchToBranchTraffic);
        return this;
    }

    @Override
    public Mono<VirtualWan> createResourceAsync() {
        this.innerModel().withLocation(this.regionName());
        this.innerModel().withTags(this.innerModel().tags());
        return this
            .manager()
            .serviceClient()
            .getVirtualWans()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<VirtualWanInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getVirtualWans()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Boolean disabledVpnEncryption() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().disableVpnEncryption());
    }

    @Override
    public String virtualWanType() {
        return this.innerModel().typePropertiesType();
    }

    @Override
    public Boolean allowBranchToBranchTraffic() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().allowBranchToBranchTraffic());
    }
}
