// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.VirtualWan;
import com.azure.resourcemanager.network.models.VpnSite;
import com.azure.resourcemanager.network.models.VpnSiteLink;
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
    public void canCreateAndUpdateWithBasicParameters() {
        String vwName1 = generateRandomResourceName("vw", 8);
        String vwName2 = generateRandomResourceName("vw", 8);
        String vwName3 = generateRandomResourceName("vw", 8);
        String vslName1 = generateRandomResourceName("vsl", 8);
        String vslName2 = generateRandomResourceName("vsl", 8);
        String vslName3 = generateRandomResourceName("vsl", 8);
        String vslName4 = generateRandomResourceName("vsl", 8);
        String vpnName = generateRandomResourceName("vpn", 8);

        resourceManager.resourceGroups().define(rgName).withRegion(region).create();
        VirtualWan basicVirtualWan = createVirtualWan(vwName1, "Basic");
        VirtualWan standardVirtualWan = createVirtualWan(vwName2, "Standard");
        Creatable<VirtualWan> virtualWanCreatable = createVirtualWanCreatable(vwName3, "Basic");

        // Create VPN site by basic parameters and virtual wan instance
        VpnSite vpnSite = networkManager.vpnSites()
            .define(vpnName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/16")
            .defineVpnSiteLink(vslName1)
                .withIpAddress("50.50.50.56")
                .attach()
            .withVirtualWan(basicVirtualWan)
            .create();

        Assertions.assertEquals(vpnName, vpnSite.name());
        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertEquals("10.0.0.0/16", vpnSite.addressPrefixes().get(0));
        Assertions.assertEquals(1, vpnSite.vpnSiteLinks().size());
        Assertions.assertEquals("50.50.50.56", vpnSite.vpnSiteLinks().get(0).ipAddress());
        Assertions.assertEquals(vwName1, vpnSite.virtualWan().name());
        Assertions.assertEquals("Basic", vpnSite.virtualWan().virtualWanType());
        networkManager.vpnSites().deleteById(vpnSite.id());

        // Create VPN site by basic parameters and virtual wan id
        vpnSite = networkManager.vpnSites()
            .define(vpnName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/16")
            .defineVpnSiteLink(vslName1)
                .withFqdn("link1." + vpnName + ".contoso.com")
                .attach()
            .withVirtualWan(standardVirtualWan.id())
            .create();

        Assertions.assertEquals(vpnName, vpnSite.name());
        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertEquals("10.0.0.0/16", vpnSite.addressPrefixes().get(0));
        Assertions.assertEquals(1, vpnSite.vpnSiteLinks().size());
        Assertions.assertEquals("link1." + vpnName + ".contoso.com", vpnSite.vpnSiteLinks().get(0).fqdn());
        Assertions.assertEquals(vwName2, vpnSite.virtualWan().name());
        Assertions.assertEquals("Standard", vpnSite.virtualWan().virtualWanType());
        networkManager.vpnSites().deleteById(vpnSite.id());

        // Create VPN site by basic parameters and virtual wan creatable
        vpnSite = networkManager.vpnSites()
            .define(vpnName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/16")
            .defineVpnSiteLink(vslName1)
                .withIpAddress("50.50.50.56")
                .attach()
            .defineVpnSiteLink(vslName2)
                .withFqdn("link1." + vpnName + ".contoso.com")
                .attach()
            .withVirtualWan(virtualWanCreatable)
            .create();

        Assertions.assertEquals(vpnName, vpnSite.name());
        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertEquals("10.0.0.0/16", vpnSite.addressPrefixes().get(0));
        Assertions.assertNull(vpnSite.vpnSiteLinks().get(0).fqdn());
        Assertions.assertEquals(2, vpnSite.vpnSiteLinks().size());
        VpnSiteLink vpnSiteLink = vpnSite.vpnSiteLinks().get(0);
        Assertions.assertEquals(vslName1, vpnSiteLink.name());
        Assertions.assertEquals("50.50.50.56", vpnSiteLink.ipAddress());
        Assertions.assertNull(vpnSiteLink.fqdn());
        vpnSiteLink = vpnSite.vpnSiteLinks().get(1);
        Assertions.assertEquals(vslName2, vpnSiteLink.name());
        Assertions.assertNull(vpnSiteLink.ipAddress());
        Assertions.assertEquals("link1." + vpnName + ".contoso.com", vpnSiteLink.fqdn());
        Assertions.assertEquals(vwName3, vpnSite.virtualWan().name());
        Assertions.assertEquals("Basic", vpnSite.virtualWan().virtualWanType());

        // Update the address space of the VPN site
        vpnSite.update().withAddressSpace("20.0.0.0/16").apply();
        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertEquals("20.0.0.0/16", vpnSite.addressPrefixes().get(0));

        vpnSite.update().withAddressSpace(Arrays.asList("10.0.0.0/16", "20.0.0.0/16")).apply();
        Assertions.assertEquals(2, vpnSite.addressPrefixes().size());
        Assertions.assertIterableEquals(Arrays.asList("10.0.0.0/16", "20.0.0.0/16"), vpnSite.addressPrefixes());

        vpnSite.update().withAddressSpace("20.0.0.0/16").apply();
        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertEquals("20.0.0.0/16", vpnSite.addressPrefixes().get(0));

        // Update the links of the VPN site
        vpnSite.update()
            .updateVpnSiteLink(vslName1)
                .withIpAddress("55.55.55.55")
                .parent()
            .updateVpnSiteLink(vslName2)
                .withFqdn("link2." + vpnName + ".contoso.com")
                .parent()
            .apply();
        Assertions.assertEquals(2, vpnSite.vpnSiteLinks().size());
        vpnSiteLink = vpnSite.vpnSiteLinks().get(0);
        Assertions.assertNull(vpnSiteLink.fqdn());
        Assertions.assertEquals("55.55.55.55", vpnSiteLink.ipAddress());
        vpnSiteLink = vpnSite.vpnSiteLinks().get(1);
        Assertions.assertNull(vpnSiteLink.ipAddress());
        Assertions.assertEquals("link2." + vpnName + ".contoso.com", vpnSiteLink.fqdn());

        vpnSite.update()
            .updateVpnSiteLink(vslName1)
                .withFqdn("link2." + vpnName + ".contoso.com")
                .parent()
            .updateVpnSiteLink(vslName2)
                .withIpAddress("55.55.55.55")
                .parent()
            .apply();
        Assertions.assertEquals(2, vpnSite.vpnSiteLinks().size());
        vpnSiteLink = vpnSite.vpnSiteLinks().get(0);
        Assertions.assertNull(vpnSiteLink.ipAddress());
        Assertions.assertEquals("link2." + vpnName + ".contoso.com", vpnSiteLink.fqdn());
        vpnSiteLink = vpnSite.vpnSiteLinks().get(1);
        Assertions.assertNull(vpnSiteLink.fqdn());
        Assertions.assertEquals("55.55.55.55", vpnSiteLink.ipAddress());

        vpnSite.update()
            .defineVpnSiteLink(vslName3)
                .withIpAddress("172.19.0.0")
                .attach()
            .defineVpnSiteLink(vslName4)
                .withFqdn("link1." + vpnName + ".contoso.com")
                .attach()
            .apply();

        Assertions.assertEquals(4, vpnSite.vpnSiteLinks().size());
        vpnSiteLink = vpnSite.vpnSiteLinks().get(2);
        Assertions.assertEquals(vslName3, vpnSiteLink.name());
        Assertions.assertNull(vpnSiteLink.fqdn());
        Assertions.assertEquals("172.19.0.0", vpnSiteLink.ipAddress());
        vpnSiteLink = vpnSite.vpnSiteLinks().get(3);
        Assertions.assertEquals(vslName4, vpnSiteLink.name());
        Assertions.assertNull(vpnSiteLink.ipAddress());
        Assertions.assertEquals("link1." + vpnName + ".contoso.com", vpnSiteLink.fqdn());

        // Update the virtual wan of the VPN site
        vpnSite.update().withVirtualWan(basicVirtualWan.id()).apply();
        Assertions.assertEquals(vwName1, vpnSite.virtualWan().name());
        Assertions.assertEquals("Basic", vpnSite.virtualWan().virtualWanType());

        vpnSite.update().withVirtualWan(standardVirtualWan).apply();
        Assertions.assertEquals(vwName2, vpnSite.virtualWan().name());
        Assertions.assertEquals("Standard", vpnSite.virtualWan().virtualWanType());

        vpnSite.update().withVirtualWan(virtualWanCreatable).apply();
        Assertions.assertEquals(vwName3, vpnSite.virtualWan().name());
        Assertions.assertEquals("Basic", vpnSite.virtualWan().virtualWanType());
    }

    @Test
    public void canCreateAndUpdateWithOtherParameters() {
        String vwName = generateRandomResourceName("vw", 8);
        String vpnName = generateRandomResourceName("vpn", 8);
        String vslName1 = generateRandomResourceName("vsl", 8);
        String vslName2 = generateRandomResourceName("vsl", 8);

        resourceManager.resourceGroups().define(rgName).withRegion(region).create();
        VirtualWan basicVirtualWan = createVirtualWan(vwName, "Basic");

        // Create VPN site with extended parameters
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
            .withVirtualWan(basicVirtualWan)
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

        Assertions.assertNotNull(vpnSite.vpnSiteLinks());
        Assertions.assertEquals(1, vpnSite.vpnSiteLinks().size());

        VpnSiteLink vpnSiteLink = vpnSite.vpnSiteLinks().get(0);
        Assertions.assertNotNull(vpnSiteLink.linkProperties());
        Assertions.assertEquals("vendor1", vpnSiteLink.linkProperties().linkProviderName());
        Assertions.assertEquals(10, vpnSiteLink.linkProperties().linkSpeedInMbps());
        Assertions.assertNotNull(vpnSiteLink.bgpProperties());
        Assertions.assertEquals("192.168.0.0", vpnSiteLink.bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(1234L, vpnSiteLink.bgpProperties().asn());

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

        // Update other parameters of VPN site link
        vpnSite.update()
            .updateVpnSiteLink(vslName1)
                .withLinkProperties("vendor2", 50)
                .withBgpProperties("172.19.0.0", 2345L)
                .parent()
            .defineVpnSiteLink(vslName2)
                .withIpAddress("60.60.60.67")
                .withLinkProperties("vendor3", 100)
                .withBgpProperties("196.196.0.0", 1000L)
                .attach()
            .apply();

        Assertions.assertEquals(2, vpnSite.vpnSiteLinks().size());

        vpnSiteLink = vpnSite.vpnSiteLinks().get(0);
        Assertions.assertNotNull(vpnSiteLink.linkProperties());
        Assertions.assertEquals("vendor2", vpnSiteLink.linkProperties().linkProviderName());
        Assertions.assertEquals(50, vpnSiteLink.linkProperties().linkSpeedInMbps());
        Assertions.assertNotNull(vpnSiteLink.bgpProperties());
        Assertions.assertEquals("172.19.0.0", vpnSiteLink.bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(2345L, vpnSiteLink.bgpProperties().asn());
        vpnSiteLink = vpnSite.vpnSiteLinks().get(1);
        Assertions.assertNotNull(vpnSiteLink.linkProperties());
        Assertions.assertEquals("vendor3", vpnSiteLink.linkProperties().linkProviderName());
        Assertions.assertEquals(100, vpnSiteLink.linkProperties().linkSpeedInMbps());
        Assertions.assertNotNull(vpnSiteLink.bgpProperties());
        Assertions.assertEquals("196.196.0.0", vpnSiteLink.bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(1000L, vpnSiteLink.bgpProperties().asn());

        // Update VPN device of VPN site
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

        // Update O365 policy of VPN site
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
