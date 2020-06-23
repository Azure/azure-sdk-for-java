// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.network.models.Troubleshooting;
import com.azure.resourcemanager.network.models.VirtualNetworkGateway;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayConnection;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewaySkuName;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class VirtualNetworkGatewayTests extends TestBase {
    private Azure azure;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        Azure.Authenticated azureAuthed =
            Azure.authenticate(httpPipeline, profile).withSdkContext(sdkContext);
        azure = azureAuthed.withDefaultSubscription();
    }

    @Override
    protected void cleanUpResources() {
    }

    @Test
    @Disabled("Service has bug that cause 'InternalServerError' - record this once service is fixed")
    public void testNetworkWatcherTroubleshooting() throws Exception {
        String gatewayName = sdkContext.randomResourceName("vngw", 8);
        String connectionName = sdkContext.randomResourceName("vngwc", 8);

        TestNetworkWatcher tnw = new TestNetworkWatcher();
        NetworkWatcher nw = tnw.createResource(azure.networkWatchers());
        Region region = nw.region();
        String resourceGroup = nw.resourceGroupName();

        VirtualNetworkGateway vngw1 =
            azure
                .virtualNetworkGateways()
                .define(gatewayName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withNewNetwork("10.11.0.0/16", "10.11.255.0/27")
                .withRouteBasedVpn()
                .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                .create();

        VirtualNetworkGateway vngw2 =
            azure
                .virtualNetworkGateways()
                .define(gatewayName + "2")
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withNewNetwork("10.41.0.0/16", "10.41.255.0/27")
                .withRouteBasedVpn()
                .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                .create();
        VirtualNetworkGatewayConnection connection1 =
            vngw1
                .connections()
                .define(connectionName)
                .withVNetToVNet()
                .withSecondVirtualNetworkGateway(vngw2)
                .withSharedKey("MySecretKey")
                .create();

        // Create storage account to store troubleshooting information
        StorageAccount storageAccount =
            azure
                .storageAccounts()
                .define("sa" + sdkContext.randomResourceName("", 8))
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .create();

        // Troubleshoot connection
        Troubleshooting troubleshooting =
            nw
                .troubleshoot()
                .withTargetResourceId(connection1.id())
                .withStorageAccount(storageAccount.id())
                .withStoragePath(storageAccount.endPoints().primary().blob() + "results")
                .execute();
        Assertions.assertEquals("UnHealthy", troubleshooting.code());

        // Create corresponding connection on second gateway to make it work
        vngw2
            .connections()
            .define(connectionName + "2")
            .withVNetToVNet()
            .withSecondVirtualNetworkGateway(vngw1)
            .withSharedKey("MySecretKey")
            .create();
        SdkContext.sleep(250000);
        troubleshooting =
            nw
                .troubleshoot()
                .withTargetResourceId(connection1.id())
                .withStorageAccount(storageAccount.id())
                .withStoragePath(storageAccount.endPoints().primary().blob() + "results")
                .execute();
        Assertions.assertEquals("Healthy", troubleshooting.code());

        azure.resourceGroups().deleteByName(resourceGroup);
    }

    /**
     * Tests the virtual network gateway implementation.
     *
     * @throws Exception
     */
    @Test
    public void testVirtualNetworkGateways() throws Exception {
        new TestVirtualNetworkGateway().new Basic(azure.virtualNetworkGateways().manager())
            .runTest(azure.virtualNetworkGateways(), azure.resourceGroups());
    }

    /**
     * Tests the virtual network gateway and virtual network gateway connection implementations for Site-to-Site
     * connection.
     *
     * @throws Exception
     */
    @Test
    public void testVirtualNetworkGatewaySiteToSite() throws Exception {
        new TestVirtualNetworkGateway().new SiteToSite(azure.virtualNetworkGateways().manager())
            .runTest(azure.virtualNetworkGateways(), azure.resourceGroups());
    }

    /**
     * Tests the virtual network gateway and virtual network gateway connection implementations for VNet-to-VNet
     * connection.
     *
     * @throws Exception
     */
    @Test
    public void testVirtualNetworkGatewayVNetToVNet() throws Exception {
        new TestVirtualNetworkGateway().new VNetToVNet(azure.virtualNetworkGateways().manager())
            .runTest(azure.virtualNetworkGateways(), azure.resourceGroups());
    }

    /**
     * Tests the virtual network gateway Point-to-Site connection.
     *
     * @throws Exception
     */
    @Test
    public void testVirtualNetworkGatewayPointToSite() throws Exception {
        new TestVirtualNetworkGateway().new PointToSite(azure.virtualNetworkGateways().manager())
            .runTest(azure.virtualNetworkGateways(), azure.resourceGroups());
    }
}
