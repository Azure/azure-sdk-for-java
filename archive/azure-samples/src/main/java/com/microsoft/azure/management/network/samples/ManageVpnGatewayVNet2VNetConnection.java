/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.samples;

import com.microsoft.azure.management.Azure;
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
 *  - Create 2 virtual network with subnets
 *  - Create first VPN gateway
 *  - Create second VPN gateway
 *  - Create VPN VNet-to-VNet connection
 *  - List VPN Gateway connections for the first gateway
 */

public final class ManageVpnGatewayVNet2VNetConnection {

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
        final String vpnGateway2Name = SdkContext.randomResourceName("vngw2", 20);
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

            VirtualNetworkGateway vngw1 = azure.virtualNetworkGateways().define(vpnGatewayName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewNetwork("10.11.0.0/16", "10.11.255.0/27")
                    .withRouteBasedVpn()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .create();

            VirtualNetworkGateway vngw2 = azure.virtualNetworkGateways().define(vpnGateway2Name)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewNetwork("10.41.0.0/16", "10.41.255.0/27")
                    .withRouteBasedVpn()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .create();

            vngw1.connections()
                    .define(connectionName)
                    .withVNetToVNet()
                    .withSecondVirtualNetworkGateway(vngw2)
                    .withSharedKey("MySecretKey")
                    .create();


            //============================================================
            // List VPN Gateway connections for particular gateway
            List<VirtualNetworkGatewayConnection> connections = vngw1.listConnections();

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

    private ManageVpnGatewayVNet2VNetConnection() {
    }
}