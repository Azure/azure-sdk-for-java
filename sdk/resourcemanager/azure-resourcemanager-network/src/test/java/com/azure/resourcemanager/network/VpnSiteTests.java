// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.network.fluent.models.VirtualWanInner;
import com.azure.resourcemanager.network.fluent.models.VpnSiteLinkInner;
import com.azure.resourcemanager.network.models.DeviceProperties;
import com.azure.resourcemanager.network.models.O365BreakOutCategoryPolicies;
import com.azure.resourcemanager.network.models.O365PolicyProperties;
import com.azure.resourcemanager.network.models.VpnLinkBgpSettings;
import com.azure.resourcemanager.network.models.VpnLinkProviderProperties;
import com.azure.resourcemanager.network.models.VpnSite;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class VpnSiteTests extends NetworkManagementTest {
    private final Region region = Region.US_WEST;
    private String vpnName = "";
    private String vwName1 = "";
    private String vwName2 = "";
    private String vslName1 = "";
    private String vslName2 = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        vpnName = generateRandomResourceName("vpn", 8);
        vwName1 = generateRandomResourceName("vw1", 8);
        vwName2 = generateRandomResourceName("vw2", 8);
        vslName1 = generateRandomResourceName("vsl1", 8);
        vslName2 = generateRandomResourceName("vsl2", 8);
        super.initializeClients(httpPipeline, profile);
    }

    private VirtualWanInner createVirtualWan(String vwName, Boolean disableVpnEncryption, String type) {
        return networkManager.serviceClient()
            .getVirtualWans()
            .createOrUpdate(rgName, vwName,
                new VirtualWanInner()
                    .withLocation(region.name())
                    .withDisableVpnEncryption(disableVpnEncryption)
                    .withTypePropertiesType(type));
    }

    @Test
    public void canCreateAndUpdateVpnSiteByMinimumParametersWithLinkIpAddress() {
        resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        VirtualWanInner virtualWanForCreate = createVirtualWan(vwName1, false, "Basic");
        VpnSite vpnSite = networkManager.vpnSites()
            .define(vpnName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withVirtualWan(virtualWanForCreate.id())
            .withAddressSpace("10.0.0.0/16")
            .withVpnSiteLinks(Arrays.asList(
                new VpnSiteLinkInner()
                    .withName(vslName1)
                    .withIpAddress("50.50.50.56")
                    .withLinkProperties(
                        new VpnLinkProviderProperties()
                            .withLinkProviderName("vendor1")
                            .withLinkSpeedInMbps(10))
                    .withBgpProperties(
                        new VpnLinkBgpSettings()
                            .withBgpPeeringAddress("192.168.0.0")
                            .withAsn(1234L))
            ))
            .create();

        vpnSite = networkManager.vpnSites().getById(vpnSite.id());
        Assertions.assertNotNull(vpnSite.virtualWan());
        Assertions.assertEquals(virtualWanForCreate.id(), vpnSite.virtualWan().id());

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

        VirtualWanInner virtualWanForUpdate = createVirtualWan(vwName2, false, "Basic");
        vpnSite.update()
            .withVirtualWan(virtualWanForUpdate.id())
            .withAddressSpace("20.0.0.0/16")
            .withVpnSiteLinks(Arrays.asList(
                new VpnSiteLinkInner()
                    .withName(vslName2)
                    .withIpAddress("60.60.60.67")
                    .withLinkProperties(
                        new VpnLinkProviderProperties()
                            .withLinkProviderName("vendor2")
                            .withLinkSpeedInMbps(100))
                    .withBgpProperties(
                        new VpnLinkBgpSettings()
                            .withBgpPeeringAddress("172.19.0.0")
                            .withAsn(2345L))
            ))
            .apply();

        Assertions.assertNotNull(vpnSite.virtualWan());
        Assertions.assertEquals(virtualWanForUpdate.id(), vpnSite.virtualWan().id());

        Assertions.assertNotNull(vpnSite.addressPrefixes());
        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertEquals("20.0.0.0/16", vpnSite.addressPrefixes().get(0));

        Assertions.assertNotNull(vpnSite.vpnSiteLinks());
        Assertions.assertEquals(2, vpnSite.vpnSiteLinks().size());
        Assertions.assertEquals(vslName1, vpnSite.vpnSiteLinks().get(0).name());
        Assertions.assertEquals("50.50.50.56", vpnSite.vpnSiteLinks().get(0).ipAddress());
        Assertions.assertEquals("vendor1", vpnSite.vpnSiteLinks().get(0).linkProperties().linkProviderName());
        Assertions.assertEquals(10, vpnSite.vpnSiteLinks().get(0).linkProperties().linkSpeedInMbps());
        Assertions.assertEquals("192.168.0.0", vpnSite.vpnSiteLinks().get(0).bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(1234L, vpnSite.vpnSiteLinks().get(0).bgpProperties().asn());

        Assertions.assertEquals(vslName2, vpnSite.vpnSiteLinks().get(1).name());
        Assertions.assertEquals("60.60.60.67", vpnSite.vpnSiteLinks().get(1).ipAddress());
        Assertions.assertEquals("vendor2", vpnSite.vpnSiteLinks().get(1).linkProperties().linkProviderName());
        Assertions.assertEquals(100, vpnSite.vpnSiteLinks().get(1).linkProperties().linkSpeedInMbps());
        Assertions.assertEquals("172.19.0.0", vpnSite.vpnSiteLinks().get(1).bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(2345L, vpnSite.vpnSiteLinks().get(1).bgpProperties().asn());
    }

    @Test
    public void canCreateAndUpdateVpnSiteByMinimumParametersWithFqdn() {
        resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        VirtualWanInner virtualWanForCreate = createVirtualWan(vwName1, false, "Basic");
        VpnSite vpnSite = networkManager.vpnSites()
            .define(vpnName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withVirtualWan(virtualWanForCreate.id())
            .withAddressSpace("10.0.0.0/16")
            .withVpnSiteLinks(Arrays.asList(
                new VpnSiteLinkInner()
                    .withName(vslName1)
                    .withFqdn("link1." + vpnName + ".contoso.com")
                    .withLinkProperties(
                        new VpnLinkProviderProperties()
                            .withLinkProviderName("vendor1")
                            .withLinkSpeedInMbps(10))
                    .withBgpProperties(
                        new VpnLinkBgpSettings()
                            .withBgpPeeringAddress("192.168.0.0")
                            .withAsn(1234L))
            ))
            .create();

        vpnSite = networkManager.vpnSites().getById(vpnSite.id());
        Assertions.assertNotNull(vpnSite.virtualWan());
        Assertions.assertEquals(virtualWanForCreate.id(), vpnSite.virtualWan().id());

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


        VirtualWanInner virtualWanForUpdate = createVirtualWan(vwName2, false, "Basic");
        vpnSite.update()
            .withVirtualWan(virtualWanForUpdate.id())
            .withAddressSpace("20.0.0.0/16")
            .withVpnSiteLinks(Arrays.asList(
                new VpnSiteLinkInner()
                    .withName(vslName2)
                    .withFqdn("link2." + vpnName + ".contoso.com")
                    .withLinkProperties(
                        new VpnLinkProviderProperties()
                            .withLinkProviderName("vendor2")
                            .withLinkSpeedInMbps(100))
                    .withBgpProperties(
                        new VpnLinkBgpSettings()
                            .withBgpPeeringAddress("172.19.0.0")
                            .withAsn(2345L))
            ))
            .apply();

        Assertions.assertNotNull(vpnSite.virtualWan());
        Assertions.assertEquals(virtualWanForUpdate.id(), vpnSite.virtualWan().id());

        Assertions.assertNotNull(vpnSite.addressPrefixes());
        Assertions.assertEquals(1, vpnSite.addressPrefixes().size());
        Assertions.assertEquals("20.0.0.0/16", vpnSite.addressPrefixes().get(0));

        Assertions.assertNotNull(vpnSite.vpnSiteLinks());
        Assertions.assertEquals(2, vpnSite.vpnSiteLinks().size());

        Assertions.assertEquals(vslName1, vpnSite.vpnSiteLinks().get(0).name());
        Assertions.assertEquals("link1." + vpnName + ".contoso.com", vpnSite.vpnSiteLinks().get(0).fqdn());
        Assertions.assertEquals("vendor1", vpnSite.vpnSiteLinks().get(0).linkProperties().linkProviderName());
        Assertions.assertEquals(10, vpnSite.vpnSiteLinks().get(0).linkProperties().linkSpeedInMbps());
        Assertions.assertEquals("192.168.0.0", vpnSite.vpnSiteLinks().get(0).bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(1234L, vpnSite.vpnSiteLinks().get(0).bgpProperties().asn());

        Assertions.assertEquals(vslName2, vpnSite.vpnSiteLinks().get(1).name());
        Assertions.assertEquals("link2." + vpnName + ".contoso.com", vpnSite.vpnSiteLinks().get(1).fqdn());
        Assertions.assertEquals("vendor2", vpnSite.vpnSiteLinks().get(1).linkProperties().linkProviderName());
        Assertions.assertEquals(100, vpnSite.vpnSiteLinks().get(1).linkProperties().linkSpeedInMbps());
        Assertions.assertEquals("172.19.0.0", vpnSite.vpnSiteLinks().get(1).bgpProperties().bgpPeeringAddress());
        Assertions.assertEquals(2345L, vpnSite.vpnSiteLinks().get(1).bgpProperties().asn());
    }

    @Test
    public void canCreateAndUpdateVpnSiteByOtherParameters() {
        resourceManager.resourceGroups().define(rgName).withRegion(region).create();
        VirtualWanInner virtualWanForCreate = createVirtualWan(vwName1, false, "Basic");

        VpnSite vpnSite = networkManager.vpnSites()
            .define(vpnName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withVirtualWan(virtualWanForCreate.id())
            .withAddressSpace("10.0.0.0/16")
            .withVpnSiteLinks(Arrays.asList(
                new VpnSiteLinkInner()
                    .withName(vslName1)
                    .withFqdn("link1." + vpnName + ".contoso.com")
                    .withLinkProperties(
                        new VpnLinkProviderProperties()
                            .withLinkProviderName("vendor1")
                            .withLinkSpeedInMbps(10))
                    .withBgpProperties(
                        new VpnLinkBgpSettings()
                            .withBgpPeeringAddress("192.168.0.0")
                            .withAsn(1234L))
            ))
            .withIsSecuritySite(true)
            .withDevice(
                new DeviceProperties()
                    .withDeviceVendor("device1")
                    .withDeviceModel("Basic")
                    .withLinkSpeedInMbps(10))
            .withO365Policy(
                new O365PolicyProperties()
                    .withBreakOutCategories(
                        new O365BreakOutCategoryPolicies()
                            .withAllow(false)
                            .withOptimize(false)
                            .withDefaultProperty(false)))
            .create();

        vpnSite = networkManager.vpnSites().getById(vpnSite.id());
        Assertions.assertTrue(vpnSite.isSecuritySite());
        Assertions.assertNotNull(vpnSite.device());
        Assertions.assertEquals("device1", vpnSite.device().deviceVendor());
        Assertions.assertEquals("Basic", vpnSite.device().deviceModel());
        Assertions.assertEquals(10, vpnSite.device().linkSpeedInMbps());
        Assertions.assertNotNull(vpnSite.o365Policy());
        Assertions.assertFalse(vpnSite.o365Policy().breakOutCategories().allow());
        Assertions.assertFalse(vpnSite.o365Policy().breakOutCategories().optimize());
        Assertions.assertFalse(vpnSite.o365Policy().breakOutCategories().defaultProperty());

        vpnSite.update()
            .withDevice(
                new DeviceProperties()
                    .withDeviceVendor("device2")
                    .withDeviceModel("Plus")
                    .withLinkSpeedInMbps(100))
            .withO365Policy(
                new O365PolicyProperties()
                    .withBreakOutCategories(
                        new O365BreakOutCategoryPolicies()
                            .withAllow(true)
                            .withOptimize(true)
                            .withDefaultProperty(true)))
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
