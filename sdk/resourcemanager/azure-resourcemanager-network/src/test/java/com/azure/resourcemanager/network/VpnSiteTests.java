// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.VirtualWan;
import com.azure.resourcemanager.network.models.VpnSite;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
    public void canCreateAndUpdateVpnSiteByMinimumParametersWithLinkIpAddress() {
        String vwName1 = generateRandomResourceName("vw1", 8);
        String vwName2 = generateRandomResourceName("vw2", 8);
        String vpnName = generateRandomResourceName("vpn", 8);
        String vslName1 = generateRandomResourceName("vsl1", 8);
        String vslName2 = generateRandomResourceName("vsl2", 8);

        resourceManager.resourceGroups().define(rgName).withRegion(region).create();
        VirtualWan virtualWanForCreate = createVirtualWan(vwName1, "Basic");

        VpnSite vpnSite = networkManager.vpnSites()
            .define(vpnName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/16")
            .withVirtualWan(virtualWanForCreate.id())
            .defineVpnSiteLink(vslName1)
            .withIpAddress("50.50.50.56")
            .withLinkProperties("vendor1", 10)
            .withBgpProperties("192.168.0.0", 1234L)
            .attach()
            .create();

        Assertions.assertNotNull(vpnSite.virtualWan());
        Assertions.assertEquals(virtualWanForCreate.id(), vpnSite.virtualWan().id());
        Assertions.assertEquals(vwName1, vpnSite.virtualWan().name());
        Assertions.assertEquals("Basic", vpnSite.virtualWan().virtualWanType());

        Assertions.assertNotNull(vpnSite.addressPrefixes());
        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertEquals("10.0.0.0/16", vpnSite.addressPrefixes().get(0));

        Assertions.assertNotNull(vpnSite.vpnSiteLinks());
        Assertions.assertEquals(1, vpnSite.vpnSiteLinks().size());
        Assertions.assertEquals(vslName1, vpnSite.vpnSiteLinks().get(0).name());
        Assertions.assertEquals("50.50.50.56", vpnSite.vpnSiteLinks().get(0).ipAddress());
        Assertions.assertEquals("vendor1", vpnSite.vpnSiteLinks().get(0).linkProperties().linkProviderName());
        Assertions.assertEquals(10, vpnSite.vpnSiteLinks().get(0).linkProperties().linkSpeedInMbps());
        Assertions.assertEquals("192.168.0.0", vpnSite.vpnSiteLinks().get(0).bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(1234L, vpnSite.vpnSiteLinks().get(0).bgpProperties().asn());

        VirtualWan virtualWanForUpdate = createVirtualWan(vwName2, "Standard");
        vpnSite.update()
            .withVirtualWan(virtualWanForUpdate.id())
            .withAddressSpace("20.0.0.0/16")
            .updateVpnSiteLink(vslName1)
            .withIpAddress("55.55.55.56")
            .withLinkProperties("vendor3", 50)
            .withBgpProperties("196.198.0.0", 3456L)
            .parent()
            .defineVpnSiteLink(vslName2)
            .withIpAddress("60.60.60.67")
            .withLinkProperties("vendor2", 100)
            .withBgpProperties("172.19.0.0", 2345L)
            .attach()
            .apply();

        Assertions.assertNotNull(vpnSite.virtualWan());
        Assertions.assertEquals(virtualWanForUpdate.id(), vpnSite.virtualWan().id());
        Assertions.assertEquals(vwName2, vpnSite.virtualWan().name());
        Assertions.assertEquals("Standard", vpnSite.virtualWan().virtualWanType());

        Assertions.assertNotNull(vpnSite.addressPrefixes());
        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertEquals("20.0.0.0/16", vpnSite.addressPrefixes().get(0));

        Assertions.assertNotNull(vpnSite.vpnSiteLinks());
        Assertions.assertEquals(2, vpnSite.vpnSiteLinks().size());
        Assertions.assertEquals(vslName1, vpnSite.vpnSiteLinks().get(0).name());
        Assertions.assertEquals("55.55.55.56", vpnSite.vpnSiteLinks().get(0).ipAddress());
        Assertions.assertEquals("vendor3", vpnSite.vpnSiteLinks().get(0).linkProperties().linkProviderName());
        Assertions.assertEquals(50, vpnSite.vpnSiteLinks().get(0).linkProperties().linkSpeedInMbps());
        Assertions.assertEquals("196.198.0.0", vpnSite.vpnSiteLinks().get(0).bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(3456L, vpnSite.vpnSiteLinks().get(0).bgpProperties().asn());

        Assertions.assertEquals(vslName2, vpnSite.vpnSiteLinks().get(1).name());
        Assertions.assertEquals("60.60.60.67", vpnSite.vpnSiteLinks().get(1).ipAddress());
        Assertions.assertEquals("vendor2", vpnSite.vpnSiteLinks().get(1).linkProperties().linkProviderName());
        Assertions.assertEquals(100, vpnSite.vpnSiteLinks().get(1).linkProperties().linkSpeedInMbps());
        Assertions.assertEquals("172.19.0.0", vpnSite.vpnSiteLinks().get(1).bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(2345L, vpnSite.vpnSiteLinks().get(1).bgpProperties().asn());
    }

    @Test
    public void canCreateAndUpdateVpnSiteByMinimumParametersWithFqdn() {
        String vwName1 = generateRandomResourceName("vw1", 8);
        String vwName2 = generateRandomResourceName("vw2", 8);
        String vpnName = generateRandomResourceName("vpn", 8);
        String vslName1 = generateRandomResourceName("vsl1", 8);
        String vslName2 = generateRandomResourceName("vsl2", 8);

        resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        VpnSite vpnSite = networkManager.vpnSites()
            .define(vpnName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/16")
            .withVirtualWan(createVirtualWanCreatable(vwName1,  "Basic"))
            .defineVpnSiteLink(vslName1)
            .withFqdn("link1." + vpnName + ".contoso.com")
            .withLinkProperties("vendor1", 10)
            .withBgpProperties("192.168.0.0", 1234L)
            .attach()
            .create();

        Assertions.assertNotNull(vpnSite.virtualWan());
        Assertions.assertEquals(vwName1, vpnSite.virtualWan().name());
        Assertions.assertEquals("Basic", vpnSite.virtualWan().virtualWanType());

        Assertions.assertNotNull(vpnSite.addressPrefixes());
        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertEquals("10.0.0.0/16", vpnSite.addressPrefixes().get(0));

        Assertions.assertNotNull(vpnSite.vpnSiteLinks());
        Assertions.assertEquals(1, vpnSite.vpnSiteLinks().size());
        Assertions.assertEquals(vslName1, vpnSite.vpnSiteLinks().get(0).name());
        Assertions.assertEquals("link1." + vpnName + ".contoso.com", vpnSite.vpnSiteLinks().get(0).fqdn());
        Assertions.assertEquals("vendor1", vpnSite.vpnSiteLinks().get(0).linkProperties().linkProviderName());
        Assertions.assertEquals(10, vpnSite.vpnSiteLinks().get(0).linkProperties().linkSpeedInMbps());
        Assertions.assertEquals("192.168.0.0", vpnSite.vpnSiteLinks().get(0).bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(1234L, vpnSite.vpnSiteLinks().get(0).bgpProperties().asn());

        vpnSite.update()
            .withVirtualWan(createVirtualWanCreatable(vwName2, "Standard"))
            .withAddressSpace("20.0.0.0/16")
            .updateVpnSiteLink(vslName1)
            .withFqdn("link3." + vpnName + ".contoso.com")
            .withLinkProperties("vendor3", 50)
            .withBgpProperties("196.198.0.0", 3456L)
            .parent()
            .defineVpnSiteLink(vslName2)
            .withFqdn("link2." + vpnName + ".contoso.com")
            .withLinkProperties("vendor2", 100)
            .withBgpProperties("172.19.0.0", 2345L)
            .attach()
            .apply();

        Assertions.assertNotNull(vpnSite.virtualWan());
        Assertions.assertEquals(vwName2, vpnSite.virtualWan().name());
        Assertions.assertEquals("Standard", vpnSite.virtualWan().virtualWanType());

        Assertions.assertNotNull(vpnSite.addressPrefixes());
        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertEquals("20.0.0.0/16", vpnSite.addressPrefixes().get(0));

        Assertions.assertNotNull(vpnSite.vpnSiteLinks());
        Assertions.assertEquals(2, vpnSite.vpnSiteLinks().size());

        Assertions.assertEquals(vslName1, vpnSite.vpnSiteLinks().get(0).name());
        Assertions.assertEquals("link3." + vpnName + ".contoso.com", vpnSite.vpnSiteLinks().get(0).fqdn());
        Assertions.assertEquals("vendor3", vpnSite.vpnSiteLinks().get(0).linkProperties().linkProviderName());
        Assertions.assertEquals(50, vpnSite.vpnSiteLinks().get(0).linkProperties().linkSpeedInMbps());
        Assertions.assertEquals("196.198.0.0", vpnSite.vpnSiteLinks().get(0).bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(3456L, vpnSite.vpnSiteLinks().get(0).bgpProperties().asn());

        Assertions.assertEquals(vslName2, vpnSite.vpnSiteLinks().get(1).name());
        Assertions.assertEquals("link2." + vpnName + ".contoso.com", vpnSite.vpnSiteLinks().get(1).fqdn());
        Assertions.assertEquals("vendor2", vpnSite.vpnSiteLinks().get(1).linkProperties().linkProviderName());
        Assertions.assertEquals(100, vpnSite.vpnSiteLinks().get(1).linkProperties().linkSpeedInMbps());
        Assertions.assertEquals("172.19.0.0", vpnSite.vpnSiteLinks().get(1).bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(2345L, vpnSite.vpnSiteLinks().get(1).bgpProperties().asn());
    }

    @Test
    public void canCreateAndUpdateVpnSiteByOtherParameters() {
        String vwName1 = generateRandomResourceName("vw1", 8);
        String vpnName = generateRandomResourceName("vpn", 8);
        String vslName1 = generateRandomResourceName("vsl1", 8);

        resourceManager.resourceGroups().define(rgName).withRegion(region).create();
        VirtualWan virtualWanForCreate = createVirtualWan(vwName1,  "Basic");

        VpnSite vpnSite = networkManager.vpnSites()
            .define(vpnName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/16")
            .withVirtualWan(virtualWanForCreate)
            .defineVpnSiteLink(vslName1)
            .withFqdn("link1." + vpnName + ".contoso.com")
            .withLinkProperties("vendor1", 10)
            .withBgpProperties("192.168.0.0", 1234L)
            .attach()
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

        Assertions.assertTrue(vpnSite.isSecuritySiteEnabled());
        Assertions.assertNotNull(vpnSite.device());
        Assertions.assertEquals("device1", vpnSite.device().deviceVendor());
        Assertions.assertEquals("Basic", vpnSite.device().deviceModel());
        Assertions.assertEquals(10, vpnSite.device().linkSpeedInMbps());
        Assertions.assertNotNull(vpnSite.o365Policy());
        Assertions.assertFalse(vpnSite.o365Policy().breakOutCategories().allow());
        Assertions.assertFalse(vpnSite.o365Policy().breakOutCategories().optimize());
        Assertions.assertFalse(vpnSite.o365Policy().breakOutCategories().defaultProperty());

        vpnSite.update()
            .updateDevice()
            .withDeviceVendor("device2")
            .withDeviceModel("Plus")
            .withLinkSpeedInMbps(100)
            .parent()
            .updateO365Policy()
            .withAllow(true)
            .withOptimize(true)
            .withDefaultProperty(true)
            .parent()
            .apply();

        Assertions.assertNotNull(vpnSite.device());
        Assertions.assertEquals("device2", vpnSite.device().deviceVendor());
        Assertions.assertEquals("Plus", vpnSite.device().deviceModel());
        Assertions.assertEquals(100, vpnSite.device().linkSpeedInMbps());
        Assertions.assertNotNull(vpnSite.o365Policy());
        Assertions.assertTrue(vpnSite.o365Policy().breakOutCategories().allow());
        Assertions.assertTrue(vpnSite.o365Policy().breakOutCategories().optimize());
        Assertions.assertTrue(vpnSite.o365Policy().breakOutCategories().defaultProperty());
    }
}
