// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.NetworkProfilesClient;
import com.azure.resourcemanager.network.fluent.models.NetworkProfileInner;
import com.azure.resourcemanager.network.models.NetworkProfile;
import com.azure.resourcemanager.network.models.NetworkProfiles;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

public final class NetworkProfilesImpl extends
    TopLevelModifiableResourcesImpl<NetworkProfile, NetworkProfileImpl, NetworkProfileInner, NetworkProfilesClient,
        NetworkManager>
    implements NetworkProfiles {

    public NetworkProfilesImpl(final NetworkManager networkManager) {
        super(networkManager.serviceClient().getNetworkProfiles(), networkManager);
    }

    @Override
    public NetworkProfileImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected NetworkProfileImpl wrapModel(String name) {
        return new NetworkProfileImpl(name, new NetworkProfileInner(), this.manager());
    }

    @Override
    protected NetworkProfileImpl wrapModel(NetworkProfileInner inner) {
        if (inner == null) {
            return null;
        }
        return new NetworkProfileImpl(inner.name(), inner, this.manager());
    }
}
