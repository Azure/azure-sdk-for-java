/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.network.LocalNetworkGateway;
import com.microsoft.azure.management.network.VirtualNetworkGateway;
import com.microsoft.azure.management.network.VirtualNetworkGatewaySkuName;
import com.microsoft.azure.management.network.VirtualNetworkGateways;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;

/**
 * Tests Virtual Network Gateway.
 */
public class TestVirtualNetworkGateway extends TestTemplate<VirtualNetworkGateway, VirtualNetworkGateways> {
    private static String TEST_ID = "";
    private static Region REGION = Region.US_NORTH_CENTRAL;
    private String groupName;
    private String gatewayName;

    private void initializeResourceNames() {
        TEST_ID = SdkContext.randomResourceName("", 8);
        groupName = "rg" + TEST_ID;
        gatewayName = "vngw" + TEST_ID;
    }

    @Override
    public VirtualNetworkGateway createResource(VirtualNetworkGateways virtualNetworkGateways) throws Exception {
        initializeResourceNames();
        VirtualNetworkGateway vngw = virtualNetworkGateways.define(gatewayName)
                .withRegion(REGION)
                .withNewResourceGroup(groupName)
                .withVPN()
                .withRouteBased()
                .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                .withTag("tag1", "value1")
//                .withActiveActive(true)
                .create();
        return vngw;
    }

    @Override
    public VirtualNetworkGateway updateResource(VirtualNetworkGateway resource) throws Exception {
        resource.update()
                .withSku(VirtualNetworkGatewaySkuName.VPN_GW2)
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

    /**
     * Test Site-To-Site Virtual Network Gateway Connection.
     */
    public static class SiteToSite extends TestTemplate<VirtualNetworkGateway, VirtualNetworkGateways> {
        private final NetworkManager networkManager;
        private String groupName;
        private String gatewayName;

        private void initializeResourceNames() {
            TEST_ID = SdkContext.randomResourceName("", 8);
            groupName = "rg" + TEST_ID;
            gatewayName = "vngw" + TEST_ID;
        }

        public SiteToSite(NetworkManager networkManager) {
            initializeResourceNames();
            this.networkManager = networkManager;
        }

        @Override
        public void print(VirtualNetworkGateway resource) {
            printVirtualNetworkGateway(resource);
        }

        @Override
        public VirtualNetworkGateway createResource(VirtualNetworkGateways gateways) throws Exception {

            // Create virtual network gateway
            initializeResourceNames();
            VirtualNetworkGateway vngw = gateways.define(gatewayName)
                    .withRegion(REGION)
                    .withNewResourceGroup()
                    .withVPN()
                    .withRouteBased()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .create();
//            VirtualNetworkGateway vngw = gateways.getByResourceGroup("vngw115313group", "vngw115313");
            LocalNetworkGateway lngw = gateways.manager().localNetworkGateways().define("lngw" + TEST_ID)
                    .withRegion(vngw.region())
                    .withExistingResourceGroup(vngw.resourceGroupName())
                    .withIPAddress("40.71.184.214")
                    .create();
            vngw.connections()
                    .define("myNewConnection")
                    .withSiteToSite()
                    .withLocalNetworkGateway(lngw)
                    .withSharedKey("MySecretKey")
                    .create();

            return vngw;
        }

        @Override
        public VirtualNetworkGateway updateResource(VirtualNetworkGateway resource) throws Exception {
            return resource;
        }
    }

    /**
     * Test VNet-to-VNet Virtual Network Gateway Connection.
     */
    public static class VNetToVNet extends TestTemplate<VirtualNetworkGateway, VirtualNetworkGateways> {
        private final NetworkManager networkManager;
        private String groupName;
        private String gatewayName;
        private String gatewayName2;

        private void initializeResourceNames() {
            TEST_ID = SdkContext.randomResourceName("", 8);
            groupName = "rg" + TEST_ID;
            gatewayName = "vngw" + TEST_ID;
            gatewayName2 = "vngw2" + TEST_ID;
        }

        public VNetToVNet(NetworkManager networkManager) {
            initializeResourceNames();
            this.networkManager = networkManager;
        }

        @Override
        public void print(VirtualNetworkGateway resource) {
            printVirtualNetworkGateway(resource);
        }

        @Override
        public VirtualNetworkGateway createResource(VirtualNetworkGateways gateways) throws Exception {

            // Create virtual network gateway
            initializeResourceNames();
            VirtualNetworkGateway vngw = gateways.define(gatewayName)
                    .withRegion(REGION)
                    .withNewResourceGroup()
                    .withVPN()
                    .withRouteBased()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .create();
            VirtualNetworkGateway vngw2 = gateways.define(gatewayName2)
                    .withRegion(REGION)
                    .withNewResourceGroup()
                    .withVPN()
                    .withRouteBased()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .create();
//            VirtualNetworkGateway vngw = gateways.getByResourceGroup("vngw115313group", "vngw115313");
            LocalNetworkGateway lngw = gateways.manager().localNetworkGateways().define("lngw" + TEST_ID)
                    .withRegion(vngw.region())
                    .withExistingResourceGroup(vngw.resourceGroupName())
                    .withIPAddress("40.71.184.214")
                    .create();
            vngw.connections()
                    .define("myNewConnection")
                    .withVNetToVNet()
                    .withSecondVirtualNetworkGateway(vngw2)
                    .withSharedKey("MySecretKey")
                    .create();
            return vngw;
        }

        @Override
        public VirtualNetworkGateway updateResource(VirtualNetworkGateway resource) throws Exception {
            return resource;
        }
    }

    static void printVirtualNetworkGateway(VirtualNetworkGateway gateway) {
        StringBuilder info = new StringBuilder();
        info.append("Virtual Network Gateway: ").append(gateway.id())
                .append("\n\tName: ").append(gateway.name())
                .append("\n\tResource group: ").append(gateway.resourceGroupName())
                .append("\n\tRegion: ").append(gateway.regionName())
                .append("\n\tTags: ").append(gateway.tags());
        System.out.println(info.toString());
    }
}

