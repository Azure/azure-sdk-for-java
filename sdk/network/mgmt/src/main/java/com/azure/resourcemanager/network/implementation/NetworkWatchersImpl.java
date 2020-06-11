// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.NetworkWatchersClient;
import com.azure.resourcemanager.network.fluent.inner.NetworkWatcherInner;
import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.network.models.NetworkWatchers;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** Implementation for Network Watchers. */
public class NetworkWatchersImpl
    extends TopLevelModifiableResourcesImpl<
        NetworkWatcher, NetworkWatcherImpl, NetworkWatcherInner, NetworkWatchersClient, NetworkManager>
    implements NetworkWatchers {

    public NetworkWatchersImpl(final NetworkManager networkManager) {
        super(networkManager.inner().getNetworkWatchers(), networkManager);
    }

    @Override
    public NetworkWatcherImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected NetworkWatcherImpl wrapModel(String name) {
        return new NetworkWatcherImpl(name, new NetworkWatcherInner(), super.manager());
    }

    @Override
    protected NetworkWatcherImpl wrapModel(NetworkWatcherInner inner) {
        if (inner == null) {
            return null;
        }
        return new NetworkWatcherImpl(inner.name(), inner, this.manager());
    }
}
