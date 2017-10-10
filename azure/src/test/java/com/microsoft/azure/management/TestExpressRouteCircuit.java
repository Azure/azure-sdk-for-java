/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.network.ExpressRouteCircuit;
import com.microsoft.azure.management.network.ExpressRouteCircuits;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;

/**
 * Tests Express Route Circuit.
 */
public class TestExpressRouteCircuit extends TestTemplate<ExpressRouteCircuit, ExpressRouteCircuits> {
    private static String TEST_ID = "";
    private static Region REGION = Region.US_NORTH_CENTRAL;
    private String circuitName;

    private void initializeResourceNames() {
        TEST_ID = SdkContext.randomResourceName("", 8);
        circuitName = "erc" + TEST_ID;
    }

    @Override
    public ExpressRouteCircuit createResource(ExpressRouteCircuits expressRouteCircuits) throws Exception {
        initializeResourceNames();

        // create Express Route Circuit
        ExpressRouteCircuit erc = expressRouteCircuits.define(circuitName)
                .withRegion(REGION)
                .withNewResourceGroup()
                .withTag("tag1", "value1")
                .create();
        return erc;
    }

    @Override
    public ExpressRouteCircuit updateResource(ExpressRouteCircuit resource) throws Exception {
        resource.update()
                .withTag("tag2", "value2")
                .withoutTag("tag1")
                .apply();
        resource.refresh();
        Assert.assertTrue(resource.tags().containsKey("tag2"));
        Assert.assertTrue(!resource.tags().containsKey("tag1"));
        return resource;
    }

    @Override
    public void print(ExpressRouteCircuit resource) {
        StringBuilder info = new StringBuilder();
        info.append("Network Watcher: ").append(resource.id())
                .append("\n\tName: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.regionName())
                .append("\n\tTags: ").append(resource.tags());
        System.out.println(info.toString());
    }
}

