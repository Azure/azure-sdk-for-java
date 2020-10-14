// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.samples;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.LocalNetworkGateway;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.VirtualNetworkGateway;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewaySkuName;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

/**
 * Azure Network sample for managing virtual network gateway.
 *  - Create virtual network with gateway subnet
 *  - Create VPN gateway
 *  - Create local network gateway
 *  - Create VPN Site-to-Site connection
 *  - List VPN Gateway connections for particular gateway
 *  - Reset virtual network gateway
 */

public final class ManageVpnGatewaySite2SiteConnection {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final Region region = Region.US_WEST2;
        final String rgName = Utils.randomResourceName(azureResourceManager, "rg", 20);
        final String vnetName = Utils.randomResourceName(azureResourceManager, "vnet", 20);
        final String vpnGatewayName = Utils.randomResourceName(azureResourceManager, "vngw", 20);
        final String localGatewayName = Utils.randomResourceName(azureResourceManager, "lngw", 20);
        final String connectionName = Utils.randomResourceName(azureResourceManager, "con", 20);


        try {
            //============================================================
            // Create virtual network
            System.out.println("Creating virtual network...");
            Network network = azureResourceManager.networks().define(vnetName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withAddressSpace("10.11.0.0/16")
                    .withSubnet("GatewaySubnet", "10.11.255.0/27")
                    .create();
            System.out.println("Created network");
            // Print the virtual network
            Utils.print(network);

            //============================================================
            // Create VPN gateway
            System.out.println("Creating virtual network gateway...");
            VirtualNetworkGateway vngw = azureResourceManager.virtualNetworkGateways().define(vpnGatewayName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingNetwork(network)
                    .withRouteBasedVpn()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .create();
            System.out.println("Created virtual network gateway");

            //============================================================
            // Create local network gateway
            System.out.println("Creating virtual network gateway...");
            LocalNetworkGateway lngw = azureResourceManager.localNetworkGateways().define(localGatewayName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withIPAddress("40.71.184.214")
                    .withAddressSpace("192.168.3.0/24")
                    .create();
            System.out.println("Created virtual network gateway");

            //============================================================
            // Create VPN Site-to-Site connection
            System.out.println("Creating virtual network gateway connection...");
            vngw.connections()
                    .define(connectionName)
                    .withSiteToSite()
                    .withLocalNetworkGateway(lngw)
                    .withSharedKey("MySecretKey")
                    .create();
            System.out.println("Created virtual network gateway connection");

            //============================================================
            // List VPN Gateway connections for particular gateway
            System.out.println("List connections...");
            vngw.listConnections().forEach(connection -> System.out.println(connection.name()));
            System.out.println();

            //============================================================
            // Reset virtual network gateway
            vngw.reset();

            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageVpnGatewaySite2SiteConnection() {
    }
}
