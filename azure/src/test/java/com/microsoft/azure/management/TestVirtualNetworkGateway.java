/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.network.LocalNetworkGateway;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.network.VirtualNetworkGateway;
import com.microsoft.azure.management.network.VirtualNetworkGatewayConnection;
import com.microsoft.azure.management.network.VirtualNetworkGatewaySkuName;
import com.microsoft.azure.management.network.VirtualNetworkGateways;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;


/**
 * Tests Virtual Network Gateway.
 */
public class TestVirtualNetworkGateway {
    private static String TEST_ID = "";
    private static Region REGION = Region.US_NORTH_CENTRAL;
    private static String GROUP_NAME;
    private static String GATEWAY_NAME1;
    private static String GATEWAY_NAME2;
    private static String CONNECTION_NAME = "myNewConnection";

    private static void initializeResourceNames() {
        TEST_ID = SdkContext.randomResourceName("", 8);
        GROUP_NAME = "rg" + TEST_ID;
        GATEWAY_NAME1 = "vngw" + TEST_ID;
        GATEWAY_NAME2 = "vngw2" + TEST_ID;
    }

    /**
     * Test Virtual Network Gateway Create and Update.
     */
    public static class Basic extends TestTemplate<VirtualNetworkGateway, VirtualNetworkGateways> {

        public Basic(NetworkManager networkManager) {
            initializeResourceNames();
        }

        @Override
        public void print(VirtualNetworkGateway resource) {
            printVirtualNetworkGateway(resource);
        }

        @Override
        public VirtualNetworkGateway createResource(VirtualNetworkGateways gateways) throws Exception {
            VirtualNetworkGateway vngw = gateways.define(GATEWAY_NAME1)
                    .withRegion(REGION)
                    .withNewResourceGroup(GROUP_NAME)
                    .withNewNetwork("10.0.0.0/25", "10.0.0.0/27")
                    .withRouteBasedVpn()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .withTag("tag1", "value1")
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
            LocalNetworkGateway lngw = gateways.manager().localNetworkGateways().define("lngw" + TEST_ID)
                    .withRegion(vngw.region())
                    .withExistingResourceGroup(vngw.resourceGroupName())
                    .withIPAddress("40.71.184.214")
                    .withAddressSpace("192.168.3.0/24")
                    .create();
            vngw.connections()
                    .define(CONNECTION_NAME)
                    .withSiteToSite()
                    .withLocalNetworkGateway(lngw)
                    .withSharedKey("MySecretKey")
                    .create();

            Assert.assertEquals(1, vngw.ipConfigurations().size());
            Subnet subnet = vngw.ipConfigurations().iterator().next().getSubnet();
            Assert.assertEquals("10.0.0.0/27", subnet.addressPrefix());

            Assert.assertEquals("40.71.184.214", lngw.ipAddress());
            Assert.assertEquals(1, lngw.addressSpaces().size());
            Assert.assertEquals("192.168.3.0/24", lngw.addressSpaces().iterator().next());

            List<VirtualNetworkGatewayConnection> connections = vngw.listConnections();
            Assert.assertEquals(1, connections.size());
            Assert.assertEquals(vngw.id(), connections.get(0).virtualNetworkGateway1Id());
            Assert.assertEquals(lngw.id(), connections.get(0).localNetworkGateway2Id());
            return vngw;
        }

        @Override
        public VirtualNetworkGateway updateResource(VirtualNetworkGateway resource) throws Exception {
            resource.connections().deleteByName(CONNECTION_NAME);
            List<VirtualNetworkGatewayConnection> connections = resource.listConnections();
            Assert.assertEquals(0, connections.size());
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
            final List<VirtualNetworkGateway> gws = new ArrayList<>();
            Observable<?> vngwObservable = gateways.define(GATEWAY_NAME1)
                    .withRegion(REGION)
                    .withNewResourceGroup(GROUP_NAME)
                    .withNewNetwork("10.11.0.0/16", "10.11.255.0/27")
                    .withRouteBasedVpn()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .createAsync();

            Observable<?> vngw2Observable = gateways.define(GATEWAY_NAME2)
                    .withRegion(REGION)
                    .withNewResourceGroup(GROUP_NAME)
                    .withNewNetwork("10.41.0.0/16", "10.41.255.0/27")
                    .withRouteBasedVpn()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .createAsync();

            Observable.merge(vngwObservable, vngw2Observable).map(new Func1<Object, Void>() {
                @Override
                public Void call(Object object) {
                    if (object instanceof VirtualNetworkGateway) {
                        gws.add((VirtualNetworkGateway) object);
                    }
                    return null;
                }
            }).toCompletable().await();
            VirtualNetworkGateway vngw1 = gws.get(0);
            VirtualNetworkGateway vngw2 = gws.get(1);
            vngw1.connections()
                    .define(CONNECTION_NAME)
                    .withVNetToVNet()
                    .withSecondVirtualNetworkGateway(vngw2)
                    .withSharedKey("MySecretKey")
                    .create();
            List<VirtualNetworkGatewayConnection> connections = vngw1.listConnections();
            Assert.assertEquals(1, connections.size());
            Assert.assertEquals(vngw1.id(), connections.get(0).virtualNetworkGateway1Id());
            Assert.assertEquals(vngw2.id(), connections.get(0).virtualNetworkGateway2Id());
            return vngw1;
        }

        @Override
        public VirtualNetworkGateway updateResource(VirtualNetworkGateway resource) throws Exception {
            resource.connections().deleteByName(CONNECTION_NAME);
            List<VirtualNetworkGatewayConnection> connections = resource.listConnections();
            Assert.assertEquals(0, connections.size());
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

