/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.network.LocalNetworkGateway;
import com.microsoft.azure.management.network.LocalNetworkGateways;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;

/**
 * Tests Local Network Gateway.
 */
public class TestLocalNetworkGateway  extends TestTemplate<LocalNetworkGateway, LocalNetworkGateways> {
    private static String TEST_ID = "";
    private static Region REGION = Region.US_NORTH_CENTRAL;
    private String groupName;
    private String lngwName;

    private void initializeResourceNames() {
        TEST_ID = SdkContext.randomResourceName("", 8);
        groupName = "rg" + TEST_ID;
        lngwName = "lngw" + TEST_ID;
    }

    @Override
    public LocalNetworkGateway createResource(LocalNetworkGateways localNetworkGateways) throws Exception {
        initializeResourceNames();
        LocalNetworkGateway gateway = localNetworkGateways.define(lngwName)
                .withRegion(REGION)
                .withNewResourceGroup(groupName)
                .withIPAddress("40.71.184.214")
                .create();
        return gateway;
    }

    @Override
    public LocalNetworkGateway updateResource(LocalNetworkGateway resource) throws Exception {
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
    public void print(LocalNetworkGateway gateway) {
        StringBuilder info = new StringBuilder();
        info.append("Local Network Gateway: ").append(gateway.id())
                .append("\n\tName: ").append(gateway.name())
                .append("\n\tResource group: ").append(gateway.resourceGroupName())
                .append("\n\tRegion: ").append(gateway.regionName())
                .append("\n\tTags: ").append(gateway.tags());
        System.out.println(info.toString());
    }
}

