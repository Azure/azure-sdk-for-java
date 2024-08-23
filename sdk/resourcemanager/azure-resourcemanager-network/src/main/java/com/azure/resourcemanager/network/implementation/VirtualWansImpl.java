// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;

import com.azure.resourcemanager.network.fluent.models.VirtualWanInner;
import com.azure.resourcemanager.network.fluent.VirtualWansClient;
import com.azure.resourcemanager.network.models.VirtualWan;
import com.azure.resourcemanager.network.models.VirtualWans;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

public class VirtualWansImpl
    extends TopLevelModifiableResourcesImpl<VirtualWan, VirtualWanImpl, VirtualWanInner, VirtualWansClient, NetworkManager>
    implements VirtualWans {

    public VirtualWansImpl(final NetworkManager networkManager) {
        super(networkManager.serviceClient().getVirtualWans(), networkManager);
    }

    @Override
    public VirtualWanImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected VirtualWanImpl wrapModel(String name) {
        VirtualWanInner inner = new VirtualWanInner();

        return new VirtualWanImpl(name, inner, super.manager());
    }

    @Override
    protected VirtualWanImpl wrapModel(VirtualWanInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualWanImpl(inner.name(), inner, this.manager());
    }

}
