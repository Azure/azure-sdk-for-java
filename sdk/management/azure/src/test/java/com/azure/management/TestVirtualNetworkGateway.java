/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.network.LocalNetworkGateway;
import com.azure.management.network.Network;
import com.azure.management.network.Subnet;
import com.azure.management.network.VirtualNetworkGateway;
import com.azure.management.network.VirtualNetworkGatewayConnection;
import com.azure.management.network.VirtualNetworkGatewaySkuName;
import com.azure.management.network.VirtualNetworkGateways;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.resources.core.TestUtilities;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Tests Virtual Network Gateway.
 */
public class TestVirtualNetworkGateway {
    private String TEST_ID = "";
    private Region REGION = Region.US_NORTH_CENTRAL;
    private String GROUP_NAME;
    private String GATEWAY_NAME1;
    private String GATEWAY_NAME2;
    private String NETWORK_NAME;
    private String CONNECTION_NAME = "myNewConnection";
    private String CERTIFICATE_NAME = "myTest3.cer";

    private void initializeResourceNames(SdkContext sdkContext) {
        TEST_ID = sdkContext.randomResourceName("", 8);
        GROUP_NAME = "rg" + TEST_ID;
        GATEWAY_NAME1 = "vngw" + TEST_ID;
        GATEWAY_NAME2 = "vngw2" + TEST_ID;
        NETWORK_NAME = "nw" + TEST_ID;
    }

    /**
     * Test Virtual Network Gateway Create and Update.
     */
    public class Basic extends TestTemplate<VirtualNetworkGateway, VirtualNetworkGateways> {

        public Basic(NetworkManager networkManager) {
            initializeResourceNames(networkManager.getSdkContext());
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
            Assertions.assertTrue(resource.tags().containsKey("tag2"));
            Assertions.assertTrue(!resource.tags().containsKey("tag1"));

            Map<String, String> tagsMap = new HashMap<>();
            tagsMap.put("tag3", "value3");
            tagsMap.put("tag4", "value4");
            resource.updateTags()
                    .withTags(tagsMap)
                    .applyTags();
            Assertions.assertEquals(2, resource.tags().size());
            Assertions.assertEquals("value4", resource.tags().get("tag4"));
            return resource;
        }
    }


    /**
     * Test Site-To-Site Virtual Network Gateway Connection.
     */
    public class SiteToSite extends TestTemplate<VirtualNetworkGateway, VirtualNetworkGateways> {
        public SiteToSite(NetworkManager networkManager) {
            initializeResourceNames(networkManager.getSdkContext());
        }

        @Override
        public void print(VirtualNetworkGateway resource) {
            printVirtualNetworkGateway(resource);
        }

        @Override
        public VirtualNetworkGateway createResource(VirtualNetworkGateways gateways) throws Exception {

            // Create virtual network gateway
            initializeResourceNames(gateways.manager().getSdkContext());
            VirtualNetworkGateway vngw = gateways.define(GATEWAY_NAME1)
                    .withRegion(REGION)
                    .withNewResourceGroup()
                    .withNewNetwork("10.0.0.0/25", "10.0.0.0/27")
                    .withRouteBasedVpn()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .withBgp(65010, "10.12.255.30")
                    .create();
            LocalNetworkGateway lngw = gateways.manager().localNetworkGateways().define("lngw" + TEST_ID)
                    .withRegion(vngw.region())
                    .withExistingResourceGroup(vngw.resourceGroupName())
                    .withIPAddress("40.71.184.214")
                    .withAddressSpace("192.168.3.0/24")
                    .withBgp(65050, "10.51.255.254")
                    .create();
            VirtualNetworkGatewayConnection connection = vngw.connections()
                    .define(CONNECTION_NAME)
                    .withSiteToSite()
                    .withLocalNetworkGateway(lngw)
                    .withSharedKey("MySecretKey")
                    .withTag("tag1", "value1")
                    .create();

            Assertions.assertEquals(1, vngw.ipConfigurations().size());
            Subnet subnet = vngw.ipConfigurations().iterator().next().getSubnet();
            Assertions.assertEquals("10.0.0.0/27", subnet.addressPrefix());
            Assertions.assertTrue(vngw.isBgpEnabled());
            Assertions.assertEquals("10.12.255.30", vngw.bgpSettings().bgpPeeringAddress());

            Assertions.assertEquals("40.71.184.214", lngw.ipAddress());
            Assertions.assertEquals(1, lngw.addressSpaces().size());
            Assertions.assertEquals("192.168.3.0/24", lngw.addressSpaces().iterator().next());
            Assertions.assertNotNull(lngw.bgpSettings());
            Assertions.assertEquals("10.51.255.254", lngw.bgpSettings().bgpPeeringAddress());

            PagedIterable<VirtualNetworkGatewayConnection> connections = vngw.listConnections();
            Assertions.assertEquals(1, TestUtilities.getSize(connections));
            VirtualNetworkGatewayConnection conn = connections.iterator().next();
            Assertions.assertEquals(vngw.id(), conn.virtualNetworkGateway1Id());
            Assertions.assertEquals(lngw.id(), conn.localNetworkGateway2Id());
            Assertions.assertEquals("value1", connection.tags().get("tag1"));

            return vngw;
        }

        @Override
        public VirtualNetworkGateway updateResource(VirtualNetworkGateway resource) throws Exception {
            VirtualNetworkGatewayConnection connection = resource.connections().getByName(CONNECTION_NAME);
            Assertions.assertFalse(connection.isBgpEnabled());
            connection.update()
                    .withBgp()
                    .apply();
            Assertions.assertTrue(connection.isBgpEnabled());

            resource.connections().deleteByName(CONNECTION_NAME);
            PagedIterable<VirtualNetworkGatewayConnection> connections = resource.listConnections();
            Assertions.assertEquals(0, TestUtilities.getSize(connections));

            resource.updateTags()
                    .withTag("tag3", "value3")
                    .withoutTag("tag1")
                    .applyTags();
            Assertions.assertEquals("value3", resource.tags().get("tag3"));
            Assertions.assertFalse(resource.tags().containsKey("tag1"));

            return resource;
        }
    }

    /**
     * Test VNet-to-VNet Virtual Network Gateway Connection.
     */
    public class VNetToVNet extends TestTemplate<VirtualNetworkGateway, VirtualNetworkGateways> {

        public VNetToVNet(NetworkManager networkManager) {
            initializeResourceNames(networkManager.getSdkContext());
        }

        @Override
        public void print(VirtualNetworkGateway resource) {
            printVirtualNetworkGateway(resource);
        }

        @Override
        public VirtualNetworkGateway createResource(final VirtualNetworkGateways gateways) throws Exception {

            // Create virtual network gateway
            final List<VirtualNetworkGateway> gws = new ArrayList<>();
            Flux<?> vngwObservable = gateways.define(GATEWAY_NAME1)
                    .withRegion(REGION)
                    .withNewResourceGroup(GROUP_NAME)
                    .withNewNetwork(NETWORK_NAME, "10.11.0.0/16", "10.11.255.0/27")
                    .withRouteBasedVpn()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .createAsync();

            Flux<?> vngw2Observable = gateways.define(GATEWAY_NAME2)
                    .withRegion(REGION)
                    .withNewResourceGroup(GROUP_NAME)
                    .withNewNetwork(NETWORK_NAME + "2","10.41.0.0/16", "10.41.255.0/27")
                    .withRouteBasedVpn()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .createAsync();

            Flux<?> vngw2ObservableSleep = Mono.delay(Duration.ofSeconds(10))
                    .thenMany(vngw2Observable);

            Flux.merge(vngwObservable, vngw2ObservableSleep).map(obj -> {
                if (obj instanceof VirtualNetworkGateway) {
                    gws.add((VirtualNetworkGateway) obj);
                }
                return obj;
            }).blockLast();
            VirtualNetworkGateway vngw1 = gws.get(0);
            VirtualNetworkGateway vngw2 = gws.get(1);
            vngw1.connections()
                    .define(CONNECTION_NAME)
                    .withVNetToVNet()
                    .withSecondVirtualNetworkGateway(vngw2)
                    .withSharedKey("MySecretKey")
                    .create();
            vngw2.connections()
                    .define(CONNECTION_NAME + "2")
                    .withVNetToVNet()
                    .withSecondVirtualNetworkGateway(vngw1)
                    .withSharedKey("MySecretKey")
                    .create();
            PagedIterable<VirtualNetworkGatewayConnection> connections = vngw1.listConnections();
            Assertions.assertEquals(1, TestUtilities.getSize(connections));
            VirtualNetworkGatewayConnection connection = connections.iterator().next();
            Assertions.assertEquals(vngw1.id(), connection.virtualNetworkGateway1Id());
            Assertions.assertEquals(vngw2.id(), connection.virtualNetworkGateway2Id());
            return vngw1;
        }

        @Override
        public VirtualNetworkGateway updateResource(VirtualNetworkGateway resource) throws Exception {
            resource.connections().deleteByName(CONNECTION_NAME);
            PagedIterable<VirtualNetworkGatewayConnection> connections = resource.listConnections();
            Assertions.assertEquals(0, TestUtilities.getSize(connections));
            return resource;
        }
    }

    /**
     * Test VNet-to-VNet Virtual Network Gateway Connection.
     */
    public class PointToSite extends TestTemplate<VirtualNetworkGateway, VirtualNetworkGateways> {

        public PointToSite(NetworkManager networkManager) {
            initializeResourceNames(networkManager.getSdkContext());
        }

        @Override
        public void print(VirtualNetworkGateway resource) {
            printVirtualNetworkGateway(resource);
        }

        @Override
        public VirtualNetworkGateway createResource(final VirtualNetworkGateways gateways) throws Exception {

            // Create virtual network gateway
            initializeResourceNames(gateways.manager().getSdkContext());

            Network network = gateways.manager().networks().define(NETWORK_NAME)
                    .withRegion(REGION)
                    .withNewResourceGroup(GROUP_NAME)
                    .withAddressSpace("192.168.0.0/16")
                    .withAddressSpace("10.254.0.0/16")
                    .withSubnet("GatewaySubnet", "192.168.200.0/24")
                    .withSubnet("FrontEnd", "192.168.1.0/24")
                    .withSubnet("BackEnd", "10.254.1.0/24")
                    .create();
            VirtualNetworkGateway vngw1 = gateways.define(GATEWAY_NAME1)
                    .withRegion(REGION)
                    .withExistingResourceGroup(GROUP_NAME)
                    .withExistingNetwork(network)
                    .withRouteBasedVpn()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .create();

            vngw1.update()
                    .definePointToSiteConfiguration()
                    .withAddressPool("172.16.201.0/24")
                    .withAzureCertificateFromFile(CERTIFICATE_NAME, new File(getClass().getClassLoader().getResource(CERTIFICATE_NAME).getFile()))
                    .attach()
                    .apply();

            Assertions.assertNotNull(vngw1.vpnClientConfiguration());
            Assertions.assertEquals("172.16.201.0/24", vngw1.vpnClientConfiguration().vpnClientAddressPool().addressPrefixes().get(0));
            Assertions.assertEquals(1, vngw1.vpnClientConfiguration().vpnClientRootCertificates().size());
            Assertions.assertEquals(CERTIFICATE_NAME, vngw1.vpnClientConfiguration().vpnClientRootCertificates().get(0).name());
            String profile = vngw1.generateVpnProfile();
            System.out.println(profile);
            return vngw1;
        }

        @Override
        public VirtualNetworkGateway updateResource(VirtualNetworkGateway vngw1) throws Exception {
            vngw1.update().updatePointToSiteConfiguration()
                    .withRevokedCertificate(CERTIFICATE_NAME, "bdf834528f0fff6eaae4c154e06b54322769276c")
                    .parent()
                    .apply();
            Assertions.assertEquals(CERTIFICATE_NAME, vngw1.vpnClientConfiguration().vpnClientRevokedCertificates().get(0).name());

            vngw1.update().updatePointToSiteConfiguration()
                    .withoutAzureCertificate(CERTIFICATE_NAME)
                    .parent()
                    .apply();
            Assertions.assertEquals(0, vngw1.vpnClientConfiguration().vpnClientRootCertificates().size());
            return vngw1;
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

