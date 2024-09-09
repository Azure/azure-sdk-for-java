// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.VirtualWan;
import com.azure.resourcemanager.network.models.VpnSite;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class VpnSiteTests extends NetworkManagementTest {
    private final Region region = Region.US_WEST;

    private VirtualWan createVirtualWan(String vwName, String virtualWanType) {
        return networkManager.virtualWans()
            .define(vwName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .enableVpnEncryption()
            .withAllowBranchToBranchTraffic(true)
            .withVirtualWanType(virtualWanType)
            .create();
    }

    private Creatable<VirtualWan> createVirtualWanCreatable(String vwName, String virtualWanType) {
        return networkManager.virtualWans()
            .define(vwName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .enableVpnEncryption()
            .withAllowBranchToBranchTraffic(true)
            .withVirtualWanType(virtualWanType);
    }

    @Test
    public void canCreateByMiniParametersWithIpAddress() {
        String vpnName = generateRandomResourceName("vpn", 8);
        String vslName = generateRandomResourceName("vsl", 8);

        resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        VpnSite vpnSite = networkManager.vpnSites()
            .define(vpnName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/16")
            .defineVpnSiteLink(vslName)
                .withIpAddress("50.50.50.56")
                .attach()
            .create();

        Assertions.assertEquals(vpnName, vpnSite.name());

        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertIterableEquals(Arrays.asList("10.0.0.0/16"), vpnSite.addressPrefixes());

        Assertions.assertFalse(vpnSite.isSecuritySiteEnabled());

        Assertions.assertNotNull(vpnSite.vpnSiteLinks());
        Assertions.assertEquals(1, vpnSite.vpnSiteLinks().size());
        Assertions.assertEquals(vslName, vpnSite.vpnSiteLinks().get(0).name());
        Assertions.assertEquals("50.50.50.56", vpnSite.vpnSiteLinks().get(0).ipAddress());
        Assertions.assertEquals(0, vpnSite.vpnSiteLinks().get(0).linkProperties().linkSpeedInMbps());
        Assertions.assertNull(vpnSite.vpnSiteLinks().get(0).bgpProperties());

        Assertions.assertNotNull(vpnSite.device());
        Assertions.assertEquals(0, vpnSite.device().linkSpeedInMbps());

        Assertions.assertNotNull(vpnSite.o365Policy());
        Assertions.assertNotNull(vpnSite.o365Policy().breakOutCategories());
        Assertions.assertFalse(vpnSite.o365Policy().breakOutCategories().allow());
        Assertions.assertFalse(vpnSite.o365Policy().breakOutCategories().optimize());
        Assertions.assertFalse(vpnSite.o365Policy().breakOutCategories().defaultProperty());
    }

    @Test
    public void canCreateByMiniParametersWithFqdn() {
        String vpnName = generateRandomResourceName("vpn", 8);
        String vslName = generateRandomResourceName("vsl", 8);

        resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        VpnSite vpnSite = networkManager.vpnSites()
            .define(vpnName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace(Arrays.asList("10.0.0.0/16", "20.0.0.0/16"))
            .defineVpnSiteLink(vslName)
                .withFqdn("link1." + vpnName + ".contoso.com")
                .attach()
            .create();

        Assertions.assertEquals(2, vpnSite.addressPrefixes().size());
        Assertions.assertIterableEquals(Arrays.asList("10.0.0.0/16", "20.0.0.0/16"), vpnSite.addressPrefixes());

        Assertions.assertNotNull(vpnSite.vpnSiteLinks());
        Assertions.assertEquals(1, vpnSite.vpnSiteLinks().size());
        Assertions.assertEquals(vslName, vpnSite.vpnSiteLinks().get(0).name());
        Assertions.assertEquals("link1." + vpnName + ".contoso.com", vpnSite.vpnSiteLinks().get(0).fqdn());
    }

    @Test
    public void canCreateWithVirtualWan() {
        String vpnName1 = generateRandomResourceName("vpn", 8);
        String vpnName2 = generateRandomResourceName("vpn", 8);
        String vpnName3 = generateRandomResourceName("vpn", 8);
        String vwName1 = generateRandomResourceName("vw", 8);
        String vwName2 = generateRandomResourceName("vw", 8);
        String vwName3 = generateRandomResourceName("vw", 8);
        String vslName = generateRandomResourceName("vsl", 8);

        resourceManager.resourceGroups().define(rgName).withRegion(region).create();
        VirtualWan virtualWanForCreate1 = createVirtualWan(vwName1, "Basic");
        VirtualWan virtualWanForCreate2 = createVirtualWan(vwName2, "Standard");
        Creatable<VirtualWan> virtualWanCreatable = createVirtualWanCreatable(vwName3, "Basic");

        VpnSite vpnSite = networkManager.vpnSites()
            .define(vpnName1)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/16")
            .defineVpnSiteLink(vslName)
                .withFqdn("link1." + vpnName1 + ".contoso.com")
                .attach()
            .withVirtualWan(virtualWanForCreate1.id())
            .create();

        Assertions.assertEquals(vwName1, vpnSite.virtualWan().name());
        Assertions.assertEquals("Basic", vpnSite.virtualWan().virtualWanType());

        vpnSite = networkManager.vpnSites()
            .define(vpnName2)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/16")
            .defineVpnSiteLink(vslName)
                .withFqdn("link2." + vpnName2 + ".contoso.com")
                .attach()
            .withVirtualWan(virtualWanForCreate2)
            .create();

        Assertions.assertEquals(vwName2, vpnSite.virtualWan().name());
        Assertions.assertEquals("Standard", vpnSite.virtualWan().virtualWanType());

        vpnSite = networkManager.vpnSites()
            .define(vpnName3)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("30.0.0.0/16")
            .defineVpnSiteLink(vslName)
                .withFqdn("link3." + vpnName3 + ".contoso.com")
                .attach()
            .withVirtualWan(virtualWanCreatable)
            .create();

        Assertions.assertEquals(vwName3, vpnSite.virtualWan().name());
        Assertions.assertEquals("Basic", vpnSite.virtualWan().virtualWanType());
    }

    @Test
    public void canCreateAndUpdateByFullParameters() {
        String vwName1 = generateRandomResourceName("vw", 8);
        String vwName2 = generateRandomResourceName("vw", 8);
        String vwName3 = generateRandomResourceName("vw", 8);
        String vpnName = generateRandomResourceName("vpn", 8);
        String vslName1 = generateRandomResourceName("vsl", 8);
        String vslName2 = generateRandomResourceName("vsl", 8);

        resourceManager.resourceGroups().define(rgName).withRegion(region).create();
        VirtualWan virtualWanForCreate = createVirtualWan(vwName1, "Basic");

        VpnSite vpnSite = networkManager.vpnSites()
            .define(vpnName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/16")
            .defineVpnSiteLink(vslName1)
                .withIpAddress("50.50.50.56")
                .withLinkProperties("vendor1", 10)
                .withBgpProperties("192.168.0.0", 1234L)
                .attach()
            .withVirtualWan(virtualWanForCreate.id())
            .enableSecuritySite()
            .defineDevice()
                .withDeviceVendor("device1")
                .withDeviceModel("Basic")
                .withLinkSpeedInMbps(10)
                .attach()
            .defineO365Policy()
                .withAllow(false)
                .withOptimize(false)
                .withDefaultProperty(false)
                .attach()
            .create();

        Assertions.assertEquals(vpnName, vpnSite.name());

        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertIterableEquals(Arrays.asList("10.0.0.0/16"), vpnSite.addressPrefixes());

        Assertions.assertNotNull(vpnSite.vpnSiteLinks());
        Assertions.assertEquals(1, vpnSite.vpnSiteLinks().size());
        Assertions.assertEquals(vslName1, vpnSite.vpnSiteLinks().get(0).name());
        Assertions.assertEquals("50.50.50.56", vpnSite.vpnSiteLinks().get(0).ipAddress());
        Assertions.assertNotNull(vpnSite.vpnSiteLinks().get(0).linkProperties());
        Assertions.assertEquals("vendor1", vpnSite.vpnSiteLinks().get(0).linkProperties().linkProviderName());
        Assertions.assertEquals(10, vpnSite.vpnSiteLinks().get(0).linkProperties().linkSpeedInMbps());
        Assertions.assertNotNull(vpnSite.vpnSiteLinks().get(0).bgpProperties());
        Assertions.assertEquals("192.168.0.0", vpnSite.vpnSiteLinks().get(0).bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(1234L, vpnSite.vpnSiteLinks().get(0).bgpProperties().asn());

        Assertions.assertEquals(virtualWanForCreate.id(), vpnSite.virtualWan().id());
        Assertions.assertEquals("Basic", vpnSite.virtualWan().virtualWanType());

        Assertions.assertTrue(vpnSite.isSecuritySiteEnabled());

        Assertions.assertNotNull(vpnSite.device());
        Assertions.assertEquals("device1", vpnSite.device().deviceVendor());
        Assertions.assertEquals("Basic", vpnSite.device().deviceModel());
        Assertions.assertEquals(10, vpnSite.device().linkSpeedInMbps());

        Assertions.assertNotNull(vpnSite.o365Policy());
        Assertions.assertNotNull(vpnSite.o365Policy().breakOutCategories());
        Assertions.assertFalse(vpnSite.o365Policy().breakOutCategories().allow());
        Assertions.assertFalse(vpnSite.o365Policy().breakOutCategories().optimize());
        Assertions.assertFalse(vpnSite.o365Policy().breakOutCategories().defaultProperty());

        vpnSite.update().withAddressSpace("20.0.0.0/16").apply();
        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertIterableEquals(Arrays.asList("20.0.0.0/16"), vpnSite.addressPrefixes());

        vpnSite.update().withAddressSpace(Arrays.asList("10.0.0.0/16", "20.0.0.0/16")).apply();
        Assertions.assertEquals(2, vpnSite.addressPrefixes().size());
        Assertions.assertIterableEquals(Arrays.asList("10.0.0.0/16", "20.0.0.0/16"), vpnSite.addressPrefixes());

        vpnSite.update()
            .updateVpnSiteLink(vslName1)
                .withIpAddress("55.55.55.56")
                .withLinkProperties("vendor2", 100)
                .withBgpProperties("172.123.0.0", 2345L)
                .parent()
            .defineVpnSiteLink(vslName2)
                .withIpAddress("60.60.60.67")
                .withLinkProperties("vendor3", 150)
                .withBgpProperties("192.168.1.1", 3456L)
                .attach()
            .apply();
        Assertions.assertEquals(2, vpnSite.vpnSiteLinks().size());
        Assertions.assertEquals("55.55.55.56", vpnSite.vpnSiteLinks().get(0).ipAddress());
        Assertions.assertNotNull(vpnSite.vpnSiteLinks().get(0).linkProperties());
        Assertions.assertEquals("vendor2", vpnSite.vpnSiteLinks().get(0).linkProperties().linkProviderName());
        Assertions.assertEquals(100, vpnSite.vpnSiteLinks().get(0).linkProperties().linkSpeedInMbps());
        Assertions.assertNotNull(vpnSite.vpnSiteLinks().get(0).bgpProperties());
        Assertions.assertEquals("172.123.0.0", vpnSite.vpnSiteLinks().get(0).bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(2345L, vpnSite.vpnSiteLinks().get(0).bgpProperties().asn());

        Assertions.assertEquals("60.60.60.67", vpnSite.vpnSiteLinks().get(1).ipAddress());
        Assertions.assertNotNull(vpnSite.vpnSiteLinks().get(1).linkProperties());
        Assertions.assertEquals("vendor3", vpnSite.vpnSiteLinks().get(1).linkProperties().linkProviderName());
        Assertions.assertEquals(150, vpnSite.vpnSiteLinks().get(1).linkProperties().linkSpeedInMbps());
        Assertions.assertNotNull(vpnSite.vpnSiteLinks().get(1).bgpProperties());
        Assertions.assertEquals("192.168.1.1", vpnSite.vpnSiteLinks().get(1).bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(3456L, vpnSite.vpnSiteLinks().get(1).bgpProperties().asn());

        VirtualWan virtualWanForUpdate = createVirtualWan(vwName2, "Standard");
        vpnSite.update().withVirtualWan(virtualWanForUpdate).apply();
        Assertions.assertEquals(vwName2, vpnSite.virtualWan().name());
        Assertions.assertEquals("Standard", vpnSite.virtualWan().virtualWanType());

        Creatable<VirtualWan> virtualWanCreatable = createVirtualWanCreatable(vwName3, "Basic");
        vpnSite.update().withVirtualWan(virtualWanCreatable).apply();
        Assertions.assertEquals(vwName3, vpnSite.virtualWan().name());
        Assertions.assertEquals("Basic", vpnSite.virtualWan().virtualWanType());

        vpnSite.update().withVirtualWan(virtualWanForUpdate.id()).apply();
        Assertions.assertEquals(vwName2, vpnSite.virtualWan().name());
        Assertions.assertEquals("Standard", vpnSite.virtualWan().virtualWanType());

        vpnSite.update()
            .updateDevice()
                .withDeviceVendor("device2")
                .withDeviceModel("Standard")
                .withLinkSpeedInMbps(50)
                .parent()
            .apply();
        Assertions.assertEquals("device2", vpnSite.device().deviceVendor());
        Assertions.assertEquals("Standard", vpnSite.device().deviceModel());
        Assertions.assertEquals(50, vpnSite.device().linkSpeedInMbps());

        vpnSite.update()
            .updateO365Policy()
                .withDefaultProperty(true)
                .withOptimize(true)
                .withAllow(true)
                .parent()
            .apply();

        Assertions.assertTrue(vpnSite.o365Policy().breakOutCategories().allow());
        Assertions.assertTrue(vpnSite.o365Policy().breakOutCategories().optimize());
        Assertions.assertTrue(vpnSite.o365Policy().breakOutCategories().defaultProperty());
    }
}
