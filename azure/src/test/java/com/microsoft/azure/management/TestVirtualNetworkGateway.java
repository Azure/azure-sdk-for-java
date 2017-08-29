/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.network.VirtualNetworkGateway;
import com.microsoft.azure.management.network.VirtualNetworkGateways;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;

/**
 * Tests Virtual Network Gateway.
 */
public class TestVirtualNetworkGateway  extends TestTemplate<VirtualNetworkGateway, VirtualNetworkGateways> {
    private static String TEST_ID = "";
    private static Region REGION = Region.US_NORTH_CENTRAL;
    private String groupName;
    private String nwName;

    private void initializeResourceNames() {
        TEST_ID = SdkContext.randomResourceName("", 8);
        groupName = "rg" + TEST_ID;
        nwName = "vngw" + TEST_ID;
    }

    @Override
    public VirtualNetworkGateway createResource(VirtualNetworkGateways virtualNetworkGateways) throws Exception {
        initializeResourceNames();
        VirtualNetworkGateway nw = virtualNetworkGateways.define(nwName)
                .withRegion(REGION)
                .withNewResourceGroup()
                .withVPN()
                .withRouteBased()
                .withSku()
                .withNewPublicIPAddress()
                .withTag("tag1", "value1")
                .create();
        return nw;
    }

    @Override
    public VirtualNetworkGateway updateResource(VirtualNetworkGateway resource) throws Exception {
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
    public void print(VirtualNetworkGateway gateway) {
        StringBuilder info = new StringBuilder();
        info.append("Virtual Network Gateway: ").append(gateway.id())
                .append("\n\tName: ").append(gateway.name())
                .append("\n\tResource group: ").append(gateway.resourceGroupName())
                .append("\n\tRegion: ").append(gateway.regionName())
                .append("\n\tTags: ").append(gateway.tags());
        System.out.println(info.toString());
    }
}

