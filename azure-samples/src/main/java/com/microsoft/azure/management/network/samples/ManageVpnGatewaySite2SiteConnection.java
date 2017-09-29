/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.LocalNetworkGateway;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.VirtualNetworkGateway;
import com.microsoft.azure.management.network.VirtualNetworkGatewayConnection;
import com.microsoft.azure.management.network.VirtualNetworkGatewaySkuName;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.List;

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
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_WEST2;
        final String rgName = SdkContext.randomResourceName("rg", 20);
        final String vnetName = SdkContext.randomResourceName("vnet", 20);
        final String vpnGatewayName = SdkContext.randomResourceName("vngw", 20);
        final String localGatewayName = SdkContext.randomResourceName("lngw", 20);
        final String connectionName = SdkContext.randomResourceName("con", 20);


        try {
            //============================================================
            // Create virtual network
            System.out.println("Creating virtual network...");
            Network network = azure.networks().define(vnetName)
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
            VirtualNetworkGateway vngw = azure.virtualNetworkGateways().define(vpnGatewayName)
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
            LocalNetworkGateway lngw = azure.localNetworkGateways().define(localGatewayName)
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
            List<VirtualNetworkGatewayConnection> connections = vngw.listConnections();

            //============================================================
            // Reset virtual network gateway
            vngw.reset();

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BODY)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageVpnGatewaySite2SiteConnection() {
    }
}