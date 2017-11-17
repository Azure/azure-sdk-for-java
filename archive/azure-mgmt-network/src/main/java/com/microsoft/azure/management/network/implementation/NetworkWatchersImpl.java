/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NetworkWatcher;
import com.microsoft.azure.management.network.NetworkWatchers;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 *  Implementation for Network Watchers.
 */
@LangDefinition
class NetworkWatchersImpl
        extends TopLevelModifiableResourcesImpl<
        NetworkWatcher,
        NetworkWatcherImpl,
        NetworkWatcherInner,
        NetworkWatchersInner,
        NetworkManager>
        implements NetworkWatchers {

    NetworkWatchersImpl(final NetworkManager networkManager) {
        super(networkManager.inner().networkWatchers(), networkManager);
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

