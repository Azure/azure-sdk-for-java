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
    private static String GROUP_NAME;
    private static String GATEWAY_NAME1;
    private static String GATEWAY_NAME2;

    static final String ID_TEMPLATE = "/subscriptions/${subId}/resourceGroups/${rgName}/providers/Microsoft.Network/virtualNetworkGateways/${resourceName}";

    static String createResourceId(String subscriptionId, String gatewayName) {
        return ID_TEMPLATE
                .replace("${subId}", subscriptionId)
                .replace("${rgName}", GROUP_NAME)
                .replace("${resourceName}", gatewayName);
    }

    private static void initializeResourceNames() {
        TEST_ID = SdkContext.randomResourceName("", 8);
        GROUP_NAME = "rg" + TEST_ID;
        GATEWAY_NAME1 = "vngw" + TEST_ID;
        GATEWAY_NAME2 = "vngw2" + TEST_ID;
    }

    @Override
    public VirtualNetworkGateway createResource(VirtualNetworkGateways virtualNetworkGateways) throws Exception {
        initializeResourceNames();
        VirtualNetworkGateway vngw = virtualNetworkGateways.define(GATEWAY_NAME1)
                .withRegion(REGION)
                .withNewResourceGroup(GROUP_NAME)
                .withNewNetwork("10.0.0.0/25", "10.0.0.0/27")
                .withRouteBasedVpn()
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

        public SiteToSite(NetworkManager networkManager) {
            initializeResourceNames();
        }

        @Override
        public void print(VirtualNetworkGateway resource) {
            printVirtualNetworkGateway(resource);
        }

        @Override
        public VirtualNetworkGateway createResource(VirtualNetworkGateways gateways) throws Exception {

            // Create virtual network gateway
            initializeResourceNames();
            VirtualNetworkGateway vngw = gateways.define(GATEWAY_NAME1)
                    .withRegion(REGION)
                    .withNewResourceGroup()
                    .withNewNetwork("10.0.0.0/25", "10.0.0.0/27")
                    .withRouteBasedVpn()
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

        public VNetToVNet(NetworkManager networkManager) {
            initializeResourceNames();
        }

        @Override
        public void print(VirtualNetworkGateway resource) {
            printVirtualNetworkGateway(resource);
        }

        @Override
        public VirtualNetworkGateway createResource(final VirtualNetworkGateways gateways) throws Exception {

            // Create virtual network gateway
            initializeResourceNames();
            Thread creationThread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    VirtualNetworkGateway vngw = gateways.define(GATEWAY_NAME1)
                            .withRegion(REGION)
                            .withNewResourceGroup()
                            .withNewNetwork("10.0.0.0/25", "10.0.0.0/27")
                            .withRouteBasedVpn()
                            .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                            .create();
                }
            });
            Thread creationThread2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    VirtualNetworkGateway vngw2 = gateways.define(GATEWAY_NAME2)
                            .withRegion(REGION)
                            .withNewResourceGroup()
                            .withNewNetwork("10.0.0.0/25", "10.0.0.0/27")
                            .withRouteBasedVpn()
                            .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                            .create();
                }
            });
            creationThread1.start();
            creationThread2.start();
            //...But bail out after 30 sec, as it is enough to test the results
            SdkContext.sleep(60 * 1000);

            // Get the resources as created so far
            VirtualNetworkGateway vngw1 = gateways.manager().virtualNetworkGateways().getById(createResourceId(gateways.manager().subscriptionId(), GATEWAY_NAME1));
            VirtualNetworkGateway vngw2 = gateways.manager().virtualNetworkGateways().getById(createResourceId(gateways.manager().subscriptionId(), GATEWAY_NAME2));
//            VirtualNetworkGateway vngw = gateways.getByResourceGroup("vngw115313group", "vngw115313");
            vngw1.connections()
                    .define("myNewConnection")
                    .withVNetToVNet()
                    .withSecondVirtualNetworkGateway(vngw2)
                    .withSharedKey("MySecretKey")
                    .create();
            return vngw1;
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

