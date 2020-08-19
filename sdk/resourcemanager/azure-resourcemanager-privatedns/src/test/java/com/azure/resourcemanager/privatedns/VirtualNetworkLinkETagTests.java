// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.privatedns.models.VirtualNetworkLink;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualNetworkLinkETagTests extends TestBase {
    private final Region region = Region.US_WEST;

    private String rgName = "";
    private String topLevelDomain = "";
    private String vnetName = "";
    private String vnetLinkName = "";
    private String nsgName = "";

    public VirtualNetworkLinkETagTests() {
        super(TestBase.RunCondition.BOTH);
    }

    protected ResourceManager resourceManager;
    protected PrivateDnsZoneManager privateZoneManager;
    protected NetworkManager networkManager;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        resourceManager =
            ResourceManager.authenticate(httpPipeline, profile).withSdkContext(sdkContext).withDefaultSubscription();
        privateZoneManager = PrivateDnsZoneManager.authenticate(httpPipeline, profile, sdkContext);
        networkManager = NetworkManager.authenticate(httpPipeline, profile, sdkContext);
        rgName = generateRandomResourceName("prdnsvnltest", 15);
        topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";
        vnetName = generateRandomResourceName("prdnsvnet", 15);
        vnetLinkName = generateRandomResourceName("prdnsvnetlink", 15);
        nsgName = generateRandomResourceName("prdnsnsg", 15);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateVirtualNetworkLinkWithDefaultETag() {
        NetworkSecurityGroup nsg = createNetworkSecurityGroup();
        Network network = createNetwork(nsg);
        PrivateDnsZone privateDnsZone = createPrivateDnsZone(network);

        PagedIterable<VirtualNetworkLink> virtualNetworkLinks = privateDnsZone.virtualNetworkLinks().list();
        Assertions.assertTrue(TestUtilities.getSize(virtualNetworkLinks) == 1);
        VirtualNetworkLink virtualNetworkLink = virtualNetworkLinks.iterator().next();
        Assertions.assertTrue(vnetLinkName.equals(virtualNetworkLink.name()));
        Assertions.assertNotNull(virtualNetworkLink.etag());
        Assertions.assertFalse(virtualNetworkLink.isAutoRegistrationEnabled());

        Exception compositeException = null;
        try {
            createPrivateDnsZone(network);
        } catch (Exception exception) {
            compositeException = exception;
        }
        validateAggregateException(compositeException);
    }

    @Test
    public void canUpdateVirtualNetworkLinkWithExplicitETag() {
        NetworkSecurityGroup nsg = createNetworkSecurityGroup();
        Network network = createNetwork(nsg);
        PrivateDnsZone privateDnsZone = createPrivateDnsZone(network);

        PagedIterable<VirtualNetworkLink> virtualNetworkLinks = privateDnsZone.virtualNetworkLinks().list();
        Assertions.assertTrue(TestUtilities.getSize(virtualNetworkLinks) == 1);
        VirtualNetworkLink virtualNetworkLink = virtualNetworkLinks.iterator().next();
        Assertions.assertTrue(vnetLinkName.equals(virtualNetworkLink.name()));
        Assertions.assertNotNull(virtualNetworkLink.etag());
        Assertions.assertFalse(virtualNetworkLink.isAutoRegistrationEnabled());

        Exception compositeException = null;
        try {
            privateDnsZone.update()
                .updateVirtualNetworkLink(vnetLinkName)
                    .enableAutoRegistration()
                    .withETagCheck(virtualNetworkLink.etag() + "-foo")
                    .parent()
                .apply();
        } catch (Exception exception) {
            compositeException = exception;
        }
        validateAggregateException(compositeException);

        privateDnsZone.update()
            .updateVirtualNetworkLink(vnetLinkName)
                .enableAutoRegistration()
                .withETagCheck(virtualNetworkLink.etag())
                .parent()
            .apply();

        virtualNetworkLinks = privateDnsZone.virtualNetworkLinks().list();
        Assertions.assertTrue(TestUtilities.getSize(virtualNetworkLinks) == 1);
        virtualNetworkLink = virtualNetworkLinks.iterator().next();
        Assertions.assertTrue(virtualNetworkLink.isAutoRegistrationEnabled());
    }

    @Test
    public void canDeleteVirtualNetworkLinkWithExplicitETag() {
        NetworkSecurityGroup nsg = createNetworkSecurityGroup();
        Network network = createNetwork(nsg);
        PrivateDnsZone privateDnsZone = createPrivateDnsZone(network);

        PagedIterable<VirtualNetworkLink> virtualNetworkLinks = privateDnsZone.virtualNetworkLinks().list();
        Assertions.assertTrue(TestUtilities.getSize(virtualNetworkLinks) == 1);
        VirtualNetworkLink virtualNetworkLink = virtualNetworkLinks.iterator().next();
        Assertions.assertTrue(vnetLinkName.equals(virtualNetworkLink.name()));
        Assertions.assertNotNull(virtualNetworkLink.etag());
        Assertions.assertFalse(virtualNetworkLink.isAutoRegistrationEnabled());

        Exception compositeException = null;
        try {
            privateDnsZone.update()
                .withoutVirtualNetworkLink(vnetLinkName, virtualNetworkLink.etag() + "-foo")
                .apply();
        } catch (Exception exception) {
            compositeException = exception;
        }
        validateAggregateException(compositeException);

        privateDnsZone.update()
            .withoutVirtualNetworkLink(vnetLinkName, virtualNetworkLink.etag())
            .apply();

        virtualNetworkLinks = privateDnsZone.virtualNetworkLinks().list();
        Assertions.assertTrue(TestUtilities.getSize(virtualNetworkLinks) == 0);
    }

    private NetworkSecurityGroup createNetworkSecurityGroup() {
        return networkManager.networkSecurityGroups().define(nsgName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .create();
    }

    private Network createNetwork(NetworkSecurityGroup nsg) {
        return networkManager.networks().define(vnetName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/28")
            .withAddressSpace("10.1.0.0/28")
            .withSubnet("subnetA", "10.0.0.0/29")
            .defineSubnet("subnetB")
            .withAddressPrefix("10.0.0.8/29")
            .withExistingNetworkSecurityGroup(nsg)
            .attach()
            .create();
    }

    private PrivateDnsZone createPrivateDnsZone(Network network) {
        return privateZoneManager.privateZones().define(topLevelDomain)
            .withExistingResourceGroup(rgName)
            .defineVirtualNetworkLink(vnetLinkName)
            .disableAutoRegistration()
            .withVirtualNetworkId(network.id())
            .withETagCheck()
            .attach()
            .create();
    }

    private void validateAggregateException(Exception compositeException) {
        Assertions.assertNotNull(compositeException);
        Assertions.assertTrue(compositeException.getSuppressed().length > 0);
        for (int i = 0; i < compositeException.getSuppressed().length; ++i) {
            Throwable exception = compositeException.getSuppressed()[i];
            if (exception instanceof ManagementException) {
                ManagementError cloudError = ((ManagementException) exception).getValue();
                Assertions.assertNotNull(cloudError);
                Assertions.assertNotNull(cloudError.getCode());
                Assertions.assertTrue(cloudError.getCode().contains("PreconditionFailed"));
            }
        }
    }
}
