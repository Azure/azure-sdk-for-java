/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.network.*;
import org.junit.Assert;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Tests Network Watcher.
 */
public class TestNetworkWatcher extends TestTemplate<NetworkWatcher, NetworkWatchers> {

    @Override
    public NetworkWatcher createResource(NetworkWatchers networkWatchers) throws Exception {
        final String newNWName = "nw" + this.testId;
        NetworkWatcher nw = networkWatchers.define(newNWName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup()
                .create();
        return nw;
    }

    @Override
    public NetworkWatcher updateResource(NetworkWatcher resource) throws Exception {
        return resource;
    }

    @Override
    public void print(NetworkWatcher nw) {
        StringBuilder info = new StringBuilder();
        info.append("Network Watcher: ").append(nw.id())
                .append("\n\tName: ").append(nw.name())
                .append("\n\tResource group: ").append(nw.resourceGroupName())
                .append("\n\tRegion: ").append(nw.regionName());
        System.out.println(info.toString());
    }
}
